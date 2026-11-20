package com.adjustclient.utilities.lunar.websocket;

import com.adjustclient.utilities.proto.ProtoWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public final class LunarAuthenticator {
    private static final URI AUTH_URI;
    private static final HttpClient HTTP_CLIENT;

    static {
        AUTH_URI = URI.create("wss://authenticator.lunarclientprod.com/game");
        HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public static String authenticate() throws Exception {
        return fetchJwt();
    }

    static byte[] encodeHello(UUID uuid, String username) {
        ProtoWriter playerId = new ProtoWriter();
        playerId.writeFixed64(1, uuid.getMostSignificantBits());
        playerId.writeFixed64(2, uuid.getLeastSignificantBits());
        ProtoWriter identity = new ProtoWriter();
        identity.writeBytes(1, playerId.toByteArray());
        identity.writeString(2, username);
        ProtoWriter hello = new ProtoWriter();
        hello.writeBytes(1, identity.toByteArray());
        hello.writeString(2, "assetserver");
        return hello.toByteArray();
    }

    static String fetchJwt() throws Exception {
        Session session = Minecraft.getMinecraft().getSession();
        LunarAuthListener listener = new LunarAuthListener(session);
        java.util.concurrent.CompletableFuture<WebSocket> opening = HTTP_CLIENT.newWebSocketBuilder()
                .header("Accept", "application/x-protobuf")
                .header("X-Initiator", "assetServer")
                .connectTimeout(Duration.ofSeconds(10))
                .buildAsync(AUTH_URI, listener);
        WebSocket socket = null;
        try {
            socket = opening.get();
            return listener.jwt.get(15, TimeUnit.SECONDS);
        } catch (ExecutionException error) {
            if (error.getCause() instanceof Exception) {
                throw (Exception) error.getCause();
            }
            throw error;
        } finally {
            if (socket != null) {
                socket.abort();
            } else {
                opening.cancel(true);
            }
        }
    }

    public static Runnable authenticationTask(java.util.function.Consumer<String> success,
                                              java.util.function.Consumer<Throwable> failure) {
        return () -> {
            try {
                success.accept(authenticate());
            } catch (Exception error) {
                failure.accept(error);
            }
        };
    }
}
