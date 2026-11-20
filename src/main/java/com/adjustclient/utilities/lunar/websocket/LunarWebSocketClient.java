package com.adjustclient.utilities.lunar.websocket;

import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.utilities.lunar.LunarClientService;
import com.adjustclient.utilities.proto.ProtoReader;
import com.adjustclient.utilities.proto.ProtoWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public final class LunarWebSocketClient {
    private final LunarTabLogoCache cache;
    private final AtomicInteger requestCounter;
    final Set<String> pendingLoginIds;
    private final Object lifecycleLock;
    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler;
    public volatile WebSocket socket;
    private volatile CompletableFuture<Void> disconnected;
    volatile ScheduledFuture<?> heartbeat;
    private volatile Thread reconnectThread;
    public volatile Runnable reconnected;
    volatile long generation;
    public volatile boolean connected;
    volatile boolean running;
    private Object field60;
    private static final URI GAME_URI;

    static {
        GAME_URI = URI.create("wss://websocket.lunarclientprod.com/game");
    }

    public LunarWebSocketClient(LunarTabLogoCache cache) {
        this.cache = cache;
        requestCounter = new AtomicInteger(1);
        pendingLoginIds = ConcurrentHashMap.newKeySet();
        lifecycleLock = new Object();
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        scheduler = Executors.newSingleThreadScheduledExecutor(
                task -> {
                    Thread thread = new Thread(task, "LunarClient-WS-Heartbeat");
                    thread.setDaemon(true);
                    return thread;
                });
    }

    byte[] encodeHandshake(String jwt) {
        Objects.requireNonNull(jwt, "jwt");
        Session session = Minecraft.getMinecraft().getSession();
        UUID uuid = session.getProfile().getId();
        ProtoWriter playerId = new ProtoWriter();
        playerId.writeFixed64(1, uuid.getMostSignificantBits());
        playerId.writeFixed64(2, uuid.getLeastSignificantBits());
        ProtoWriter identity = new ProtoWriter();
        identity.writeBytes(1, playerId.toByteArray());
        identity.writeString(2, session.getUsername());
        ProtoWriter authentication = new ProtoWriter();
        authentication.writeBytes(1, identity.toByteArray());
        authentication.writeInt32(2, 1);
        authentication.writeString(3, jwt);
        String version = Objects.requireNonNull(LunarClientService.INSTANCE.version,
                "Lunar Client version is unavailable");
        String commit = Objects.requireNonNull(LunarClientService.INSTANCE.gitCommit,
                "Lunar Client git commit is unavailable");
        ProtoWriter clientVersion = new ProtoWriter();
        clientVersion.writeString(1, version);
        ProtoWriter language = new ProtoWriter();
        language.writeString(1, "eng");
        language.writeString(2, "en_US");
        ProtoWriter minecraftVersion = new ProtoWriter();
        minecraftVersion.writeString(1, "V1_8");
        ProtoWriter build = new ProtoWriter();
        build.writeString(1, "dev");
        build.writeString(2, commit.substring(0, Math.min(commit.length(), 7)));
        build.writeString(3, version);
        ProtoWriter gameBuild = new ProtoWriter();
        gameBuild.writeBytes(1, minecraftVersion.toByteArray());
        gameBuild.writeBytes(2, build.toByteArray());
        ProtoWriter handshake = new ProtoWriter();
        handshake.writeBytes(1, authentication.toByteArray());
        handshake.writeBytes(2, clientVersion.toByteArray());
        handshake.writeString(5, operatingSystem());
        handshake.writeString(6, architecture());
        handshake.writeBytes(7, language.toByteArray());
        handshake.writeBytes(8, gameBuild.toByteArray());
        return handshake.toByteArray();
    }

    static String describeError(Throwable error) {
        while ((error instanceof CompletionException || error instanceof ExecutionException) && error.getCause() != null) {
            error = error.getCause();
        }
        String message = error.getMessage();
        return message == null || message.isBlank() ? error.getClass().getSimpleName() : message;
    }

    private UUID decodeUuid(byte[] bytes) {
        ProtoReader reader = new ProtoReader(ByteBuffer.wrap(bytes));
        long most = 0L;
        long least = 0L;
        for (int tag; (tag = reader.readTag()) != 0;) {
            if (tag >>> 3 == 1) most = reader.readFixed64();
            else if (tag >>> 3 == 2) least = reader.readFixed64();
            else reader.skipField(tag);
        }
        return new UUID(most, least);
    }

    public void requestTabLogos(List<UUID> uuids) {
        if (connected && socket != null && !socket.isInputClosed() && !socket.isOutputClosed() && !uuids.isEmpty()) {
            sendRpc("lunarclient.websocket.cosmetic.v2.CosmeticService", "LoadTabLogos", encodeUuids(uuids));
        }
    }

    public void start() {
        synchronized (lifecycleLock) {
            if (running || (connected && socket != null && !socket.isInputClosed() && !socket.isOutputClosed())) {
                return;
            }
            running = true;
            long connectionGeneration = ++generation;
            Callable<String> fetcher = LunarTabLogoCache.jwtFetcher();
            reconnectThread = new Thread(
                    () -> reconnectLoop(fetcher, connectionGeneration), "LunarClient-WS");
            reconnectThread.setDaemon(true);
            reconnectThread.start();
        }
    }

    private void connect(String jwt, long connectionGeneration) throws Exception {
        CompletableFuture<Void> completion = new CompletableFuture<>();
        disconnected = completion;
        LunarWebSocketListener listener = new LunarWebSocketListener(this, jwt, completion, connectionGeneration);
        WebSocket opened;
        try {
            opened = httpClient.newWebSocketBuilder()
                    .header("Accept", "application/x-protobuf")
                    .connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(GAME_URI, listener).get();
        } catch (ExecutionException error) {
            if (error.getCause() instanceof Exception) throw (Exception) error.getCause();
            throw error;
        }
        if (!running || connectionGeneration != generation) {
            opened.abort();
            return;
        }
        completion.get();
    }

    private void reconnectLoop(Callable<String> jwtFetcher, long connectionGeneration) {
        long delay = 2000L;
        try {
            while (running && connectionGeneration == generation) {
                try {
                    String jwt = Objects.requireNonNull(jwtFetcher.call(), "jwtFetcher returned null");
                    connect(jwt, connectionGeneration);
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception error) {
                    Logger.log(LogLevel.WARN, true, "WebSocket connection failed: {}", describeError(error));
                } finally {
                    clearConnection(connectionGeneration);
                }
                if (!running || connectionGeneration != generation) break;
                if (Logger.debug) Logger.log(LogLevel.INFO, true, "Reconnecting WebSocket in {}ms...", delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    return;
                }
                delay = Math.min(delay * 2L, 30000L);
            }
        } finally {
            synchronized (lifecycleLock) {
                if (connectionGeneration == generation) {
                    running = false;
                    reconnectThread = null;
                }
            }
        }
    }

    String sendRpc(String service, String method, byte[] payload) {
        WebSocket current = socket;
        if (!connected || current == null || current.isInputClosed() || current.isOutputClosed()) {
            throw new IllegalStateException("WebSocket is not connected");
        }
        String id = Integer.toString(requestCounter.getAndIncrement());
        byte[] frame = encodeRpc(id.getBytes(StandardCharsets.UTF_8), service, method, payload);
        CompletableFuture<Void> completion = disconnected;
        current.sendBinary(ByteBuffer.wrap(frame), true).whenComplete(
                (result, error) -> sendCompleted(completion, current, result, error));
        return id;
    }

    void connectionFailed(WebSocket failedSocket, CompletableFuture<Void> completion, Throwable error) {
        if (socket == failedSocket) {
            connected = false;
            if (heartbeat != null) {
                heartbeat.cancel(false);
                heartbeat = null;
            }
        }
        completion.completeExceptionally(error);
        failedSocket.abort();
    }

    private void sendCompleted(CompletableFuture<Void> completion, WebSocket sentSocket,
                               WebSocket result, Throwable error) {
        if (error != null && completion != null) {
            Logger.log(LogLevel.WARN, true, "WebSocket send failed: {}", describeError(error));
            connectionFailed(sentSocket, completion, error);
        }
    }

    private byte[] encodeRpc(byte[] requestId, String service, String method, byte[] payload) {
        ProtoWriter writer = new ProtoWriter();
        writer.writeBytes(1, requestId);
        writer.writeString(2, service);
        writer.writeString(3, method);
        writer.writeBytes(4, payload);
        return writer.toByteArray();
    }

    public void stop() {
        WebSocket previous;
        CompletableFuture<Void> completion;
        Thread thread;
        synchronized (lifecycleLock) {
            running = false;
            ++generation;
            connected = false;
            if (heartbeat != null) {
                heartbeat.cancel(false);
                heartbeat = null;
            }
            pendingLoginIds.clear();
            previous = socket;
            socket = null;
            completion = disconnected;
            disconnected = null;
            thread = reconnectThread;
            reconnectThread = null;
        }
        if (completion != null) completion.complete(null);
        if (previous != null) previous.abort();
        if (thread != null) thread.interrupt();
    }

    private void clearConnection(long connectionGeneration) {
        synchronized (lifecycleLock) {
            if (connectionGeneration == generation) {
                connected = false;
                if (heartbeat != null) {
                    heartbeat.cancel(false);
                    heartbeat = null;
                }
                socket = null;
                disconnected = null;
            }
        }
    }

    private int decodeColor(byte[] bytes) {
        ProtoReader reader = new ProtoReader(ByteBuffer.wrap(bytes));
        for (int tag; (tag = reader.readTag()) != 0;) {
            if (tag >>> 3 == 1) return (int) reader.readEnum();
            reader.skipField(tag);
        }
        return 0;
    }

    private byte[] encodeUuids(List<UUID> uuids) {
        ProtoWriter writer = new ProtoWriter();
        for (UUID uuid : uuids) {
            Objects.requireNonNull(uuid, "uuids contains null");
            ProtoWriter entry = new ProtoWriter();
            entry.writeFixed64(1, uuid.getMostSignificantBits());
            entry.writeFixed64(2, uuid.getLeastSignificantBits());
            writer.writeBytes(1, entry.toByteArray());
        }
        return writer.toByteArray();
    }

    void startHeartbeat() {
        if (heartbeat != null) {
            heartbeat.cancel(false);
        }
        heartbeat = scheduler.scheduleAtFixedRate(
                () -> {
                    if (connected && socket != null && !socket.isInputClosed() && !socket.isOutputClosed()) {
                        try {
                            sendRpc("lunarclient.websocket.heartbeat.v1.HeartbeatService", "GameHeartbeat", new byte[0]);
                        } catch (Exception error) {
                            Logger.log(LogLevel.WARN, true, "WebSocket heartbeat failed: {}", describeError(error));
                        }
                    }
                }, 60L, 60L, TimeUnit.SECONDS);
    }

    private void decodeTabLogos(byte[] bytes) {
        ProtoReader reader = new ProtoReader(ByteBuffer.wrap(bytes));
        for (int tag; (tag = reader.readTag()) != 0;) {
            if (tag >>> 3 == 1) decodeTabLogo(reader.readBytes());
            else reader.skipField(tag);
        }
    }

    private void decodeTabLogo(byte[] bytes) {
        ProtoReader reader = new ProtoReader(ByteBuffer.wrap(bytes));
        UUID uuid = null;
        float red = 1.0F;
        float green = 1.0F;
        float blue = 1.0F;
        float alpha = 1.0F;
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag >>> 3) {
                case 1:
                    uuid = decodeUuid(reader.readBytes());
                    break;
                case 2:
                    int color = decodeColor(reader.readBytes());
                    alpha = (color & 0xFF000000) != 0 ? ((color >>> 24) & 255) / 255.0F : 1.0F;
                    blue = (color & 255) / 255.0F;
                    green = ((color >>> 8) & 255) / 255.0F;
                    red = ((color >>> 16) & 255) / 255.0F;
                    break;
                case 3:
                    reader.readBytes();
                    break;
                case 4:
                    reader.readEnum();
                    break;
                default:
                    reader.skipField(tag);
                    break;
            }
        }
        if (uuid != null) {
            cache.logos.put(uuid, new LunarTabLogo(uuid, red, green, blue, alpha));
        }
    }

    private static String operatingSystem() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac") || os.contains("darwin")) return "darwin";
        if (os.contains("win")) return "win32";
        return "linux";
    }

    private void decodeLogin(byte[] bytes) {
        ProtoReader reader = new ProtoReader(ByteBuffer.wrap(bytes));
        boolean hasColor = false;
        boolean flag = false;
        float red = 1.0F;
        float green = 1.0F;
        float blue = 1.0F;
        float alpha = 1.0F;
        for (int tag; (tag = reader.readTag()) != 0;) {
            if (tag >>> 3 == 1) {
                ProtoReader logo = new ProtoReader(ByteBuffer.wrap(reader.readBytes()));
                for (int logoTag; (logoTag = logo.readTag()) != 0;) {
                    if (logoTag >>> 3 == 1) {
                        int color = (int) logo.readEnum();
                        hasColor = true;
                        alpha = (color & 0xFF000000) != 0 ? ((color >>> 24) & 255) / 255.0F : 1.0F;
                        blue = (color & 255) / 255.0F;
                        green = ((color >>> 8) & 255) / 255.0F;
                        red = ((color >>> 16) & 255) / 255.0F;
                    } else {
                        logo.skipField(logoTag);
                    }
                }
            } else if (tag >>> 3 == 2) {
                flag = reader.readEnum() != 0;
            } else {
                reader.skipField(tag);
            }
        }
        if (hasColor) {
            UUID uuid = Minecraft.getMinecraft().getSession().getProfile().getId();
            cache.logos.put(uuid, new LunarTabLogo(uuid, red, green, blue, alpha));
        }
    }

    private static String architecture() {
        String arch = System.getProperty("os.arch", "").toLowerCase();
        return arch.contains("aarch64") || arch.contains("arm64") ? "arm64" : "x64";
    }

    void receive(byte[] bytes) {
        try {
            ProtoReader reader = new ProtoReader(ByteBuffer.wrap(bytes));
            for (int tag; (tag = reader.readTag()) != 0;) {
                if (tag >>> 3 != 1) {
                    reader.skipField(tag);
                    continue;
                }
                ProtoReader reply = new ProtoReader(ByteBuffer.wrap(reader.readBytes()));
                String requestId = null;
                byte[] payload = null;
                for (int replyTag; (replyTag = reply.readTag()) != 0;) {
                    if (replyTag >>> 3 == 1) requestId = reply.readString();
                    else if (replyTag >>> 3 == 2) payload = reply.readBytes();
                    else reply.skipField(replyTag);
                }
                if (payload == null) continue;
                if (requestId != null && pendingLoginIds.remove(requestId)) {
                    decodeLogin(payload);
                } else {
                    decodeTabLogos(payload);
                }
            }
        } catch (Exception error) {
            Logger.log(LogLevel.WARN, true, "Failed to parse WebSocket frame: {}", describeError(error));
        }
    }
}
