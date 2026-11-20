package com.adjustclient.modules.impl.combat.killaura.target.priority;

import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.anticheat.entity.TrackedLivingEntity;

public abstract class TargetPriority implements ModeOption, MinecraftInstance {
    protected final String name;
    protected TargetPriority(String name) { this.name = name; }
    public boolean usesSecondaryPriority() { return true; }
    @Override public String getName() { return name; }
    public abstract double score(TrackedLivingEntity target);
}
