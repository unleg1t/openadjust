package com.adjustclient.modules.impl.combat;

import com.adjustclient.utilities.anticheat.feature.Feature;
import com.adjustclient.modules.Module;
import com.adjustclient.modules.Category;
import com.adjustclient.core.settings.NumberSetting;

public class ReachModule extends Module {
    public static ReachModule INSTANCE;
    public final NumberSetting range;
    public ReachModule() {
        super("Reach", Category.COMBAT);
        INSTANCE = this;
        range = new NumberSetting(this, "Range", 3.2, 3.0, 6.0, 0.05);
    }
    public double getReach(double original) {
        return isEnabled() && com.adjustclient.utilities.anticheat.feature.Features.REACH.isAllowed() ? Math.max(original, range.getDouble()) : original;
    }
}
