package com.adjustclient.module.impl.combat.killaura.target.priority;

import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.anticheat.entity.TrackedLivingEntity;

public abstract class TargetPriority implements ModeOption, MinecraftInstance {
    protected final String name;
    protected TargetPriority(String name) { this.name = name; }
    public boolean usesSecondaryPriority() { return true; }
    @Override public String getName() { return name; }
    public abstract double score(TrackedLivingEntity target);
}
