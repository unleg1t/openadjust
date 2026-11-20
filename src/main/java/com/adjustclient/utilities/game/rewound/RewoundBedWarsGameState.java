package com.adjustclient.utilities.game.rewound;

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public final class RewoundBedWarsGameState extends RewoundGameState {
    public Vec3 spawnPosition;

    public RewoundBedWarsGameState() {
        super(RewoundGame.BEDWARS);
    }

    public boolean isNearSpawn(BlockPos pos) {
        return spawnPosition != null
                && pos.distanceSqToCenter(spawnPosition.xCoord, spawnPosition.yCoord, spawnPosition.zCoord) <= 512.0D;
    }

    @Override
    public void onPacket(Packet<?> packet, boolean before) {
        if (before && packet instanceof S08PacketPlayerPosLook position && Boolean.FALSE.equals(lobby)) {
            detectSpawn(position.getX(), position.getY(), position.getZ());
        }
        super.onPacket(packet, before);
    }

    private void detectSpawn(double x, double y, double z) {
        if (spawnPosition != null || startTimer == null || mc.thePlayer == null) return;
        boolean validWindow = rejoined
                ? (startTimer.hasElapsed(3500L) && !startTimer.hasElapsed(7500L))
                : !startTimer.hasElapsed(3000L);
        if (!validWindow) return;
        if (x % 0.5D != 0.0D || z % 0.5D != 0.0D) return;
        if (mc.thePlayer.getDistanceSq(x, y, z) > 1024.0D) return;
        spawnPosition = new Vec3(x, y, z);
    }
}
