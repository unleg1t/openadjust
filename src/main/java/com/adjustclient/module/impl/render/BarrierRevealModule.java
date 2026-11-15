package com.adjustclient.module.impl.render;

import com.adjustclient.module.Module;
import com.adjustclient.module.UtilityModule;
import com.adjustclient.module.Category;
import com.adjustclient.setting.*;

public final class BarrierRevealModule extends UtilityModule {
    public static BarrierRevealModule INSTANCE;
    @Override public String getDescription() { return "Makes barriers always visible."; }
    public BarrierRevealModule() { super("Barrier Reveal", Category.RENDER); INSTANCE = this; }
}
