package com.adjustclient.protocol.v1_12_2.play.server;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.play.server.S38PacketPlayerListItem.AddPlayerData;

public class SPacketPlayerListItem implements ProtocolPacket<PlayClientHandler> {
    private SPacketPlayerListItemAction action;
    private final List<AddPlayerData> players;

    public SPacketPlayerListItem() {
        this.players = new java.util.ArrayList<>();
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

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(SPacketPlayerListItem.class).add("action", (Object) null).add("entries", players).toString();
    }
}
