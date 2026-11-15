package com.adjustclient.module.impl.movement.sprint;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.JumpMotionEvent;
import com.adjustclient.event.SprintEligibilityEvent;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.setting.SettingContainer;

public final class NormalOmniSprintMode extends OmniSprintMode {
    public static NormalOmniSprintMode INSTANCE;
    public final ModeSetting<OmniSprintConditionMode> condition;

    public NormalOmniSprintMode(SprintModule module, SettingContainer parent) {
        super(module, "Normal");
        INSTANCE = this;
        condition = new ModeSetting<>(parent, "Condition", OmniSprintConditionMode.ALWAYS,
                () -> module.omnidirectionalMode.getValue() != this, OmniSprintConditionMode.values());
    }

    @EventHandler(priority = 1) public void onTick(SprintEligibilityEvent event) {
        if (!isActive() || mc.thePlayer == null) return;
        boolean allowed;
        switch (condition.getValue()) {
            case AIR_ONLY: allowed = !mc.thePlayer.onGround; break;
            case GROUND_ONLY: allowed = mc.thePlayer.onGround; break;
            default: allowed = true;
        }
        if (condition.getValue() == OmniSprintConditionMode.GROUND_ONLY
                && allowed && mc.thePlayer.movementInput.jump && mc.thePlayer.onGround) allowed = false;
        mc.thePlayer.omniSprint = allowed;
    }

    @EventHandler public void onJumpSprint(JumpMotionEvent event) {
        if (isActive() && event.pre && condition.getValue() == OmniSprintConditionMode.NO_JUMP
                && mc.thePlayer.movementInput.moveForward < 0.8F) event.sprintImpulse = false;
    }
}
