package com.adjustclient.module.impl.player.nofall;
import com.adjustclient.Adjust;
import com.adjustclient.anticheat.feature.ModuleFeature;
import com.adjustclient.hypixel.game.SkyWarsGameState;
import com.adjustclient.event.*;
import com.adjustclient.module.Module;
import com.adjustclient.module.Category;
import com.adjustclient.module.impl.player.nofall.mode.*;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.hypixel.*;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.*;
import com.adjustclient.util.anticheat.ServerTracker;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
public final class NoFallModule extends Module {
    public static NoFallModule INSTANCE;
    public final PacketNoFallMode packet;
    public final WaterNoFallMode water;
    public final ModeSetting<NoFallMode> mode;
    public float lastFallDistance;
    public int field52;
    public NoFallModule() {
        super("No Fall", Category.PLAYER);
        INSTANCE = this;
        packet = new PacketNoFallMode(this);
        water = new WaterNoFallMode(this);
        SpoofNoFallMode spoof = new SpoofNoFallMode(this);
        RewoundNoFallMode rewound = new RewoundNoFallMode(this);
        mode = new ModeSetting<>(this, "Mode", spoof, new NoFallMode[] {spoof, packet, rewound, water});
        settings.remove(mode);
        settings.add(0, mode);
        mode.addListener((Runnable) () -> { java.util.Objects.requireNonNull(this); });
    }
    public boolean canUseOnHypixel() {
        var tracker = ServerTracker.INSTANCE.serverState;
        if (tracker == null || tracker.antiCheatModel == null
                || tracker.antiCheatModel.serverType != ServerType.HYPIXEL
                || !tracker.antiCheatModel.isAllChecksPassed()) return false;
        HypixelGameState state = Adjust.gameStateTracker.state;
        if ((state.game.flags & 0x10) != 0
                && (state.game != HypixelGame.BED_WARS_PRACTICE || !(mode.getValue() instanceof WaterNoFallMode))) return false;
        long delay = state.game == HypixelGame.SKYWARS && state instanceof SkyWarsGameState
                && ((SkyWarsGameState)state).isLargeGame() ? 15000L : 5000L;
        if (state.startTimer == null || state.startTimer.lastMs + delay > System.currentTimeMillis() || state.ended) return false;
        return !WorldUtil.isOverVoid() || mc.thePlayer.fallDistance <= 6.0F;
    }
    public boolean isLandingSafe(float minimumFallDistance) {
        Simulation simulation = new Simulation(mc.thePlayer).includeSpeedModule();
        SimulationPlayer player = simulation.player;
        player.fallDistance = mc.thePlayer.fallDistance - lastFallDistance;
        int ticks = mc.thePlayer.motionY <= 0.0D ? 13 : Math.min(60,
                15 * (mc.thePlayer.isPotionActive(Potion.jump) ? mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1 : 1));
        for (int i = 0; i < ticks; i++) {
            player.tick();
            if (player.posX == player.prevPosX && player.posY == player.prevPosY && player.posZ == player.prevPosZ) return false;
            float halfWidth = mc.thePlayer.width * 0.5F;
            net.minecraft.util.Vec3 feet = new net.minecraft.util.Vec3(player.posX, player.posY, player.posZ);
            if (!WorldUtil.isColumnCollisionFree(feet, 0.0D, halfWidth)
                    && !player.onGround && player.fallDistance >= minimumFallDistance) return true;
        }
        return false;
    }
    @EventHandler public void onMotion(MotionEvent event) {
        if (!isEnabled() || !event.pre) return;
        if (mc.thePlayer.capabilities.allowFlying || mc.thePlayer.isOnLadder() || mc.thePlayer.ridingEntity != null)
            lastFallDistance = mc.thePlayer.fallDistance;
        else if (mc.thePlayer.fallDistance == 0.0F) lastFallDistance = 0.0F;
    }
    @Override public void onEnable() { lastFallDistance = 0.0F; }
    @Override public void onDisable() { }
    @Override public String getSuffix() { return mode.getValue().getName(); }
    public boolean canUse() {
        if (mc.thePlayer.capabilities.allowFlying) return false;
        var tracker = ServerTracker.INSTANCE.serverState;
        return tracker == null || tracker.antiCheatModel == null
                || tracker.antiCheatModel.serverType != ServerType.HYPIXEL || canUseOnHypixel();
    }
    public boolean isActive() {
        if (!isEnabled()) return false;
        NoFallMode selected = mode.getValue();
        if (selected instanceof PacketNoFallMode) return com.adjustclient.anticheat.feature.Features.NO_FALL_PACKET.isAllowed();
        if (selected instanceof SpoofNoFallMode) return com.adjustclient.anticheat.feature.Features.NO_FALL_SPOOF.isAllowed();
        if (selected instanceof RewoundNoFallMode) return com.adjustclient.anticheat.feature.Features.NO_FALL_REWOUND.isAllowed();
        return true;
    }
}
