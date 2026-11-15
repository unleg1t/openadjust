package com.adjustclient.protocol.v1_12_2.play.server;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import java.lang.Object;
import net.minecraft.client.audio.SoundCategory;

public class SPacketSoundEffect implements ProtocolPacket<PlayClientHandler> {
    private Object sound;
    private SoundCategory soundCategory;
    private int posX;
    private int posY;
    private int posZ;
    private float volume;
    private float pitch;

    public SPacketSoundEffect() {
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
