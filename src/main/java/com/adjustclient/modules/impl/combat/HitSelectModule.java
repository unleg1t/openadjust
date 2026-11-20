package com.adjustclient.modules.impl.combat;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.*;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.*;
import com.adjustclient.utilities.*;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.entity.*;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.util.MovingObjectPosition;
import com.adjustclient.modules.impl.combat.displacement.DisplacementModule;
import com.adjustclient.modules.impl.movement.noslowdown.NoSlowdownModule;
import com.adjustclient.modules.impl.movement.sprint.VanillaKeepSprintMode;
import com.adjustclient.modules.impl.movement.sprint.SprintModule;

public final class HitSelectModule extends Module {
    public static HitSelectModule INSTANCE;
    public final ModeSetting<HitSelectMode> mode;
    public final NumberSetting pauseDuration;
    public final BooleanSetting allowSecondHit;
    public final BooleanSetting useServerHurtTime;
    public final BooleanSetting fakeSwing;
    public final NumberSetting combatCps;
    public final NumberSetting missedCps;
    public final BooleanSetting disableOnNotSprinting;
    public final BooleanSetting disableOnNoWeapon;
    public final BooleanSetting disableOnBlockingSword;
    private final Int2IntOpenHashMap pausedTargets = new Int2IntOpenHashMap();
    private final IntOpenHashSet secondHitPending = new IntOpenHashSet();
    private final ArrayList<HitSelectClick> clicks = new ArrayList<>();

    public HitSelectModule() {
        super("Hit Select", Category.COMBAT);
        INSTANCE = this;
        mode = new ModeSetting<>(this, "Mode", HitSelectMode.BURST, HitSelectMode.values());
        pauseDuration = new NumberSetting(this, "Pause duration", 500, 0, 500, 1);
        pauseDuration.formatter(NumberSetting.MILLISECONDS);
        allowSecondHit = new BooleanSetting(this, "Allow Second Hit", true);
        allowSecondHit.setDescription("Allows you to click a second time during pause duration to ensure a hit server-side.");
        useServerHurtTime = new BooleanSetting(this, "Use server hurt time", false);
        allowSecondHit.hideIf = () -> this.useServerHurtTime.isEnabled();
        useServerHurtTime.setDescription("Wait till server confirms hit.");
        fakeSwing = new BooleanSetting(this, "Fake swing", false);
        ExpandSetting cpsLimit = new ExpandSetting(this, "CPS limit");
        combatCps = new NumberSetting(cpsLimit, "Combat CPS", 2, 2, 20, 1);
        missedCps = new NumberSetting(cpsLimit, "Missed CPS", 10, 0, 20, 1);
        ExpandSetting disableOn = new ExpandSetting(this, "Disable on");
        disableOnNotSprinting = new BooleanSetting(disableOn, "Not sprinting", false);
        disableOnNoWeapon = new BooleanSetting(disableOn, "No weapon", false);
        disableOnBlockingSword = new BooleanSetting(disableOn, "Blocking sword", false);
    }

    public boolean shouldSuppressClick() {
        if (!canSelect()) return false;
        var hit = mc.objectMouseOver;
        if (hit == null || hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) return false;
        var tracked = ServerTracker.INSTANCE.entityTracker.get(hit.entityHit);
        if (!(tracked instanceof TrackedLivingEntity) || !((TrackedLivingEntity) tracked).matchesType(110)
                || !tracked.matches(110)) return shouldPause(null, missedCps.getInt());
        TrackedLivingEntity target = (TrackedLivingEntity) tracked;
        if (shouldPause(target, combatCps.getInt()) && !shouldPreserveSprint()) return true;
        {
            if (!useServerHurtTime.isEnabled()) {
                int id = target.entityId;
                if (allowSecondHit.isEnabled() && pausedTargets.containsKey(id) && secondHitPending.remove(id)) return false;
                pausedTargets.put(id, mc.thePlayer.ticksExisted + (int) (pauseDuration.getLong() / 50));
                if (allowSecondHit.isEnabled()) secondHitPending.add(id);
            }
            return false;
        }
    }

