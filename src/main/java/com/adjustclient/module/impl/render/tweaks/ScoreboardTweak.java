package com.adjustclient.module.impl.render.tweaks;

import com.adjustclient.setting.BooleanSetting;

public final class ScoreboardTweak extends Tweak {
    public static ScoreboardTweak INSTANCE;
    public final BooleanSetting enabled;
    public final BooleanSetting noScoreboard;
    public final BooleanSetting transparent;
    public final BooleanSetting removeScores;
    private Object field24;

    public ScoreboardTweak(TweaksModule module) {
        super(module);
        INSTANCE = this;
        com.adjustclient.setting.ExpandSetting group = new com.adjustclient.setting.ExpandSetting(module, "Scoreboard");
        group.enabledPredicate = this::thirdPredicate;
        enabled = new BooleanSetting(group, "Enabled", true, "Yes", "No");
        noScoreboard = new BooleanSetting(group, "No scoreboard", false);
        removeScores = new BooleanSetting(group, "Remove scores", true, this::firstPredicate);
        transparent = new BooleanSetting(group, "Transparent", false, this::secondPredicate);
    }
    @Override public boolean enabledSetting() { return module.isEnabled() && enabled.isEnabled(); }
    private boolean firstPredicate() { return noScoreboard.isEnabled(); }
    private boolean secondPredicate() { return noScoreboard.isEnabled(); }
    private boolean thirdPredicate() { return enabled.isEnabled(); }
}
