package com.adjustclient.modules.impl.movement.sprint;

import com.adjustclient.core.events.AttackSlowdownEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.core.settings.SettingContainer;

public final class VanillaKeepSprintMode extends KeepSprintMode {
    public static VanillaKeepSprintMode INSTANCE;
    public final NumberSetting slowdown;
    public final BooleanSetting sprintReset;

    public VanillaKeepSprintMode(SprintModule module, SettingContainer parent) {
        super(module, "Vanilla");
        INSTANCE = this;
        slowdown = new NumberSetting(parent, "Slowdown", 0, 0, 100, 1,
                () -> module.keepSprintMode.getValue() != this);
        slowdown.formatter(setting -> setting.getInt() == 40 ? "Default" : setting.getInt() + "%");
        sprintReset = new BooleanSetting(parent, "Sprint reset", false,
                () -> module.keepSprintMode.getValue() != this);
    }

    @EventHandler
    public void onAttackSlowdown(AttackSlowdownEvent event) {
        if (!isActive()) return;
        if (sprintReset.isEnabled() && SprintModule.shouldResetSprint()) {
            event.factor = 0.6D;
            return;
        }
        event.factor = 1.0D - slowdown.getValue().doubleValue() / 100.0D;
    }
}
