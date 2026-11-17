package com.adjustclient.module.impl.combat;

import com.adjustclient.event.AttackEntityEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.event.MovementInputEvent;
import com.adjustclient.event.PostInputEvent;
import com.adjustclient.event.SprintEligibilityEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.exploit.playerdetect.hackerdetect.HackerDetector;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.anticheat.ServerTracker;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public class MoreKnockbackModule extends Module implements MinecraftInstance {
    public static MoreKnockbackModule INSTANCE;
    private MoreKnockbackState state;
    public final ModeSetting<MoreKnockbackMode> mode;
    public final NumberSetting delay;
    public final NumberSetting stopDuration;
    public final ExpandSetting targets;
    public final BooleanSetting targetsPlayers;
    public final BooleanSetting targetsTeammates;
    public final BooleanSetting targetsHackers;
    public final BooleanSetting targetsPassive;
    public final BooleanSetting targetsHostile;
    private int ticks;
    private boolean cancelSprint;

    private static final MoreKnockbackState IDLE = MoreKnockbackState.IDLE;
    private static final MoreKnockbackState WAITING = MoreKnockbackState.ATTACKED;
    private static final MoreKnockbackState STOPPING = MoreKnockbackState.STOP;
    private static final MoreKnockbackState RESTORE = MoreKnockbackState.RESET;

    public MoreKnockbackModule() {
        super("More Knockback", Category.COMBAT);
        INSTANCE = this;
        state = IDLE;
        mode = new ModeSetting<>(this, "Mode", MoreKnockbackMode.values()[0], MoreKnockbackMode.values());
        delay = new NumberSetting(this, "Delay", 300.0, 0.0, 500.0, 10.0,
                () -> mode.getValue() == MoreKnockbackMode.values()[3]);
        delay.formatter(NumberSetting.MILLISECONDS);
        delay.setDescription("Delay before resetting your sprint after you attack.");
        stopDuration = new NumberSetting(this, "Stop duration", 50.0, 50.0, 300.0, 10.0,
                () -> mode.getValue() == MoreKnockbackMode.values()[3]);
        stopDuration.formatter(NumberSetting.MILLISECONDS);
        stopDuration.setDescription("Duration to stop sprinting.");
        targets = new ExpandSetting(this, "Targets");
        targetsPlayers = new BooleanSetting(targets, "Players", true);
        targetsTeammates = new BooleanSetting(targets, "Teammates", true);
        targetsHackers = new BooleanSetting(targets, "Hackers", false);
        targetsPassive = new BooleanSetting(targets, "Passive", false);
        targetsHostile = new BooleanSetting(targets, "Hostile", false);
    }

    @EventHandler
    public void onTick(PostInputEvent event) {
        if (state != RESTORE) return;
        KeyBinding sneak = mc.gameSettings.keyBindSneak;
        KeyBinding.setKeyBindState(sneak.getKeyCode(), GameSettings.isKeyDown(sneak));
        state = IDLE;
    }

    @EventHandler
    public void onSprint(SprintEligibilityEvent event) {
        if (!cancelSprint) return;
        event.sprintEligible = false;
        mc.thePlayer.setSprinting(false);
        mc.thePlayer.sprintingTicksLeft = 0;
        cancelSprint = false;
    }

    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        if (!event.pre) return;
        if (!isEnabled()) return;
        if (mode.getValue() != MoreKnockbackMode.values()[3] && (state == STOPPING || state == WAITING)) return;
        var entity = event.entity;
        if (entity == null) return;
        var tracked = ServerTracker.INSTANCE.entityTracker.get(entity);
        if (tracked == null) return;
        int filter = (targetsPlayers.isEnabled() ? 2 : 0)
                | (targetsHostile.isEnabled() ? 4 : 0)
                | (targetsPassive.isEnabled() ? 8 : 0)
                | (targetsTeammates.isEnabled() ? 0x60 : 0x40);
        if (tracked.entity.isUser() || tracked.isRemoved() || !tracked.matchesType(filter)) return;
        if (!targetsHackers.isEnabled() && HackerDetector.isCheater(tracked.entityId)) return;
        state = WAITING;
        ticks = 0;
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!isEnabled() || !event.pre || mode.getValue() != MoreKnockbackMode.values()[3]) return;
        if (state == WAITING) state = STOPPING;
        else if (state != STOPPING) return;
        event.sprinting = false;
        state = RESTORE;
    }

    @EventHandler
    public void onMovementInput(MovementInputEvent event) {
        if (!isEnabled()) return;
        if (state == WAITING) {
            if (((Number) delay.getValue()).longValue() / 50L <= ticks) {
                state = STOPPING;
                ticks = 0;
            }
        }
        if (state == STOPPING) {
            if (((Number) stopDuration.getValue()).longValue() / 50L <= ticks) {
                state = RESTORE;
                return;
            }
            switch (mode.getValue().ordinal()) {
                case 0: event.forward = false; break;
                case 1: event.sneak = true; break;
                case 2: cancelSprint = true; break;
                default: break;
            }
        }
        if (state == WAITING || state == STOPPING) ticks++;
    }

    @Override public void onEnable() {
        state = IDLE;
        ticks = 0;
    }

    @Override public String getDescription() {
        return "Deal more knockback to your targets, commonly referred to as W-Tap.";
    }
}
