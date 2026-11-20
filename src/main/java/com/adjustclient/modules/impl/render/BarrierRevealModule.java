package com.adjustclient.modules.impl.render;

import com.adjustclient.modules.Module;
import com.adjustclient.modules.UtilityModule;
import com.adjustclient.modules.Category;
import com.adjustclient.core.settings.*;

public final class BarrierRevealModule extends UtilityModule {
    public static BarrierRevealModule INSTANCE;
    @Override public String getDescription() { return "Makes barriers always visible."; }
    public BarrierRevealModule() { super("Barrier Reveal", Category.RENDER); INSTANCE = this; }
}
