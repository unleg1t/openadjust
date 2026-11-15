package com.adjustclient.protocol.v1_12_2.play.client;

import com.adjustclient.protocol.type.EnumHand;
import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayServerHandler;
import java.io.IOException;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.util.Vec3;

public class CPacketUseEntity implements ProtocolPacket<PlayServerHandler> {
    private int entityId;
    private Action action;
    private Vec3 hitVec;
    private EnumHand hand;

    public CPacketUseEntity() {
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
