package com.adjustclient.utilities.protocol.v1_12_2.play.server;

import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.utilities.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import net.minecraft.network.play.server.S42PacketCombatEvent.Event;
import net.minecraft.util.IChatComponent;

public class SPacketCombatEvent implements ProtocolPacket<PlayClientHandler> {
    public Event eventType;
    public int playerId;
    public int entityId;
    public int duration;
    public IChatComponent deathMessage;

    public SPacketCombatEvent() {
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
