package com.adjustclient.modules.impl.player.fastuse;

import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.modules.impl.player.fastuse.feature.AutomationFeature;
import com.adjustclient.modules.impl.player.fastuse.feature.BlockBreakingFeature;
import com.adjustclient.modules.impl.player.fastuse.feature.EatingFeature;
import com.adjustclient.core.settings.ModeOption;

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
