package com.adjustclient.utilities.lunar.apollo;

import com.adjustclient.utilities.lunar.LunarClientService;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.utilities.lunar.apollo.protocol.common.*;
import com.adjustclient.utilities.lunar.apollo.protocol.player.*;
import com.adjustclient.managers.session.SessionService;
import com.adjustclient.utilities.proto.ProtoWriter;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

public final class LunarHandshakeSender {
    private static NetworkManager lastConnection;

    public static void sendInitialHandshake() {
        NetworkManager connection = Minecraft.getMinecraft().thePlayer.sendQueue.getNetworkManager();
        if (connection == lastConnection) return;
        String brand = LunarClientService.INSTANCE.brand;
        if (brand == null) return;
        lastConnection = connection;
        PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
        payload.writeString(brand);
        connection.sendPacket(new C17PacketCustomPayload("MC|Brand", payload));
        registerChannels(connection);
        sendApolloHandshake(connection);
    }

    public static void registerChannels(NetworkManager connection) {
        String channels = String.join("\u0000", "lunar:apollo", "apollo:json", "lunarclient:pm",
                "badlion:timers", "transfer:channel");
        PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
        payload.writeBytes(channels.getBytes(StandardCharsets.UTF_8));
        connection.sendPacket(new C17PacketCustomPayload("REGISTER", payload));
    }

    public static void sendApolloHandshake(NetworkManager connection) {
        String version = LunarClientService.INSTANCE.version;
        String commit = LunarClientService.INSTANCE.gitCommit;
        if (version == null || commit == null) {
            Logger.log(LogLevel.WARN, true, "Lunar version not yet available, skipping Apollo handshake", new Object[0]);
            return;
        }
        if (!version.startsWith("v")) version = "v" + version;
        ApolloMinecraftVersion minecraft = new ApolloMinecraftVersion("v1_8");
        ApolloLunarClientVersion lunar = new ApolloLunarClientVersion("master", commit, version);
        PlayerHandshakeMessage handshake = new PlayerHandshakeMessage(minecraft, lunar, new ArrayList<>(),
                ApolloEmbeddedCheckoutSupport.EMBEDDED_CHECKOUT_SUPPORT_WINDOW, new LinkedHashMap<>());
        UUID uuid = SessionService.resolveSessionUuid(Minecraft.getMinecraft().getSession());
        LunarModStatusBuilder.populateModStatus(handshake, uuid != null ? uuid : new UUID(0L, 0L));
        // Registry-backed packing uses type.googleapis.com/lunarclient.apollo.player.v1.PlayerHandshakeMessage.
        ProtoAny envelope = ProtoAny.pack(handshake);
        ProtoWriter writer = new ProtoWriter();
        envelope.writeTo(writer);
        PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
        payload.writeBytes(writer.toByteArray());
        connection.sendPacket(new C17PacketCustomPayload("lunar:apollo", payload));
        if (Logger.debug) Logger.log(LogLevel.INFO, true, "Sent Apollo player handshake", new Object[0]);
    }
}
