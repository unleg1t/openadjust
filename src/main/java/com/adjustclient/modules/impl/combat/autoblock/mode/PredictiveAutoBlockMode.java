package com.adjustclient.modules.impl.combat.autoblock.mode;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.AttackEntityEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.GameLoopEvent;
import com.adjustclient.core.events.IngameActionEvent;
import com.adjustclient.core.events.WorldResetEvent;
import com.adjustclient.utilities.game.packet.PacketHold;
import com.adjustclient.modules.impl.combat.autoblock.AutoBlockMode;
import com.adjustclient.modules.impl.combat.autoblock.AutoBlockModule;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.entity.CombatStats;
import com.adjustclient.utilities.anticheat.entity.TrackedLivingEntity;
import net.minecraft.entity.Entity;
import org.lwjgl.input.Mouse;

public final class PredictiveAutoBlockMode extends AutoBlockMode {
    public static PredictiveAutoBlockMode INSTANCE;
    public final PingSetting ping;
    public final NumberSetting maxHoldTime;
    public final BooleanSetting fakeBlockVisual;
    public final BooleanSetting lagEnabled;
    public final NumberSetting lagMaxTime;
    public final BooleanSetting preventDelayingAttacks;
    public final BooleanSetting blockAgainImmediately;
    public final BooleanSetting holdAttack;
    public final BooleanSetting holdBlock;
    public final BooleanSetting onlyWhenHurt;
    public final Timer timer;
    public AutoBlockState state;
    public PacketHold hold;
    private int nextBlockTick;
    public boolean fakeBlocking;
    public boolean releasePending;

    public PredictiveAutoBlockMode(AutoBlockModule module) {
        super(module, "Predictive");
        INSTANCE = this;
        timer = new Timer();
        state = AutoBlockState.IDLE;
        ping = new PingSetting(module, () -> module.mode.getValue() != this);
        maxHoldTime = new NumberSetting(module, "Max hold time", 50, 50, 500, 1,
                () -> module.mode.getValue() != this);
        maxHoldTime.formatter(NumberSetting.MILLISECONDS);
        fakeBlockVisual = new BooleanSetting(module, "Fake block visual", false,
                () -> module.mode.getValue() != this);
        ExpandSetting lag = new ExpandSetting(module, "Lag",
                () -> module.mode.getValue() != this);
        lag.setDescription("Simulates outbound network lag to block longer.");
        lagEnabled = new BooleanSetting(lag, "Enabled", true, "Yes", "No",
                () -> module.mode.getValue() != this);
        lag.enabledPredicate = () -> lagEnabled.isEnabled();
        lagMaxTime = new NumberSetting(lag, "Max. time", 100, 50, 500, 1);
        lagMaxTime.formatter(NumberSetting.MILLISECONDS);
        preventDelayingAttacks = new BooleanSetting(lag, "Prevent delaying attacks", true);
        preventDelayingAttacks.setDescription("Prevents lag from delaying attacks.");
        blockAgainImmediately = new BooleanSetting(module, "Block again immediately", false,
                () -> module.mode.getValue() != this);
        blockAgainImmediately.setDescription("Block again immediately after unblocking.");
        holdAttack = new BooleanSetting(module, "Hold attack", true,
                () -> module.mode.getValue() != this);
        holdAttack.setDescription("Requires you to hold down attack to auto block.");
        holdBlock = new BooleanSetting(module, "Hold block", true,
                () -> module.mode.getValue() != this);
        holdBlock.setDescription("Requires you to hold down block to auto block.");
        onlyWhenHurt = new BooleanSetting(module, "Only when hurt", false,
                () -> module.mode.getValue() != this);
        onlyWhenHurt.setDescription("Only auto block when hurt.");
    }

    private static boolean isHurtWindowReady(CombatStats stats, int ticks, int pingMs) {
        int latency = (int) (pingMs * 1.5F);
        long now = System.currentTimeMillis();
        long hurtDelay = 50L * (9 - ticks) - latency;
        if (stats.hurtWindowOnly) {
            return hurtDelay <= now - stats.lastHurt;
        }
        return 50L * (10 - ticks) <= now - stats.lastAttack
                && hurtDelay <= now - stats.lastHurt;
    }

