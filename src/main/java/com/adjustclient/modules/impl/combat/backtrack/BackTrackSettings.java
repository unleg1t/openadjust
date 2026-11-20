package com.adjustclient.modules.impl.combat.backtrack;

import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.*;
import java.awt.Color;

public final class BackTrackSettings {
    public final NumberSetting maximumTime;
    public final NumberSetting rangeTimeout;
    public final NumberSetting rangeMaximum;
    public final BooleanSetting targetsPlayers;
    public final BooleanSetting targetsTeammates;
    public final BooleanSetting targetsInvisibles;
    public final BooleanSetting disableOnKnockback;
    public final BooleanSetting renderRealPosition;
    public final ColorSetting renderColor;
    public BackTrackSettings(Module module) {
        maximumTime = new NumberSetting(module, "Maximum time", 500, 100, 5000, 50).formatter(NumberSetting.MILLISECONDS);
        ExpandSetting range = new ExpandSetting(module, "Range");
        rangeTimeout = new NumberSetting(range, "Timeout", 2.0, 1.0, 4.0, 0.1).formatter(NumberSetting.METERS);
        rangeMaximum = new NumberSetting(range, "Maximum", 6.0, 3.1, 8.0, 0.1).formatter(NumberSetting.METERS);
        ExpandSetting targets = new ExpandSetting(module, "Targets");
        targetsPlayers = new BooleanSetting(targets, "Players", true, "On", "Off");
        targetsTeammates = new BooleanSetting(targets, "Teammates", false, "On", "Off");
        targetsInvisibles = new BooleanSetting(targets, "Invisibles", true, "On", "Off");
        ExpandSetting disableOn = new ExpandSetting(module, "Disable on");
        disableOnKnockback = new BooleanSetting(disableOn, "Knockback", false, "On", "Off");
        ExpandSetting render = new ExpandSetting(module, "Render");
        renderRealPosition = new BooleanSetting(render, "Real position", true, "On", "Off");
        renderColor = new ColorSetting(render, "Color", new Color(0xff435ad1, true), false, false,
                ColorSetting.Mode.PLAIN, () -> !renderRealPosition.isEnabled());
    }
    public double getTimeoutRange() { return rangeTimeout.getDouble(); }
    public int getTargetFlags() {
        boolean players = targetsPlayers.isEnabled();
        boolean teammates = targetsTeammates.isEnabled();
        boolean invisibles = targetsInvisibles.isEnabled();
        return (players ? 2 : 0) | (teammates ? 32 : 0) | (invisibles ? 64 : 0);
    }
}
