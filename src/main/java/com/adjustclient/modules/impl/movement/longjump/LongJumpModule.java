package com.adjustclient.modules.impl.movement.longjump;

import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.core.settings.ModeSetting;
import com.adjustclient.utilities.MinecraftInstance;

public final class LongJumpModule extends Module implements MinecraftInstance {
    public static LongJumpModule INSTANCE;
    public final ModeSetting<LongJumpMode> mode;

    public LongJumpModule() {
        super("Long Jump", Category.MOVEMENT);
        INSTANCE = this;
        notifyToggle = false;
        VanillaLongJumpMode vanilla = new VanillaLongJumpMode(this);
        HypixelLongJumpMode hypixel = new HypixelLongJumpMode(this);
        RewoundLongJumpMode rewound = new RewoundLongJumpMode(this);
        mode = new ModeSetting<>(this, "Mode", vanilla, vanilla, hypixel, rewound);
        mode.addListener((previous, next) -> onModeChanged(previous, next));
        settings.remove(mode);
        settings.add(0, mode);
    }

    @Override public boolean canToggle(boolean enabling) { return !enabling || mc.thePlayer != null; }

    public void onModeChanged(ModeOption previous, ModeOption next) {
        if (!isEnabled() || previous == next) return;
        if (previous instanceof LongJumpMode) ((LongJumpMode) previous).onDisable();
        if (next instanceof LongJumpMode) ((LongJumpMode) next).onEnable();
    }

    @Override public void onEnable() {
        mode.getValue().onEnable();
    }

    @Override public void onDisable() {
        mode.getValue().onDisable();
    }

    @Override public String getSuffix() { return mode.getValue().getName(); }
}
