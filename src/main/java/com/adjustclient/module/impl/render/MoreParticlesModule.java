package com.adjustclient.module.impl.render;

import com.adjustclient.module.Module;
import com.adjustclient.module.UtilityModule;
import com.adjustclient.module.Category;
import com.adjustclient.setting.*;

public final class MoreParticlesModule extends Module {
    public static MoreParticlesModule INSTANCE;
    public final NumberSetting enchantment = new NumberSetting(this, "Enchantment", 1, 1, 10, 1);
    public final NumberSetting critical = new NumberSetting(this, "Critical", 1, 1, 10, 1);
    public final BooleanSetting alwaysShowSharpParticles = new BooleanSetting(this, "Always show sharp particles", true);
    public MoreParticlesModule() { super("More Particles", Category.RENDER); INSTANCE = this; }
}
