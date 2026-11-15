package com.adjustclient.protocol.v1_12_2.play.server;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import java.util.List;
import net.minecraft.util.BlockPos;

public class SPacketExplosion implements ProtocolPacket<PlayClientHandler> {
    private double posX;
    private double posY;
    private double posZ;
    private float strength;
    private List<BlockPos> affectedBlockPositions;
    public float motionX;
    public float motionY;
    public float motionZ;

    public SPacketExplosion() {
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
