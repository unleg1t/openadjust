package com.adjustclient.hypixel.game.hideandseek;

import com.adjustclient.Adjust;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.hypixel.HypixelGameState;
import com.adjustclient.hypixel.game.LocrawResponse;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.Timer;
import com.adjustclient.util.anticheat.ServerTracker;
import it.unimi.dsi.fastutil.ints.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.scoreboard.ScorePlayerTeam;

public final class HideAndSeekGameState extends HypixelGameState {
    public HideAndSeekMode mode = HideAndSeekMode.UNKNOWN;
    public HideAndSeekRole role;
    public final Int2ObjectOpenHashMap<Timer> movementTimers = new Int2ObjectOpenHashMap<>();
    public final IntOpenHashSet movedStands = new IntOpenHashSet();
    public final IntOpenHashSet preGamePlayers = new IntOpenHashSet();
    public Object field64;
    public HideAndSeekGameState() { super(HypixelGame.HIDE_AND_SEEK); }
    public void acceptLocation(LocrawResponse location) {
        if ("HIDE_AND_SEEK_PROP_HUNT".equals(location.mode)) mode = HideAndSeekMode.PROP_HUNT;
        else if ("HIDE_AND_SEEK_PARTY_POOPER".equals(location.mode)) mode = HideAndSeekMode.PARTY_POOPER;
    }
    @Override public void onPacket(Packet<?> packet, boolean before) {
        super.onPacket(packet, before);
        if (before) {
            if (packet instanceof S0CPacketSpawnPlayer && startTimer == null)
                preGamePlayers.add(((S0CPacketSpawnPlayer) packet).getEntityID());
        } else {
            int id;
            if (packet instanceof S18PacketEntityTeleport) id = ((S18PacketEntityTeleport) packet).getEntityId();
            else if (packet instanceof S14PacketEntity.S15PacketEntityRelMove || packet instanceof S14PacketEntity.S17PacketEntityLookMove) {
                Entity entity = ((S14PacketEntity) packet).getEntity(mc.theWorld);
                if (!(entity instanceof EntityArmorStand)) return;
                id = entity.getEntityId();
            } else return;
            if (id != mc.thePlayer.getEntityId()
                    && mc.theWorld.getEntityByID(id) instanceof EntityArmorStand) movedStands.add(id);
        }
    }
    @Override public HypixelGameState locate() {
        CompletableFuture<LocrawResponse> future;
        if (locraw != null) future = CompletableFuture.completedFuture(locraw);
        else if (locrawFuture != null) future = locrawFuture;
        else {
            if (ServerTracker.INSTANCE.serverState == null
                    || ServerTracker.INSTANCE.serverState.antiCheatModel == null
                    || ServerTracker.INSTANCE.serverState.antiCheatModel.serverType != ServerType.HYPIXEL)
                throw new IllegalStateException("Unable to locate because we are not on Hypixel!");
            Adjust.chatMessageSender.sendMessage("/locraw", false);
            future = locrawFuture = new CompletableFuture<>();
        }
        future.thenAccept(location -> acceptLocation(location));
        return super.locate();
    }
    @Override public void onUpdate() {
        IntIterator iterator = movedStands.iterator();
        while (iterator.hasNext()) {
            int id = iterator.nextInt();
            Entity entity = mc.theWorld.getEntityByID(id);
            if (entity instanceof EntityArmorStand && (entity.posX != entity.prevPosX || entity.posY != entity.prevPosY
                    || entity.posZ != entity.prevPosZ || entity.rotationYaw != entity.prevRotationYaw
                    || entity.rotationPitch != entity.prevRotationPitch)) movementTimers.put(id, new Timer());
            iterator.remove();
        }
        ScorePlayerTeam team = mc.thePlayer.worldObj.getScoreboard().getPlayersTeam(mc.thePlayer.getName());
        if (team == null) return;
        if ("HIDER".equals(team.getRegisteredName())) role = HideAndSeekRole.HIDER;
        else if ("SEEKER".equals(team.getRegisteredName())) role = HideAndSeekRole.SEEKER;
        else role = null;
    }
}
