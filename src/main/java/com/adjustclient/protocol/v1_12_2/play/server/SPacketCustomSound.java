package com.adjustclient.protocol.v1_12_2.play.server;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import java.lang.String;
import net.minecraft.client.audio.SoundCategory;

public class SPacketCustomSound implements ProtocolPacket<PlayClientHandler> {
    private String soundName;
    private SoundCategory category;
    private int x;
    private int y;
    private int z;
    private float volume;
    private float pitch;

    public SPacketCustomSound() {
    }

    @Override
    public void readPacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void writePacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void processPacket(PlayClientHandler handler) {
    }
}
