package com.adjustclient.utilities.hypixel.game.zombies.entity.prediction;

import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.hypixel.game.zombies.entity.TrackedEntityRef;
import com.adjustclient.utilities.BoundedList;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.util.Vec3;

public final class PositionPredictor implements MinecraftInstance {
    public final TrackedEntityRef target;
    public final BoundedList<Vec3> positions = new BoundedList<>(40);
    public final BoundedList<Long> times = new BoundedList<>(40);
    public Vec3 velocity = new Vec3(0, 0, 0);
    public float yaw;
    public double x, y, z;
    public long lastPositionNanos;
    public long lastVelocityNanos;
    public float pitch;
    public float headYaw;
    public Object field72;

    public PositionPredictor(TrackedEntityRef target) {
        this.target = target;
        Entity entity = target.getEntity();
        if (entity != null) { sample(entity.posX, entity.posY, entity.posZ); yaw = entity.rotationYaw; pitch = entity.rotationPitch; }
    }
    public Vec3 averageVelocity(double seconds) {
        int size = positions.size();
        if (size < 2 || seconds == 0) return velocity;
        long end = times.get(size - 1);
        int index = size - 1;
        for (int i = index; ; ) {
            int previous = i - 1;
            if (seconds < (end - times.get(previous)) / 1.0E9) { index = i; break; }
            if (previous < 1) { index = previous; break; }
            i = previous;
        }
        double elapsed = (end - times.get(index)) / 1.0E9;
        if (elapsed <= 0) return velocity;
        Vec3 start = positions.get(index), finish = positions.get(size - 1);
        return new Vec3((finish.xCoord - start.xCoord) / elapsed,
            (finish.yCoord - start.yCoord) / elapsed, (finish.zCoord - start.zCoord) / elapsed);
    }
    public void onVelocity(S12PacketEntityVelocity packet) {
        velocity = new Vec3(packet.getMotionX() / 8000.0 * 20.0,
            packet.getMotionY() / 8000.0 * 20.0, packet.getMotionZ() / 8000.0 * 20.0);
        lastVelocityNanos = System.nanoTime();
    }
    public void onRelativeMove(S14PacketEntity packet) {
        Entity entity = target.getEntity();
        if (entity != null) sample(entity.posX, entity.posY, entity.posZ);
        if (packet.func_149060_h()) {
            yaw = packet.func_149066_f() * 360.0F / 256.0F;
            pitch = packet.func_149063_g() * 360.0F / 256.0F;
        }
    }
    public void onTeleport(S18PacketEntityTeleport packet) {
        positions.clear(); times.clear();
        sample(packet.getX() / 32.0, packet.getY() / 32.0, packet.getZ() / 32.0);
        yaw = packet.getYaw() * 360.0F / 256.0F;
        pitch = packet.getPitch() * 360.0F / 256.0F;
    }
    public void sample(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
        positions.add(new Vec3(x, y, z)); times.add(System.nanoTime());
        lastPositionNanos = times.get(times.size() - 1);
    }
    public PredictedPosition predict(double latencyScale, double historySeconds, double velocityBlendSeconds, double maximumSeconds) {
        long now = System.nanoTime();
        if (lastPositionNanos == 0 || now - lastPositionNanos >= 350000001L)
            return new PredictedPosition(new Vec3(x, y, z), 0.0F);
        latencyScale = Math.max(0, latencyScale); historySeconds = Math.max(0, historySeconds);
        velocityBlendSeconds = Math.max(0, velocityBlendSeconds); maximumSeconds = Math.max(0, maximumSeconds);
        Vec3 motion = averageVelocity(historySeconds);
        if (lastVelocityNanos != 0 && velocityBlendSeconds > 0) {
            double age = (now - lastVelocityNanos) / 1.0E9;
            if (velocityBlendSeconds > age) {
                double blend = 1.0 - age / velocityBlendSeconds;
                motion = new Vec3(motion.xCoord * (1 - blend) + velocity.xCoord * blend,
                    motion.yCoord * (1 - blend) + velocity.yCoord * blend,
                    motion.zCoord * (1 - blend) + velocity.zCoord * blend);
            }
        }
        double latency = 0;
        if (ServerTracker.INSTANCE.serverState != null) {
            var watcher = ServerTracker.INSTANCE.serverState.packetWatcher;
            if (watcher.delayCount != 0) latency = (watcher.totalDelay / watcher.delayCount) / 1000.0;
        }
        double age = (now - lastPositionNanos) / 1.0E9;
        double extrapolation = Math.min(latency * latencyScale + age, maximumSeconds);
        double dx = motion.xCoord * extrapolation, dy = motion.yCoord * extrapolation, dz = motion.zCoord * extrapolation;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal > 3.0) { dx *= 3.0 / horizontal; dy *= 3.0 / horizontal; dz *= 3.0 / horizontal; }
        dy = Math.max(-1.5, Math.min(1.5, dy));
        float accuracy = positions.size() < 2 ? 0.25F : (float)Math.max(0.0, Math.min(1.0, 1.0 - age / 0.35));
        return new PredictedPosition(new Vec3(x + dx, y + dy, z + dz), accuracy);
    }
}
