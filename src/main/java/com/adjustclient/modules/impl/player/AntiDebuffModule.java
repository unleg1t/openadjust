package com.adjustclient.modules.impl.player;

import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.BooleanSetting;

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
