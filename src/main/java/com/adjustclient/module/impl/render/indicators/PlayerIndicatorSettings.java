package com.adjustclient.module.impl.render.indicators;

import com.adjustclient.module.Module;
import com.adjustclient.setting.BooleanSetting;

public class PlayerIndicatorSettings extends LivingIndicatorSettings {
    public final BooleanSetting teamColor;
    public final BooleanSetting nameTeamColor;

    public PlayerIndicatorSettings(Module parent, String label, boolean enabled, java.awt.Color defaultColor) {
        super(parent, label, enabled, defaultColor);
        teamColor = new BooleanSetting(group, "Team color", false);
        color.hideIf = () -> teamColor.isEnabled();
        nameTeamColor = new BooleanSetting(name, "Team color", false);
    }
}
