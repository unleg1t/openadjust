package com.adjustclient.modules.impl.player.fastuse.feature;

import com.adjustclient.core.events.BlockBreakProgressEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.LowLevelPacketSendEvent;
import com.adjustclient.core.events.MiningFatigueEvent;
import com.adjustclient.core.events.MotionEvent;
import com.adjustclient.core.events.WorldResetEvent;
import com.adjustclient.modules.impl.player.fastuse.FastUseFeature;
import com.adjustclient.modules.impl.player.fastuse.FastUseModule;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.ModeSetting;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.utilities.BlockBreakUtil;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;

public final class BlockBreakingFeature extends FastUseFeature {
    public static BlockBreakingFeature INSTANCE;
    public final BooleanSetting enabled;
    public final ModeSetting<HitDelayMode> hitDelay;
    public final NumberSetting maxHitDelay;
    public final BooleanSetting overrideDamage;
    public final NumberSetting addedTicks;
    public final ModeSetting<GroundSpoofMode> groundSpoof;
    public final BooleanSetting noSwapReset;
    public final BooleanSetting ignoreMiningFatigue;
    public final NumberSetting multiplier;
    public final BreakProgressTracker tracker;
    public boolean hitDelayApplied;
    public boolean spoofGround;

    public BlockBreakingFeature(FastUseModule module) {
        super(module);
        INSTANCE = this;
        tracker = new BreakProgressTracker(this);
        ExpandSetting group = new ExpandSetting(module, "Block breaking");
        group.enabledPredicate = () -> isFeatureEnabled();
        enabled = new BooleanSetting(group, "Enabled", true, "Yes", "No");
        hitDelay = new ModeSetting<>(group, "Hit delay", HitDelayMode.DEFAULT,
                HitDelayMode.DEFAULT, HitDelayMode.RESET, HitDelayMode.FORCE_TICK);
        maxHitDelay = new NumberSetting(group, "Max hit delay", 5, 1, 5, 1,
                () -> hitDelay.getValue() == HitDelayMode.RESET);
        overrideDamage = new BooleanSetting(group, "Override damage", true, "On", "Off");
        addedTicks = new NumberSetting(group, "Added ticks", 0, 0, 5, 1,
                () -> !overrideDamage.isEnabled());
        groundSpoof = new ModeSetting<GroundSpoofMode>(group, "Ground spoof", GroundSpoofMode.NONE,
                () -> !overrideDamage.isEnabled(), GroundSpoofMode.NONE, GroundSpoofMode.VANILLA);
        noSwapReset = new BooleanSetting(group, "No swap reset", true, "On", "Off");
        ignoreMiningFatigue = new BooleanSetting(group, "Ignore mining fatigue", false, "On", "Off");
        multiplier = new NumberSetting(group, "Multiplier", 1.0F, 1.0F, 2.0F, 0.1F).formatter(NumberSetting.MULTIPLIER);
    }

    @Override public void onDisable() {
        hitDelayApplied = false;
        spoofGround = false;
    }

    public void updateHitDelay() {
        if (maxHitDelay.getValue().intValue() < mc.playerController.blockHitDelay) {
            mc.playerController.blockHitDelay = maxHitDelay.getValue().intValue();
        }
        hitDelayApplied = true;
    }

    @EventHandler public void updateDamage(BlockBreakProgressEvent event) {
        if (!isActive() || !overrideDamage.isEnabled()) return;
        int ticks = mc.playerController.blockBreakingTicks + addedTicks.getValue().intValue();
        float damage = ticks * event.strength;
        if (damage >= 1.0F || groundSpoof.getValue() != GroundSpoofMode.VANILLA) {
            event.damage = damage;
            spoofGround = false;
            return;
        }
        BlockPos pos = event.pos;
        float hardness = BlockBreakUtil.getRelativeHardness(mc.thePlayer,
                mc.theWorld.getBlockState(pos).getBlock(), pos, mc.thePlayer.getHeldItem(), true);
        event.damage = hardness * ticks;
        spoofGround = event.damage < 1.0F
                && event.damage + hardness >= 1.0F;
    }

    @EventHandler(priority = -5) public void onMotion(MotionEvent event) {
        if (isActive() && event.pre && spoofGround) {
            event.onGround = true;
            spoofGround = false;
        }
    }

    @EventHandler public void onWorldLoad(WorldResetEvent event) {
        if (isActive()) spoofGround = false;
    }

    public void resetHitDelay() {
        if (!isActive()) return;
        if (hitDelay.getValue() == HitDelayMode.RESET) {
            mc.playerController.blockHitDelay = 0;
            return;
        }
        if (mc.playerController.getCurrentGameType() == net.minecraft.world.WorldSettings.GameType.CREATIVE) {
            mc.playerController.blockHitDelay = 0;
            return;
        }
        if (hitDelay.getValue() == HitDelayMode.FORCE_TICK) {
            if (!hitDelayApplied && mc.playerController.blockHitDelay >= 1) mc.playerController.blockHitDelay--;
            hitDelayApplied = false;
        }
    }

    @EventHandler public void onMiningFatigue(MiningFatigueEvent event) {
        if (isActive() && ignoreMiningFatigue.isEnabled()) event.cancelled = true;
    }

    @EventHandler public void onPacket(LowLevelPacketSendEvent event) {
        if (!isActive() || event.packet == null || event.packet.getClass() != C07PacketPlayerDigging.class) return;
        C07PacketPlayerDigging packet = (C07PacketPlayerDigging) event.packet;
        if (packet.getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) tracker.add(packet.getPosition());
    }

    @Override public boolean isFeatureEnabled() { return enabled.isEnabled(); }

    public float getMultiplier() {
        if (!isActive()) return 1.0F;
        return multiplier.getValue().floatValue();
    }

    public enum HitDelayMode implements com.adjustclient.core.settings.ModeOption {
        DEFAULT("Default"), RESET("Reset"), FORCE_TICK("Force Tick");
        private final String name;
        HitDelayMode(String name) { this.name = name; }
        @Override public String getName() { return name; }
    }
    public enum GroundSpoofMode implements com.adjustclient.core.settings.ModeOption {
        NONE("None"), VANILLA("Vanilla");
        private final String name;
        GroundSpoofMode(String name) { this.name = name; }
        @Override public String getName() { return name; }
    }
}
