package com.adjustclient.modules.impl.player.voicechat.protocol.packet;

import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.clientbound.*;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.PacketEncryption;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.VoiceProtocolEnumB;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.VoiceProtocolEnumH;
import java.util.Map;
import java.util.concurrent.Callable;

public final class VoicePacketRegistry {
    public static final Map<Class<?>, Byte> PACKET_IDS = new java.util.HashMap<>();
    static {
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.UnusedServerboundPacketP.class, (byte) 28);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.ServerboundAudioPacket.class, (byte) 8);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.UnusedServerboundPacketG.class, (byte) 32);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.ServerboundPropertiesPacket.class, (byte) 30);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.UnusedServerboundPacketR.class, (byte) 33);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.ServerboundStringPacket.class, (byte) 0);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.ServerboundUserInfoRequestPacket.class, (byte) 7);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.ServerboundDisconnectPacket.class, (byte) 42);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.UnusedServerboundPacketL.class, (byte) 43);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.ServerboundErrorReportPacket.class, (byte) 44);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.ServerboundKeepAlivePacket.class, (byte) 3);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.UnusedServerboundPacketT.class, (byte) 26);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.UnusedServerboundPacketN.class, (byte) 23);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.UnusedServerboundPacketB.class, (byte) 29);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.UnusedServerboundPacketC.class, (byte) 31);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.UnusedServerboundPacketH.class, (byte) 6);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.ServerboundServerAddressPacket.class, (byte) 12);
        PACKET_IDS.put(com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound.UnusedServerboundPacketI.class, (byte) 25);
    }

    public static int getId(VoicePacket packet) {
        Byte id = PACKET_IDS.get(packet.getClass());
        if (id == null) {
            Logger.log(LogLevel.WARN, true, "Invalid packet {}", packet.getClass().getSimpleName());
            return -1;
        }
        return id.byteValue();
    }

    public static final Callable<ClientboundVoicePacket> CREATE_USER_INFO = () -> new ClientboundUserInfoPacket();
    public static final Callable<ClientboundVoicePacket> CREATE_UPDATE_CHANNEL = () -> new ClientboundUpdateChannelPacket();
    public static final Callable<ClientboundVoicePacket> CREATE_USER_STATE = () -> new ClientboundUserStatePacket();
    public static final Callable<ClientboundVoicePacket> CREATE_AUDIO = () -> {
        ClientboundAudioPacket packet = new ClientboundAudioPacket();
        packet.reliability = VoiceProtocolEnumH.UDP;
        return packet;
    };
    public static final Callable<ClientboundVoicePacket> CREATE_MODERATOR_WARNING = () -> new ClientboundModeratorWarningPacket();
    public static final Callable<ClientboundVoicePacket> CREATE_LOGIN_RESULT = () -> {
        ClientboundLoginResultPacket packet = new ClientboundLoginResultPacket();
        packet.phase = VoiceProtocolEnumB.HANDSHAKE;
        return packet;
    };
    public static final Callable<ClientboundVoicePacket> CREATE_INVALID_ENCRYPTION = () -> {
        ClientboundInvalidEncryptionPacket packet = new ClientboundInvalidEncryptionPacket();
        packet.encryption = PacketEncryption.NONE;
        packet.phase = VoiceProtocolEnumB.HANDSHAKE;
        return packet;
    };
    public static final Callable<ClientboundVoicePacket> CREATE_UPDATE_USER = () -> new ClientboundUpdateUserPacket();
    public static final Callable<ClientboundVoicePacket> CREATE_ADD_USERS = () -> new ClientboundAddUsersPacket();
    public static final Callable<ClientboundVoicePacket> CREATE_KEEP_ALIVE = () -> {
        ClientboundKeepAlivePacket packet = new ClientboundKeepAlivePacket();
        packet.reliability = VoiceProtocolEnumH.UDP;
        return packet;
    };
    public static final Callable<ClientboundVoicePacket> CREATE_STRING_LIST = () -> new ClientboundStringListPacket();
    public static final Callable<ClientboundVoicePacket> CREATE_HIDE_CHANNELS = () -> new ClientboundHideChannelsPacket();
    public static final Callable<ClientboundVoicePacket> CREATE_MOVE_USER = () -> new ClientboundMoveUserPacket();
    public static final Callable<ClientboundVoicePacket> CREATE_DISCONNECT = () -> new ClientboundDisconnectPacket();
    public static final Callable<ClientboundVoicePacket> CREATE_SHOW_CHANNELS = () -> new ClientboundShowChannelsPacket();
    public static final Callable<ClientboundVoicePacket> CREATE_USER_INFO_LIST = () -> new ClientboundUserInfoListPacket();
    public static final Callable<ClientboundVoicePacket> CREATE_EMPTY_L = () -> new ClientboundEmptyPacketL();
    public static final Callable<ClientboundVoicePacket> CREATE_RESET = () -> new ClientboundResetPacket();
    public static final Callable<ClientboundVoicePacket> CREATE_REMOVE_USERS = () -> new ClientboundRemoveUsersPacket();
    public static final Callable<ClientboundVoicePacket> CREATE_EMPTY_P = () -> new ClientboundEmptyPacketP();
    public static final Map<Byte, Callable<ClientboundVoicePacket>> INCOMING_PACKETS =
            new it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap<>();
    static {
        INCOMING_PACKETS.put((byte) 40, CREATE_REMOVE_USERS);
        INCOMING_PACKETS.put((byte) 41, CREATE_STRING_LIST);
        INCOMING_PACKETS.put((byte) 45, CREATE_RESET);
        INCOMING_PACKETS.put((byte) 5, CREATE_EMPTY_L);
        INCOMING_PACKETS.put((byte) 36, CREATE_HIDE_CHANNELS);
        INCOMING_PACKETS.put((byte) 38, CREATE_UPDATE_USER);
        INCOMING_PACKETS.put((byte) 24, CREATE_USER_INFO);
        INCOMING_PACKETS.put((byte) 34, CREATE_SHOW_CHANNELS);
        INCOMING_PACKETS.put((byte) 3, CREATE_KEEP_ALIVE);
        INCOMING_PACKETS.put((byte) 1, CREATE_LOGIN_RESULT);
        INCOMING_PACKETS.put((byte) 10, CREATE_USER_INFO_LIST);
        INCOMING_PACKETS.put((byte) 11, CREATE_USER_STATE);
        INCOMING_PACKETS.put((byte) 9, CREATE_AUDIO);
        INCOMING_PACKETS.put((byte) 14, CREATE_EMPTY_P);
        INCOMING_PACKETS.put((byte) 4, CREATE_DISCONNECT);
        INCOMING_PACKETS.put((byte) 27, CREATE_MODERATOR_WARNING);
        INCOMING_PACKETS.put((byte) 37, CREATE_ADD_USERS);
        INCOMING_PACKETS.put((byte) 2, CREATE_INVALID_ENCRYPTION);
        INCOMING_PACKETS.put((byte) 39, CREATE_MOVE_USER);
        INCOMING_PACKETS.put((byte) 35, CREATE_UPDATE_CHANNEL);
    }
}
