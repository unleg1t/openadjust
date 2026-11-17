package com.adjustclient.module.impl.movement.jesus;

import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.ModeSetting;

public final class JesusModule extends Module {
    public static JesusModule INSTANCE;
    public final ModeSetting<JesusMode> mode;

    public JesusModule() {
        super("Jesus", Category.MOVEMENT);
        INSTANCE = this;
        SolidJesusMode solid = new SolidJesusMode(this);
        RewoundJesusMode rewound = new RewoundJesusMode(this);
        FloatJesusMode floating = new FloatJesusMode(this);
        mode = new ModeSetting<>(this, "Mode", solid, solid, rewound, floating);
        mode.addListener((oldMode, newMode) -> {
            if (isEnabled() && oldMode != newMode) {
                if (oldMode != null) ((JesusMode) oldMode).onDisable();
                ((JesusMode) newMode).onEnable();
            }
        });
    }

    @Override
    public void onEnable() {
        mode.getValue().onEnable();
    }

    @Override
    public void onDisable() {
        mode.getValue().onDisable();
    }

    @Override
    public String getSuffix() {
        return mode.getValue().getName();
    }
}
