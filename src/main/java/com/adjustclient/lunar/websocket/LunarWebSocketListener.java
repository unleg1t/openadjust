package com.adjustclient.lunar.websocket;

import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import java.io.ByteArrayOutputStream;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

final class LunarWebSocketListener implements WebSocket.Listener {
    private final String jwt;
    private final CompletableFuture<Void> disconnected;
    private final ByteArrayOutputStream fragments = new ByteArrayOutputStream();
    private final LunarWebSocketClient client;
    private Object field20;
    private final long generation;

    LunarWebSocketListener(LunarWebSocketClient client, String jwt,
                           CompletableFuture<Void> disconnected, long generation) {
        this.client = client;
        this.jwt = jwt;
        this.disconnected = disconnected;
        this.generation = generation;
    }

    private void handshakeSent(WebSocket socket, WebSocket result, Throwable error) {
        if (error != null) {
            Logger.log(LogLevel.WARN, true, "WebSocket handshake send failed: {}", LunarWebSocketClient.describeError(error));
            client.connectionFailed(socket, disconnected, error);
            return;
        }
        if (!client.running || generation != client.generation || client.socket != socket) {
            socket.abort();
            disconnected.complete(null);
            return;
        }
        client.connected = true;
        client.startHeartbeat();
        try {
            if (client.reconnected != null) {
                client.reconnected.run();
            }
        } catch (Exception failure) {
            Logger.log(LogLevel.WARN, true, "WebSocket reconnect callback failed: {}", LunarWebSocketClient.describeError(failure));
        }
        try {
            String login = client.sendRpc(
                    "lunarclient.websocket.cosmetic.v2.CosmeticService", "Login", new byte[0]);
            client.pendingLoginIds.add(login);
        } catch (Exception failure) {
            Logger.log(LogLevel.WARN, true, "CosmeticService.Login failed: {}", LunarWebSocketClient.describeError(failure));
        }
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket socket, ByteBuffer data, boolean last) {
        byte[] bytes = new byte[data.remaining()];
        data.get(bytes);
        fragments.writeBytes(bytes);
        if (last) {
            byte[] message = fragments.toByteArray();
            fragments.reset();
            client.receive(message);
        }
        socket.request(1);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket socket, int statusCode, String reason) {
        if (client.socket == socket) {
            client.connected = false;
            if (client.heartbeat != null) {
                client.heartbeat.cancel(false);
                client.heartbeat = null;
            }
        }
        Logger.log(LogLevel.INFO, true, "Lunar WebSocket closed: {} {}", statusCode, reason);
        disconnected.complete(null);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onOpen(WebSocket socket) {
        if (!client.running || generation != client.generation) {
            socket.abort();
            disconnected.complete(null);
            return;
        }
        client.socket = socket;
        socket.request(1);
        try {
            byte[] hello = client.encodeHandshake(jwt);
            socket.sendBinary(ByteBuffer.wrap(hello), true).whenComplete(
                    (result, error) -> handshakeSent(socket, result, error));
        } catch (Exception error) {
            Logger.log(LogLevel.WARN, true, "WebSocket handshake encoding failed: {}", LunarWebSocketClient.describeError(error));
            client.connectionFailed(socket, disconnected, error);
        }
    }

    @Override
    public CompletionStage<?> onText(WebSocket socket, CharSequence text, boolean last) {
        socket.request(1);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onError(WebSocket socket, Throwable error) {
        Logger.log(LogLevel.WARN, true, "Lunar WebSocket error: {}", LunarWebSocketClient.describeError(error));
        client.connectionFailed(socket, disconnected, error);
    }
}
