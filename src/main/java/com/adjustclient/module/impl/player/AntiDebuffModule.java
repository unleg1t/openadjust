package com.adjustclient.module.impl.player;

import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.BooleanSetting;

public final class AntiDebuffModule extends Module {
    public static AntiDebuffModule INSTANCE;
    public final BooleanSetting blindnessEffect;
    public final BooleanSetting nauseaEffect;
    public final BooleanSetting pumpkinOverlay;
    public final BooleanSetting portalOverlay;
    public AntiDebuffModule() {
        super("Anti Debuff", Category.PLAYER);
        INSTANCE = this;
        blindnessEffect = new BooleanSetting(this, "Blindness effect", true);
        nauseaEffect = new BooleanSetting(this, "Nausea effect", true);
        pumpkinOverlay = new BooleanSetting(this, "Pumpkin overlay", true);
        portalOverlay = new BooleanSetting(this, "Portal overlay", true);
    }
}
