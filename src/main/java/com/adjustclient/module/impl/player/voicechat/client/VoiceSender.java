package com.adjustclient.module.impl.player.voicechat.client;

import com.adjustclient.module.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.module.impl.player.voicechat.playback.AudioStreamManager;
import com.adjustclient.module.impl.player.voicechat.playback.PlayerAudioStream;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.serverbound.ServerboundAudioPacket;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.serverbound.ServerboundServerAddressPacket;
import java.net.IDN;
import net.minecraft.client.multiplayer.ServerAddress;

public final class VoiceSender {
    public final VoiceChatClient client;
    public AudioStreamManager audioStreams;

    public VoiceSender(VoiceChatClient client) { this.client = client; }

    public void sendServerAddress(ServerAddress address) {
        try {
            client.udpClient.sendPacket(new ServerboundServerAddressPacket(IDN.toASCII(address.getIP()), address.getPort()),
                    client.udpClient.connection, null);
        } catch (Exception ignored) {
        }
    }

    public void sendAudio(byte[] data) {
        int length = data.length;
        try {
            client.udpClient.sendPacket(new ServerboundAudioPacket(data), client.udpClient.connection, null);
        } catch (Exception ignored) {
            // Transmission failures do not suppress stream send-statistics updates.
        }
        if (audioStreams == null) audioStreams = VoiceChatModule.INSTANCE.getAudioStreamManager();
        for (PlayerAudioStream stream : audioStreams.streams.values()) {
            stream.field40 += length;
            ++stream.field56;
        }
    }
}