    private boolean shouldStartBlock(TrackedLivingEntity player) {
        CombatStats stats = player.combatStats;
        if (!onlyWhenHurt.isEnabled()) {
            if (System.currentTimeMillis() - stats.lastHurt >= 500L) {
                return true;
            }
            return isHurtWindowReady(stats, 0, ping.getPing());
        }
        if (System.currentTimeMillis() - stats.lastHurt < 500L) {
            return isHurtWindowReady(stats, 0, ping.getPing()) && module.hasThreat(true);
        }
        return false;
    }

    @EventHandler(priority = 81)
    public void onTick(IngameActionEvent event) {
        if (event.suppressActions) return;
        fakeBlocking = false;
        if (!isEnabled()) return;
        if (!hasRequiredButtons() || !module.hasSword(true)) {
            releasePending = false;
            state = AutoBlockState.IDLE;
            timer.reset();
            releaseHold();
            return;
        }
        var use = Adjust.mouseClickHelper.right;
        use.clicks = null;
        use.down = false;
        use.holdTicks = 0;
        if (fakeBlockVisual.isEnabled()) fakeBlocking = true;
        if (!module.shouldBlock()) {
            releasePending = false;
            state = AutoBlockState.IDLE;
            timer.reset();
            releaseHold();
            return;
        }
        var player = ServerTracker.INSTANCE.entityTracker.getLocalPlayer();
        if (player != null && System.currentTimeMillis() - player.combatStats.lastHurt < 500L
                && !isHurtWindowReady(player.combatStats, 0, ping.getPing())) {
            nextBlockTick = 0;
            resetAndFlush();
            return;
        }
        if (state == AutoBlockState.LAG && timer.hasElapsed(lagMaxTime.getLong())) {
            state = AutoBlockState.IDLE;
            releasePending = true;
        }
        if (player == null) return;
        if (shouldStartBlock(player)) {
            if (state == AutoBlockState.IDLE && (blockAgainImmediately.isEnabled()
                    || mc.thePlayer.ticksExisted >= nextBlockTick)) {
                state = AutoBlockState.BLOCK;
                nextBlockTick = mc.thePlayer.ticksExisted + 10;
                timer.reset();
            }
            if (state != AutoBlockState.BLOCK) return;
            if (!timer.hasElapsed(maxHoldTime.getLong())) {
                use.clicks = 1;
                use.down = true;
            } else if (lagEnabled.isEnabled()) {
                state = AutoBlockState.LAG;
                timer.reset();
                addHold();
            } else {
                state = AutoBlockState.IDLE;
            }
        }
    }

    @EventHandler
    public void onGameLoop(GameLoopEvent event) {
        if (!isEnabled() || !event.preScheduled || mc.thePlayer == null) return;
        if (releasePending) {
            releaseHold();
            releasePending = false;
        }
    }

    @EventHandler(priority = 1)
    public void onWorldReset(WorldResetEvent event) {
        if (!isEnabled()) return;
        nextBlockTick = 0;
        resetAndFlush();
    }

    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        if (!event.pre || !isEnabled() || !preventDelayingAttacks.isEnabled()) return;
        Entity target = event.entity;
        var tracked = ServerTracker.INSTANCE.entityTracker.get(target);
        if (state != AutoBlockState.LAG || !(tracked instanceof TrackedLivingEntity)) return;
        var stats = ((TrackedLivingEntity) tracked).combatStats;
        if (isHurtWindowReady(stats, 1, ping.getPing())) {
            state = AutoBlockState.IDLE;
            releasePending = true;
        }
    }

    public boolean isBlockingClick() {
        return state == AutoBlockState.BLOCK && Adjust.mouseClickHelper.right.isClicked(false);
    }

    public boolean hasRequiredButtons() {
        if (holdAttack.isEnabled() && (!Mouse.isCreated() || !Mouse.isButtonDown(0))) return false;
        return !holdBlock.isEnabled() || Mouse.isCreated() && Mouse.isButtonDown(1);
    }

    public void addHold() {
        if (hold == null) hold = Adjust.outboundPacketQueue.addHold(null, false);
    }

    public void releaseHold() {
        if (hold != null) {
            Adjust.outboundPacketQueue.removeHold(hold);
            hold = null;
        }
    }

    public void resetAndFlush() {
        releasePending = false;
        state = AutoBlockState.IDLE;
        timer.reset();
        if (hold != null) {
            Adjust.outboundPacketQueue.removeHold(hold);
            hold = null;
        }
    }
}
