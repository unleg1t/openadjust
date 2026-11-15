package com.adjustclient.protocol.v1_12_2.play.client;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayServerHandler;
import java.io.IOException;
import net.minecraft.item.ItemStack;

public class CPacketCreativeInventoryAction implements ProtocolPacket<PlayServerHandler> {
    private int slotId;
    private ItemStack stack;

    public CPacketCreativeInventoryAction() {
        this.stack = null;
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
