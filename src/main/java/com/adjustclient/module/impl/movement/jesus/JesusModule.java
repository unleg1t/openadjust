package com.adjustclient.module.impl.movement.jesus;

import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.ModeSetting;
import java.util.Objects;

public final class JesusModule extends Module {
    public static JesusModule INSTANCE;
    public final ModeSetting<JesusMode> mode;

    public JesusModule() {
        super("Jesus", Category.MOVEMENT);
        INSTANCE = this;
        SolidJesusMode solid = new SolidJesusMode(this);
        FloatJesusMode floating = new FloatJesusMode(this);
        mode = new ModeSetting<>(this, "Mode", solid, solid, floating);
        mode.addListener((oldMode, newMode) -> {
            if (isEnabled() && oldMode != newMode) {
                if (oldMode instanceof FloatJesusMode old) {
                    old.waterTicks = 0;
                    old.ascentTicks = 0;
                }
                Objects.requireNonNull(newMode);
            }
        });
    }

    @Override
    public void onEnable() {
        Objects.requireNonNull(mode.getValue());
    }

    @Override
    public void onDisable() {
        if (Objects.requireNonNull(mode.getValue()) instanceof FloatJesusMode floating) {
            floating.waterTicks = 0;
            floating.ascentTicks = 0;
        }
    }

    @Override
    public String getSuffix() {
        return mode.getValue().getName();
    }
}