    public float simulatedForwardMotion(boolean attacking) {
        var player = mc.thePlayer;
        Simulation snapshot = new Simulation(player);
        SimulationPlayer simulation = snapshot.player;
        if (attacking) {
            AttackSlowdownEvent slowdown = new AttackSlowdownEvent(0.6);
            if (VanillaKeepSprintMode.INSTANCE != null && VanillaKeepSprintMode.INSTANCE.isActive())
                VanillaKeepSprintMode.INSTANCE.onAttackSlowdown(slowdown);
            if (SprintModule.INSTANCE.isEnabled() && SprintModule.INSTANCE.isVelocitySprintResetActive()) slowdown.factor = 0.6;
            simulation.motionX *= slowdown.factor;
            simulation.motionZ *= slowdown.factor;
            simulation.setSprinting(false);
            simulation.sprintingTicksLeft = 0;
        }
        simulation.tick();
        double yaw = player.rotationYaw * 0.0174532925199433;
        return (float) (simulation.motionX * Math.sin(yaw) - Math.cos(yaw) * simulation.motionZ);
    }

    public boolean isClickDelayElapsed(int cps) {
        HitSelectClick last = null;
        int count = 0;
        Iterator<HitSelectClick> iterator = clicks.iterator();
        while (iterator.hasNext()) {
            HitSelectClick click = iterator.next();
            if (click.timer.hasElapsed(1000L)) iterator.remove();
            else { last = click; count++; }
        }
        int effective = Math.min(20, Math.max(1, cps - count / 11));
        int delay = 920 / effective;
        return delay < 53 || last == null || last.timer.hasElapsed(delay);
    }

    @EventHandler(priority = 40)
    public void onAttackTick(AttackInputStageEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        var left = Adjust.mouseClickHelper.left;
        if (!left.isPressed(false) || DisplacementModule.INSTANCE.swapped) return;
        if (shouldSuppressClick()) {
            left.disabled = true;
            if (fakeSwing.isEnabled()) mc.thePlayer.swingItem();
        }
        if (!left.disabled) clicks.add(new HitSelectClick());
    }

    @EventHandler public void onTick(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled() || mc.thePlayer == null) return;
        pausedTargets.int2IntEntrySet().removeIf(entry -> entry.getIntValue() <= mc.thePlayer.ticksExisted);
        secondHitPending.removeIf((java.util.function.IntPredicate) id -> !pausedTargets.containsKey(id));
    }

    public boolean shouldPause(TrackedLivingEntity target, int cps) {
        return switch (mode.getValue()) {
            case BURST -> shouldPauseBurst(target, cps);
            case CRITICAL -> !mc.thePlayer.onGround && mc.thePlayer.motionY > 0.0 && shouldPauseBurst(target, cps);
        };
    }

    public boolean shouldPauseBurst(TrackedLivingEntity target, int cps) {
        if (target == null) return cps == 0 || !isClickDelayElapsed(cps);
        if (useServerHurtTime.isEnabled()) {
            if (!target.combatStats.canKillWithHeldItem(9 - (int) (pauseDuration.getLong() / 50))) return true;
            return !isClickDelayElapsed(cps);
        }
        if (!pausedTargets.containsKey(target.entityId)) return false;
        if (allowSecondHit.isEnabled() && secondHitPending.contains(target.entityId)) return false;
        return cps == 0 || !isClickDelayElapsed(cps);
    }

    @Override public void onDisable() { pausedTargets.clear(); secondHitPending.clear(); }
    @Override public String getDescription() { return "Limits unnecessary clicks to reduce average click speed."; }
    public boolean shouldPreserveSprint() {
        if (ServerTracker.INSTANCE.field116.hasElapsed(500L)) return false;
        if ((mc.thePlayer.getDataWatcher().getWatchableObjectByte(0) & 8) == 0) return false;
        float attacking = simulatedForwardMotion(true);
        return simulatedForwardMotion(false) > attacking;
    }
    public boolean canSelect() {
        if (disableOnNotSprinting.isEnabled() && (mc.thePlayer.getDataWatcher().getWatchableObjectByte(0) & 8) == 0) return false;
        if (disableOnNoWeapon.isEnabled() && !WorldUtil.isHoldingWeapon()) return false;
        return !disableOnBlockingSword.isEnabled() || !mc.thePlayer.isBlocking() && !NoSlowdownModule.INSTANCE.isBlocking();
    }
    @EventHandler public void onWorldChange(WorldResetEvent event) { pausedTargets.clear(); secondHitPending.clear(); }
}
