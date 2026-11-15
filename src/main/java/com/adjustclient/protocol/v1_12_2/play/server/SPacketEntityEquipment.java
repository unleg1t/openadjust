package com.adjustclient.protocol.v1_12_2.play.server;

import com.adjustclient.protocol.type.EntityEquipmentSlot;
import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import net.minecraft.item.ItemStack;

public class SPacketEntityEquipment implements ProtocolPacket<PlayClientHandler> {
    private int entityId;
    private EntityEquipmentSlot equipmentSlot;
    private ItemStack itemStack;

    public SPacketEntityEquipment() {
        this.itemStack = null;
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
