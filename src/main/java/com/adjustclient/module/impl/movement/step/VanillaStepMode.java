package com.adjustclient.module.impl.movement.step;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MoveHeadingEvent;
import com.adjustclient.setting.NumberSetting;

public class VanillaStepMode extends StepMode {
    public static VanillaStepMode INSTANCE;
    public final NumberSetting height;

    public VanillaStepMode(StepModule module) {
        super(module, "Vanilla");
        INSTANCE = this;
        height = new NumberSetting(module, "Height", 1.5F, 1.0F, 10.0F, 0.5F,
                () -> module.mode.getValue() != this);
    }

    @EventHandler public void onStepHeight(com.adjustclient.event.StepHeightEvent event) {
        if (!isActive() || module.disableBind.isDown()) return;
        event.stepHeight = height.getFloat();
    }
}
