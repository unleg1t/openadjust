package com.adjustclient.protocol.v1_12_2.play.client;

import com.adjustclient.protocol.type.EnumHand;
import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayServerHandler;
import java.io.IOException;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class CPacketPlayerTryUseItemOnBlock implements ProtocolPacket<PlayServerHandler> {
    private BlockPos position;
    private EnumFacing placedBlockDirection;
    private EnumHand hand;
    private float facingX;
    private float facingY;
    private float facingZ;

    public CPacketPlayerTryUseItemOnBlock() {
    }

    @Override
    public void readPacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void writePacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void processPacket(PlayServerHandler handler) {
    }
}
