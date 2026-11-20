package com.adjustclient.modules.impl.render.tweaks;

import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;

public final class NoRenderTweak extends Tweak {
    public static NoRenderTweak INSTANCE;
    public final BooleanSetting title;
    public final BooleanSetting bossBar;
    public final BooleanSetting achievementNotifications;

    public NoRenderTweak(TweaksModule module) {
        super(module);
        INSTANCE = this;
        ExpandSetting group = new ExpandSetting(module, "No render");
        title = new BooleanSetting(group, "Title", false);
        bossBar = new BooleanSetting(group, "Boss bar", false);
        achievementNotifications = new BooleanSetting(group, "Achievement notifications", true);
    }

    public boolean hideTitle() {
        return isEnabled() && title.isEnabled();
    }

    public boolean hideBossBar() {
        return isEnabled() && bossBar.isEnabled();
    }

    public boolean hideAchievements() {
        return isEnabled() && achievementNotifications.isEnabled();
    }

    @Override
    protected boolean enabledSetting() {
        return true;
    }
}
