package com.adjustclient.modules.impl.movement.sprint;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.AttackInputStageEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.SprintEligibilityEvent;
import com.adjustclient.core.events.UserInputVerificationEvent;
import com.adjustclient.modules.impl.combat.displacement.DisplacementModule;
import com.adjustclient.modules.impl.combat.killaura.KillAuraModule;
import com.adjustclient.modules.impl.combat.killaura.target.TargetInfo;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.managers.util.ServiceUtil;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.SettingContainer;
import com.adjustclient.utilities.MovementUtil;
import com.adjustclient.utilities.RotationUtil;
import com.adjustclient.utilities.anticheat.ServerTracker;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public final class HypixelKeepSprintMode extends KeepSprintMode {
    public static HypixelKeepSprintMode INSTANCE;
    public final BooleanSetting targetMovingAway;
    public final BooleanSetting targetLookingAway;
    public boolean resetting;

    public HypixelKeepSprintMode(SprintModule module, SettingContainer parent) {
        super(module, "Hypixel");
        INSTANCE = this;
        ExpandSetting disableOn = new ExpandSetting(parent, "Disable on");
        disableOn.hideIf = () -> module.keepSprintMode.getValue() != this;
        targetMovingAway = new BooleanSetting(disableOn, "Target moving away", false);
        targetMovingAway.setDescription("Only activates when opponent is moving away from us.");
        targetLookingAway = new BooleanSetting(disableOn, "Target looking away", false);
        targetLookingAway.setDescription("Only activates when opponent is looking away from us.");
    }

    @Override
    public void onDisable() {
        resetting = false;
    }

    public boolean canAttack() {
        return !resetting || !mc.thePlayer.isSprinting();
    }

    @EventHandler
    public void onAttackInput(AttackInputStageEvent event) {
        if (!isActive() || !ServiceUtil.isActiveModel(ServerType.HYPIXEL)) return;
        if (DisplacementModule.INSTANCE.isEnabled()
                && (DisplacementModule.INSTANCE.displaced || DisplacementModule.INSTANCE.swapped)) {
            resetting = false;
            return;
        }
        if (resetting) {
            Adjust.mouseClickHelper.left.disabled = true;
            return;
        }
        if (!Adjust.mouseClickHelper.left.isPressed(false)) return;
        if (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null) return;
        if (mc.thePlayer.onGround && MovementUtil.isJumping()) return;
        Entity hit = mc.objectMouseOver.entityHit;
        if (targetMovingAway.isEnabled() && !isTargetMovingAway(hit)) return;
        if (targetLookingAway.isEnabled() && isTargetLookingAtPlayer(hit)) return;
        if (!mc.thePlayer.isSprinting() || SprintModule.shouldResetSprint()) return;
        resetting = true;
        Adjust.mouseClickHelper.left.disabled = true;
    }

    @EventHandler
    public void onSprintEligibility(SprintEligibilityEvent event) {
        if (!isActive() || !ServiceUtil.isActiveModel(ServerType.HYPIXEL)) return;
        if (DisplacementModule.INSTANCE.isEnabled()
                && (DisplacementModule.INSTANCE.displaced || DisplacementModule.INSTANCE.swapped)) {
            resetting = false;
            return;
        }
        if (mc.thePlayer.onGround && MovementUtil.isJumping()) return;
        Entity target;
        KillAuraModule aura = KillAuraModule.INSTANCE;
        if (aura.isEnabled()) {
            TargetInfo rotationTarget = aura.targetSelector.rotationTarget;
            if (rotationTarget == null || rotationTarget.entity.isDead() || !aura.attack(rotationTarget, true)) {
                return;
            }
            target = rotationTarget.entity.getEntity();
        } else {
            if (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null) return;
            target = mc.objectMouseOver.entityHit;
        }
        if (targetMovingAway.isEnabled() && !resetting && !isTargetMovingAway(target)) return;
        if (targetLookingAway.isEnabled() && !resetting && isTargetLookingAtPlayer(target)) return;
        if (ServerTracker.INSTANCE.attackedEntity != null && SprintModule.shouldResetSprint()) return;
        mc.thePlayer.setSprinting(false);
        mc.thePlayer.sprintingTicksLeft = 0;
        event.sprintEligible = false;
    }

    @EventHandler
    public void onInputVerification(UserInputVerificationEvent event) {
        if (!isActive() || mc.thePlayer == null) return;
        if (DisplacementModule.INSTANCE.isEnabled()
                && (DisplacementModule.INSTANCE.displaced || DisplacementModule.INSTANCE.swapped)) {
            resetting = false;
            return;
        }
        if (!resetting || mc.thePlayer.isSprinting()) return;
        resetting = false;
        Adjust.mouseClickHelper.left.clicks = 1;
        Adjust.mouseClickHelper.left.down = Boolean.TRUE;
    }

    public boolean isTargetMovingAway(Entity target) {
        if (target.motionX == 0.0D && target.motionZ == 0.0D) return false;
        double motionYaw = Math.toDegrees(Math.atan2(-target.motionX, target.motionZ));
        if (motionYaw < 0.0D) motionYaw += 360.0D;
        float towardPlayerYaw = RotationUtil.getYawBetween(
                mc.thePlayer.getPositionEyes(1.0F), target.getPositionVector()).x;
        return Math.abs(MathHelper.wrapAngleTo180_float((float) motionYaw - towardPlayerYaw)) > 90.0F;
    }

    public static boolean isTargetLookingAtPlayer(Entity target) {
        float towardPlayerYaw = RotationUtil.getYawBetween(
                mc.thePlayer.getPositionEyes(1.0F), target.getPositionVector()).x;
        return Math.abs(MathHelper.wrapAngleTo180_float(target.rotationYaw - towardPlayerYaw)) <= 90.0F;
    }
}
