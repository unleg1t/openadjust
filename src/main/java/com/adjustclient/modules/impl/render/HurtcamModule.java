package com.adjustclient.modules.impl.render;

import com.adjustclient.modules.Module;
import com.adjustclient.modules.UtilityModule;
import com.adjustclient.modules.Category;
import com.adjustclient.core.settings.*;

public final class HurtcamModule extends Module {
    public static HurtcamModule INSTANCE;
    public final ExpandSetting local = new ExpandSetting(this, "Local");
    public final BooleanSetting localDisabled = new BooleanSetting(local, "Disabled", true);
    public final ExpandSetting modelColor = new ExpandSetting(this, "Model color");
    public final BooleanSetting modelColorEnabled = new BooleanSetting(modelColor, "Enabled", false);
    public final ColorSetting modelColorColor = new ColorSetting(modelColor, "Color", new java.awt.Color(0xff435ad1, true), () -> !modelColorEnabled.isEnabled());
    public final BooleanSetting armor = new BooleanSetting(this, "Armor", true);
    public HurtcamModule() { super("Hurtcam", Category.RENDER); INSTANCE = this; }
}
