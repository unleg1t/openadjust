package com.adjustclient.module.impl.render.trajectories.settings;
import com.adjustclient.setting.*;
import java.util.function.BooleanSupplier;
public final class ExplosionSettings {
    public final BooleanSetting enabled;
    public final NumberSetting expand;
    public final BooleanSetting outline;
    public ExplosionSettings(SettingContainer parent) {
        ExpandSetting group = new ExpandSetting(parent, "Entity Hit Box");
        enabled = new BooleanSetting(group, "Enabled", false);
        BooleanSupplier hidden = () -> !enabled.isEnabled();
        group.enabledPredicate = hidden;
        expand = new NumberSetting(group, "Expand", 0.1F, 0.0F, 0.2F, 0.05F);
        outline = new BooleanSetting(group, "Outline", true);
    }
    public float getExpand() { return expand.getValue().floatValue(); }
}
