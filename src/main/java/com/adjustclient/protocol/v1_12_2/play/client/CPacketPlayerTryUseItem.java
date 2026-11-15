package com.adjustclient.protocol.v1_12_2.play.client;

import com.adjustclient.protocol.type.EnumHand;
import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayServerHandler;
import java.io.IOException;

public class CPacketPlayerTryUseItem implements ProtocolPacket<PlayServerHandler> {
    private EnumHand hand;

    public CPacketPlayerTryUseItem() {
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
