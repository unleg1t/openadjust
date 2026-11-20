package com.adjustclient.modules.impl.render;

import com.adjustclient.modules.Module;
import com.adjustclient.modules.UtilityModule;
import com.adjustclient.modules.Category;
import com.adjustclient.core.settings.*;

public final class MoreParticlesModule extends Module {
    public static MoreParticlesModule INSTANCE;
    public final NumberSetting enchantment = new NumberSetting(this, "Enchantment", 1, 1, 10, 1);
    public final NumberSetting critical = new NumberSetting(this, "Critical", 1, 1, 10, 1);
    public final BooleanSetting alwaysShowSharpParticles = new BooleanSetting(this, "Always show sharp particles", true);
    public MoreParticlesModule() { super("More Particles", Category.RENDER); INSTANCE = this; }
}
