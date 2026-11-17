package com.adjustclient.game.rewound;

import com.adjustclient.Adjust;
import com.adjustclient.event.DirectionalPacketEvent;
import com.adjustclient.event.EventBus;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.GameLoopEvent;
import com.adjustclient.event.Listener;
import com.adjustclient.event.LowLevelPacketSendEvent;
import com.adjustclient.event.RewoundDisablerEvent;
import com.adjustclient.game.render.progress.ProgressBar;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.Timer;
import com.adjustclient.util.anticheat.ServerTracker;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.Vec3;

public final class RewoundDisablerTracker implements Listener, MinecraftInstance {
    public Timer activeTimer = new Timer(0L);
    public RewoundDisablerType lastType;
    public Timer pearlThrowTimer = new Timer(0L);
    public final CopyOnWriteArrayList<Timer> sentPackets = new CopyOnWriteArrayList<>();
    public boolean pearlSpawned;

    public boolean isActive() {
        return activeTimer != null && !activeTimer.hasElapsed(3000L);
    }

    public boolean canUseTimer() {
        return sentPackets.size() < 800;
    }

    public void trigger(RewoundDisablerType type) {
        activeTimer.reset();
        lastType = type;
        EventBus.post(new RewoundDisablerEvent(type));
    }

    public void renderProgress() {
        long elapsed = System.currentTimeMillis() - activeTimer.lastMs;
        if (elapsed < 3001L) {
            float progress = (float) (int) elapsed / 3000.0F;
            float clamped = Math.max(0.0F, Math.min(1.0F, progress));
            if (clamped != progress) {
                Logger.log(LogLevel.WARN, true, "In-game progress bar went out of bounds: {} != {}", progress, clamped);
            } else if (Adjust.progressBarRenderer != null) {
                Adjust.progressBarRenderer.bars.add(new ProgressBar(clamped));
            }
        }
    }

    @EventHandler
    public void onGameLoop(GameLoopEvent event) {
        if (!event.preScheduled) return;
        if (ServerTracker.INSTANCE.packetTimer.hasElapsed(70L)) return;
        sentPackets.removeIf(timer -> timer.hasElapsed(1000L));
    }

    @EventHandler
    public void onPacketSend(LowLevelPacketSendEvent event) {
        if (event.packet instanceof C08PacketPlayerBlockPlacement placement) {
            ItemStack stack = placement.getStack();
            if (stack != null && stack.getItem() == Items.ender_pearl) {
                pearlThrowTimer.reset();
            }
        }
    }

    @EventHandler
    public void onPacket(DirectionalPacketEvent event) {
        if (mc.thePlayer == null) return;
        if (event.outgoing) {
            sentPackets.add(new Timer());
            return;
        }
        Packet<?> packet = event.packet;
        if (packet == null) return;
        int ping = ServerTracker.INSTANCE.serverState != null && ServerTracker.INSTANCE.serverState.packetWatcher != null
                ? ServerTracker.INSTANCE.serverState.packetWatcher.getAverageDelay()
                : 0;
        if (packet instanceof S08PacketPlayerPosLook) {
            if (!pearlThrowTimer.hasElapsed(ping + 500L)) {
                if (pearlSpawned) {
                    trigger(RewoundDisablerType.ENDER_PEARL);
                }
                pearlSpawned = false;
            }
        } else if (packet instanceof S0EPacketSpawnObject spawn) {
            if (spawn.getType() == 65 && !pearlThrowTimer.hasElapsed(ping + 70L)) {
                pearlSpawned = true;
            }
        } else if (packet instanceof S01PacketJoinGame) {
            activeTimer.lastMs = 0L;
            lastType = null;
            pearlThrowTimer.lastMs = 0L;
            pearlSpawned = false;
            sentPackets.clear();
        } else if (packet instanceof S27PacketExplosion explosion) {
            Vec3 motion = new Vec3(explosion.func_149149_c(), explosion.func_149144_d(), explosion.func_149147_e());
            if (motion.lengthVector() > 0.05D) {
                trigger(RewoundDisablerType.EXPLOSION);
            }
        }
    }
}
