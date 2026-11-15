package com.adjustclient.service.payload;

import com.adjustclient.Adjust;
import com.adjustclient.module.impl.combat.antibot.AntiBotModule;
import com.adjustclient.service.party.ui.PartyPanel;
import com.adjustclient.user.ClientUser;
import com.adjustclient.util.MinecraftInstance;
import net.minecraft.network.PacketBuffer;

public final class PartyPingPayload implements MinecraftInstance, PayloadHandler {
    public int dimension;
    public double x;
    public double y;
    public double z;
    public int entityId = -1;
    public Object field36;

    public boolean handle(ClientUser sender) {
        PartyPanel party = Adjust.INSTANCE.screenStack.partyService.party;
        if (party == null) return false;
        boolean member = false;
        for (var entry : party.members) {
            if (entry.user == sender) { member = true; break; }
        }
        if (!member || mc.thePlayer == null || mc.thePlayer.dimension != dimension) return false;
        if (entityId == -1) {
            party.receiveBlockPing(sender, x, y, z);
        } else {
            if (AntiBotModule.isBot(entityId)) return false;
            party.receiveEntityPing(entityId, sender, x, y, z);
        }
        return true;
    }

    public void write(PacketBuffer buffer) {
        buffer.writeVarIntToBuffer(dimension);
        buffer.writeVarIntToBuffer(entityId);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }

    public void read(PacketBuffer buffer) {
        dimension = buffer.readVarIntFromBuffer();
        entityId = buffer.readVarIntFromBuffer();
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
    }
}
