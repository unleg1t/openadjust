package com.adjustclient.module.impl.combat.backtrack;

import com.adjustclient.module.Module;
import com.adjustclient.setting.*;

public final class LagBreakSettings {
    public final NumberSetting maximumTime;
    public final NumberSetting maximumTargets;
    public final NumberSetting rangeRequirement;
    public final NumberSetting rangeTimeout;
    public BooleanSetting breakInRangeEnabled;
    public final BooleanSetting targetsPlayers;
    public final BooleanSetting targetsTeammates;
    public final BooleanSetting targetsInvisibles;
    public final BooleanSetting hud;
    public LagBreakSettings(Module module) {
        maximumTime = new NumberSetting(module, "Maximum time", 500, 100, 1000, 50).formatter(NumberSetting.MILLISECONDS);
        maximumTargets = new NumberSetting(module, "Maximum targets", 4, 1, 12, 1);
        maximumTargets.setDescription("The module won't function if there are more targets than the specified amount in range.");
        ExpandSetting range = new ExpandSetting(module, "Range");
        rangeRequirement = new NumberSetting(range, "Requirement", 6.5, 3.5, 8.0, 0.1).formatter(NumberSetting.METERS);
        rangeTimeout = new NumberSetting(range, "Timeout", 3.5, 3.0, 8.0, 0.1).formatter(NumberSetting.METERS);
        ExpandSetting breakInRange = new ExpandSetting(module, "Break in range");
        breakInRange.enabledPredicate = () -> breakInRangeEnabled.isEnabled();
        breakInRangeEnabled = new BooleanSetting(breakInRange, "Enabled", true, "Yes", "No");
        ExpandSetting targets = new ExpandSetting(module, "Targets");
        targetsPlayers = new BooleanSetting(targets, "Players", true, "On", "Off");
        targetsTeammates = new BooleanSetting(targets, "Teammates", false, "On", "Off");
        targetsInvisibles = new BooleanSetting(targets, "Invisibles", true, "On", "Off");
        hud = new BooleanSetting(module, "HUD", true, "On", "Off");
    }
    public double getTimeoutRange() { return rangeTimeout.getDouble(); }
    public int getMaximumTime() { return maximumTime.getInt(); }
    public int getMaximumTargets() { return maximumTargets.getInt(); }
    public int getTargetFlags() {
        boolean players = targetsPlayers.isEnabled();
        boolean teammates = targetsTeammates.isEnabled();
        boolean invisibles = targetsInvisibles.isEnabled();
        return (players ? 2 : 0) | (teammates ? 32 : 0) | (invisibles ? 64 : 0);
    }
}
