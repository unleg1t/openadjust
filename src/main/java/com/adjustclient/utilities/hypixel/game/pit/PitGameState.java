package com.adjustclient.utilities.hypixel.game.pit;

import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.hypixel.HypixelGameState;
import com.adjustclient.utilities.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public final class PitGameState extends HypixelGameState {
    public final Timer spawnTimer = new Timer();
    public Integer spawnHeight;
    public int spawnMarkerId;
    public boolean inSpawn = true;
    public boolean nearSpawn;
    public Object field60;

    public PitGameState() { super(HypixelGame.THE_HYPIXEL_PIT); }

    public void onPlayerMove(C03PacketPlayer packet) {
        if (!inSpawn || packet == null || !packet.isMoving()) return;
        if (spawnHeight == null) findSpawn();
        if (spawnHeight != null && spawnTimer.hasElapsed(1200L) && packet.getPositionY() <= spawnHeight - 7.0) {
            inSpawn = false;
        }
        if (inSpawn) {
            Entity marker = mc.theWorld.getEntityByID(spawnMarkerId);
            nearSpawn = marker != null && mc.thePlayer.getDistanceSqToEntity(marker) <= 4096.0;
        } else {
            nearSpawn = false;
        }
    }

    @Override public void onPacket(Packet<?> packet, boolean before) {
        if (before && packet instanceof S08PacketPlayerPosLook) {
            S08PacketPlayerPosLook position = (S08PacketPlayerPosLook) packet;
            // Relative-Y corrections are ignored entirely, including superclass dispatch.
            if (position.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y)) return;
            if (spawnHeight == null) findSpawn();
            if (spawnHeight != null && position.getY() >= spawnHeight - 5.0 && position.getY() <= spawnHeight + 4.5) {
                nearSpawn = true;
                inSpawn = true;
                spawnTimer.reset();
            }
        }
        super.onPacket(packet, before);
    }

    @Override public int isSpecialState() { return nearSpawn || !locationTimer.hasElapsed(1500L) ? 1 : 0; }

    public void findSpawn() {
        if (mc.theWorld == null || mc.theWorld.loadedEntityList == null) return;
        try {
            for (Entity marker : mc.theWorld.loadedEntityList) {
                if (!(marker instanceof EntityArmorStand) || !marker.getName().startsWith("§2§l")) continue;
                for (Entity player : mc.theWorld.loadedEntityList) {
                    if (!(player instanceof EntityPlayer)) continue;
                    if (player.posX == marker.posX && player.posZ == marker.posZ && player.posY - marker.posY == -0.28125) {
                        spawnMarkerId = marker.getEntityId();
                        spawnHeight = (int) marker.posY - 1;
                        return;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
