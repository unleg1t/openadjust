package com.adjustclient.module.impl.movement.sprint;

import com.adjustclient.Adjust;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.DirectionalPacketEvent;
import com.adjustclient.event.SprintEligibilityEvent;
import com.adjustclient.game.packet.PacketHold;
import com.adjustclient.module.impl.combat.killaura.KillAuraModule;
import com.adjustclient.module.impl.combat.killaura.target.TargetInfo;
import com.adjustclient.module.impl.movement.noslowdown.HypixelNoSlowMode;
import com.adjustclient.module.impl.movement.noslowdown.NoSlowdownModule;
import com.adjustclient.util.MovementUtil;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public final class HypixelKeepSprintMode extends KeepSprintMode {
    public static HypixelKeepSprintMode INSTANCE;
    public PacketHold hold;
    public boolean allowSprint;

    public HypixelKeepSprintMode(SprintModule module) {
        super(module, "Hypixel");
        INSTANCE = this;
    }

    @Override public void onDisable() {
        releaseHold();
        allowSprint = false;
    }

    @EventHandler public void onSprintEligibility(SprintEligibilityEvent event) {
        if (!isActive()) return;
        if (hold != null) { releaseHold(); return; }
        if (mc.thePlayer.isUsingItem() && NoSlowdownModule.INSTANCE.isEnabled()
                && NoSlowdownModule.INSTANCE.mode.getValue() instanceof HypixelNoSlowMode) return;
        if (mc.thePlayer.onGround && MovementUtil.isJumping()) return;
        KillAuraModule aura = KillAuraModule.INSTANCE;
        if (!aura.isEnabled()) return;
        if (module.isVelocitySprintResetActive()) {
            allowSprint = true;
            return;
        }
        TargetInfo target = aura.targetSelector.rotationTarget;
        if (target != null && !target.entity.isDead() && aura.attack(target, true)) {
            mc.thePlayer.setSprinting(false);
            mc.thePlayer.sprintingTicksLeft = 0;
            event.sprintEligible = false;
        }
    }

    public boolean canAttack() {
        if (!mc.thePlayer.isSprinting()) return true;
        boolean result = allowSprint;
        allowSprint = false;
        return result;
    }

    public void releaseHold() {
        if (hold == null) return;
        Adjust.inboundPacketQueue.removeHold(hold);
        hold = null;
    }

    @EventHandler public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isActive() || mc.thePlayer == null || event.outgoing) return;
        if (event.packet instanceof S12PacketEntityVelocity
                && ((S12PacketEntityVelocity) event.packet).getEntityID() == mc.thePlayer.getEntityId()) beginHoldForTarget();
    }

    public void beginHold() {
        if (hold == null) hold = Adjust.inboundPacketQueue.addHold(null, false);
    }

    public void beginHoldForTarget() {
        if (!module.isSprintResetEnabled()) return;
        KillAuraModule aura = KillAuraModule.INSTANCE;
        if (aura.isEnabled() && aura.targetSelector.rotationTarget != null) beginHold();
    }
}
