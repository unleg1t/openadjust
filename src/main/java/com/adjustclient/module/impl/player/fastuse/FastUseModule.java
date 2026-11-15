package com.adjustclient.module.impl.player.fastuse;

import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.player.fastuse.feature.AutomationFeature;
import com.adjustclient.module.impl.player.fastuse.feature.BlockBreakingFeature;
import com.adjustclient.module.impl.player.fastuse.feature.EatingFeature;
import com.adjustclient.setting.ModeOption;

public final class FastUseModule extends Module {
    public static FastUseModule INSTANCE;
    public final BlockBreakingFeature blockBreaking;
    public final AutomationFeature automation;
    public final EatingFeature eating;
    public final FastUseFeature[] features;

    public FastUseModule() {
        super("Fast Use", Category.PLAYER);
        INSTANCE = this;
        blockBreaking = new BlockBreakingFeature(this);
        automation = new AutomationFeature(this);
        eating = new EatingFeature(this);
        features = new FastUseFeature[] {blockBreaking, automation, eating};
    }

    @Override public void onEnable() {
        for (FastUseFeature feature : features) feature.onEnable();
    }

    @Override public void onDisable() {
        for (FastUseFeature feature : features) feature.onDisable();
    }

    public enum EatingMode implements ModeOption {
        VANILLA("Vanilla"), PINGLESS("Pingless");
        private final String displayName;
        EatingMode(String displayName) { this.displayName = displayName; }
        @Override public String getName() { return displayName; }
    }
}
