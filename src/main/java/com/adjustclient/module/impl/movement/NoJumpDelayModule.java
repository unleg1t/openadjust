package com.adjustclient.module.impl.movement;

import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.BooleanSetting;

public final class NoJumpDelayModule extends Module {
    public static NoJumpDelayModule INSTANCE;
    public final BooleanSetting disableWithFly;

    public NoJumpDelayModule() {
        super("No Jump Delay", Category.MOVEMENT);
        INSTANCE = this;
        disableWithFly = new BooleanSetting(this, "Disable with fly", false);
        disableWithFly.setDescription("Normally, double jumping will make you fly; but this module doesn't make you fly, therefore it can be detected. Enabling this option will make the module completely undetectable.");
    }

    public boolean shouldRemoveDelay() {
        return isEnabled() && (!disableWithFly.isEnabled() || mc.thePlayer == null || !mc.thePlayer.capabilities.allowFlying);
    }
}
