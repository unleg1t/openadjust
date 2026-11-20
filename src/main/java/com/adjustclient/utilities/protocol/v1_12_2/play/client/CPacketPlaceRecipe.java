package com.adjustclient.utilities.protocol.v1_12_2.play.client;

import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.utilities.protocol.v1_12_2.play.PlayServerHandler;
import java.io.IOException;
import net.minecraft.item.crafting.IRecipe;

public class CPacketPlaceRecipe implements ProtocolPacket<PlayServerHandler> {
    private int windowId;
    private IRecipe recipe;
    private boolean placeAll;

    public CPacketPlaceRecipe() {
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
