package com.adjustclient.module.impl.combat;

import com.adjustclient.anticheat.feature.Feature;
import com.adjustclient.module.Module;
import com.adjustclient.module.Category;
import com.adjustclient.setting.NumberSetting;

public class ReachModule extends Module {
    public static ReachModule INSTANCE;
    public final NumberSetting range;
    public ReachModule() {
        super("Reach", Category.COMBAT);
        INSTANCE = this;
        range = new NumberSetting(this, "Range", 3.2, 3.0, 6.0, 0.05);
    }
    public double getReach(double original) {
        return isEnabled() && com.adjustclient.anticheat.feature.Features.REACH.isAllowed() ? Math.max(original, range.getDouble()) : original;
    }
}
