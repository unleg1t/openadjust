package com.adjustclient.modules.impl.player.voicechat.client;

import com.adjustclient.modules.impl.player.voicechat.client.session.VoiceSessionListener;
import com.adjustclient.modules.impl.player.voicechat.protocol.property.PropertyContainer;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ServerboundVoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.VoicePacketListener;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.ServerboundDisconnectPacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.ServerboundPropertiesPacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.ServerboundUserInfoRequestPacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.transport.VoiceUdpClient;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.ConnectionState;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.PacketEncryption;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.VoiceServer;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class VoiceChatClient implements VoicePacketListener {
    @FunctionalInterface public interface PacketFactory {
        ServerboundVoicePacket create(VoiceServer server, byte[] secret, Object field3, String response);
    }
    public static PacketFactory loginPacketFactory = (server, secret, field3, response) -> {
        var packet = new com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.ServerboundStringPacket();
        packet.mode = server;
        packet.data = secret;
        packet.field24 = field3 instanceof UUID ? (UUID) field3 : null;
        packet.value = response;
        return packet;
    };
    public final AtomicBoolean connecting = new AtomicBoolean();
    public final java.util.EnumMap<VoiceServer, VoiceAuthenticator> authenticators = new java.util.EnumMap<>(VoiceServer.class);
    public VoiceUdpClient udpClient;
    public VoiceSessionListener listener = new com.adjustclient.modules.impl.player.voicechat.client.session.NoOpVoiceSessionListener();
    public int requestId;
    public int timeout = 20000;

    public VoiceChatClient() {}

    public void requestUserInfo(UUID[] userIds) {
        ServerboundUserInfoRequestPacket packet = new ServerboundUserInfoRequestPacket(requestId);
        try {
            for (UUID userId : userIds) {
                packet.userIds.add(userId);
                if (packet.userIds.size() >= 50) {
                    udpClient.sendPacket(packet, udpClient.connection, null);
                    packet = new ServerboundUserInfoRequestPacket(requestId);
                }
            }
            if (!packet.userIds.isEmpty()) {
                udpClient.sendPacket(packet, udpClient.connection, null);
            }
        } catch (Exception ignored) {
        }
        requestId = (requestId + 1) % 127;
    }

    public void sendProperties(UUID target, PropertyContainer properties) {
        try {
            udpClient.sendPacket(new ServerboundPropertiesPacket(target, properties), udpClient.connection, null);
        } catch (Exception ignored) {
        }
    }

    public void disconnect(String reason) {
        if (udpClient == null) return;
        try {
            udpClient.sendPacket(new ServerboundDisconnectPacket(reason), udpClient.connection, null);
        } catch (Exception ignored) {
            // Failed disconnect notification must not prevent closing the connection.
        }
        udpClient.setState(ConnectionState.DISCONNECT);
    }

    public void connect(java.net.InetSocketAddress address, VoiceServer server) {
        if (connecting.getAndSet(true)) return;
        try {
            if (udpClient != null && udpClient.open.get()) {
                udpClient.setState(ConnectionState.DISCONNECT);
            }
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://dl.labymod.net/labyconnect/voice/key.pub"))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36")
                    .timeout(java.time.Duration.ofSeconds(5)).GET().build();
            HttpResponse<String> httpResponse;
            try (HttpClient http = HttpClient.newHttpClient()) {
                httpResponse = http.send(request, HttpResponse.BodyHandlers.ofString());
            }
            if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) return;
            udpClient = new VoiceUdpClient(this, address, httpResponse.body(), timeout);
            byte[] secret = udpClient.connection.symmetricCrypto
                    .secretKey.getEncoded();
            byte[] publicKey = udpClient.connection.asymmetricCrypto
                    .publicKey.getEncoded();
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update("laby".getBytes(StandardCharsets.ISO_8859_1));
            sha1.update(secret);
            sha1.update(publicKey);
            String hash = java.net.URLEncoder.encode(new java.math.BigInteger(sha1.digest()).toString(16), StandardCharsets.UTF_8);
            VoiceAuthenticator authenticator = authenticators.get(server);
            if (authenticator == null) throw new IllegalStateException("No authenticator for " + server + " defined!");
            try {
                AuthenticationResponse response = authenticator.request(hash);
                if (response == null) {
                    udpClient.setState(ConnectionState.AUTHENTICATION_FAILED);
                    return;
                }
                udpClient.start();
                if (loginPacketFactory != null) {
                    ServerboundVoicePacket login = loginPacketFactory.create(server, secret, null, response.string);
                    for (int i = 0; i < 5; i++) udpClient.sendPacket(login, udpClient.connection, null);
                }
            } catch (Exception exception) {
                Logger.log(LogLevel.ERROR, true, "Voice chat authentication failed", exception);
                udpClient.setState(ConnectionState.AUTHENTICATION_FAILED);
            }
            udpClient.stateListener = state -> {
                listener.onDisconnect(state, "Disconnected");
            };
        } catch (Exception exception) {
            Logger.log(LogLevel.ERROR, true, "Connecting to Voice Client {}", exception);
        } finally {
            connecting.set(false);
        }
    }
}
