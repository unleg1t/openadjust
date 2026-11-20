package com.adjustclient.utilities.lunar.apollo;

import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ProtoAny;
import com.adjustclient.utilities.lunar.apollo.protocol.configurable.OverrideConfigurableSettingsMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.transfer.*;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.utilities.proto.ProtoReader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

public final class ApolloMessageHandler {
    public static void handleProtobuf(byte[] payload) {
        try {
            ProtoAny envelope = ProtoAny.parse(new ProtoReader(ByteBuffer.wrap(payload)));
            if (envelope.is(OverrideConfigurableSettingsMessage.class)) {
                OverrideConfigurableSettingsMessage settings = envelope.unpack(OverrideConfigurableSettingsMessage.class);
                int count = settings.settings.size();
                if (Logger.debug) Logger.log(LogLevel.INFO, true, "Server configures {} Apollo module(s)", new Object[]{count});
            } else if (envelope.is(ApolloTransferRequest.class)) {
                ApolloTransferRequest transfer = envelope.unpack(ApolloTransferRequest.class);
                byte[] id = transfer.getRequestId();
                if (id == null) return;
                id = id.clone();
                String server = transfer.getServer();
                Logger.log(LogLevel.INFO, true, "Server requested transfer to {}", new Object[]{server});
                sendTransferResponse(id);
                NotificationBuilder notification = new NotificationBuilder();
                notification.title = "Apollo";
                notification.parts = new ArrayList<>();
                notification.actions = new ArrayList<>();
                notification.clearable = true;
                notification.addText("Server requested transfer to ", 0xFFBEBCBD)
                        .append(server).addText(".", 0xFFBEBCBD).show();
            } else if (envelope.is(ApolloPingRequest.class)) {
                ApolloPingRequest ping = envelope.unpack(ApolloPingRequest.class);
                byte[] id = ping.getRequestId();
                if (id != null) sendPingResponse(id.clone());
            }
        } catch (Exception error) {
            Logger.log(LogLevel.WARN, true, "Failed to parse Apollo protobuf message: {}", new Object[]{error.getMessage()});
        }
    }

    public static void handleJson(byte[] payload) {
        try {
            JsonObject message = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
            String type = message.has("@type") ? message.get("@type").getAsString() : "unknown";
            if (type.contains("OverrideConfigurableSettingsMessage") && Logger.debug)
                Logger.log(LogLevel.INFO, true, "Server sent Apollo module configuration", new Object[0]);
        } catch (Exception error) {
            Logger.log(LogLevel.WARN, true, "Failed to parse Apollo protobuf message: {}", new Object[]{error.getMessage()});
        }
    }

    public static void sendPingResponse(byte[] requestId) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.thePlayer == null || minecraft.thePlayer.sendQueue == null) return;
        NetworkManager connection = minecraft.thePlayer.sendQueue.getNetworkManager();
        if (connection == null || !connection.isChannelOpen()) return;
        ApolloPingResponse response = new ApolloPingResponse(requestId.clone(), new ArrayList<>());
        ProtoAny envelope = ProtoAny.pack(response);
        PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
        payload.writeBytes(envelope.toByteArray());
        connection.sendPacket(new C17PacketCustomPayload("lunar:apollo", payload));
    }

    public static void sendTransferResponse(byte[] requestId) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.thePlayer == null || minecraft.thePlayer.sendQueue == null) return;
        NetworkManager connection = minecraft.thePlayer.sendQueue.getNetworkManager();
        if (connection == null || !connection.isChannelOpen()) return;
        ApolloTransferResponse response = new ApolloTransferResponse(requestId.clone(), ApolloTransferStatus.STATUS_REJECTED);
        ProtoAny envelope = ProtoAny.pack(response);
        PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
        payload.writeBytes(envelope.toByteArray());
        connection.sendPacket(new C17PacketCustomPayload("lunar:apollo", payload));
    }
}
