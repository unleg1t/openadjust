package com.adjustclient.game.entity;

import com.adjustclient.Adjust;
import com.adjustclient.game.entity.tracker.ServerPositionTracker;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.anticheat.ServerTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import java.util.Objects;

public final class TrackedPacketFilter {
    public Object field4;

    public boolean shouldDelay(Packet<?> packet) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return true;
        ServerPositionTracker tracker = Adjust.entityTrackerService.tracker;
        Objects.requireNonNull(packet);
        if (packet instanceof S19PacketEntityStatus p) {
            byte status = p.getOpCode();
            if (status != 2 && status != 3) return true;
            return !tracker.entries.containsKey(p.entityId);
        }
        if (packet instanceof S0BPacketAnimation p) return !tracker.entries.containsKey(p.getEntityID());
        if (packet instanceof S1CPacketEntityMetadata p) {
            int id = p.getEntityId();
            if (ServerTracker.INSTANCE.serverState != null
                    && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                    && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL
                    && id == -1234) return false;
            if (id == mc.thePlayer.getEntityId()) return true;
            if (tracker.entries.containsKey(id)) return false;
            Entity entity = mc.theWorld.getEntityByID(id);
            return entity instanceof EntityLivingBase;
        }
        if (packet instanceof S04PacketEntityEquipment p) return !tracker.entries.containsKey(p.getEntityID());
        if (packet instanceof S25PacketBlockBreakAnim p) return p.getBreakerId() == mc.thePlayer.getEntityId();
        if (packet instanceof S13PacketDestroyEntities p) {
            if (ServerTracker.INSTANCE.serverState != null
                    && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                    && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL) {
                for (int id : p.getEntityIDs()) if (id == -1234) return false;
            }
            return true;
        }
        return !(packet instanceof S03PacketTimeUpdate)
                && !(packet instanceof S02PacketChat)
                && !(packet instanceof S29PacketSoundEffect)
                && !(packet instanceof S45PacketTitle)
                && !(packet instanceof S06PacketUpdateHealth)
                && !(packet instanceof S0EPacketSpawnObject);
    }
}
