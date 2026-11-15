package com.adjustclient.module.impl.movement.longjump;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.GameLoopEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.util.MinecraftInstance;

public final class LongJumpModule extends Module implements MinecraftInstance {
    public static LongJumpModule INSTANCE;
    public final ModeSetting<LongJumpMode> mode;

    public LongJumpModule() {
        super("Long Jump", Category.MOVEMENT);
        INSTANCE = this;
        notifyToggle = false;
        VanillaLongJumpMode vanilla = new VanillaLongJumpMode(this);
        HypixelLongJumpMode hypixel = new HypixelLongJumpMode(this);
        mode = new ModeSetting<>(this, "Mode", vanilla, vanilla, hypixel);
        mode.addListener((previous, next) -> onModeChanged(previous, next));
        settings.remove(mode);
        settings.add(0, mode);
    }

    @Override public boolean canToggle(boolean enabling) { return !enabling || mc.thePlayer != null; }

    public void onModeChanged(com.adjustclient.setting.ModeOption previous, com.adjustclient.setting.ModeOption next) {
        if (!isEnabled() || previous == next) return;
        if (previous instanceof HypixelLongJumpMode) ((HypixelLongJumpMode) previous).onDisable();
        else if (previous instanceof VanillaLongJumpMode) ((VanillaLongJumpMode) previous).onDisable();
        if (next instanceof HypixelLongJumpMode) ((HypixelLongJumpMode) next).onEnable();
        else if (next instanceof VanillaLongJumpMode) ((VanillaLongJumpMode) next).onEnable();
    }

    @Override public void onEnable() {
        LongJumpMode selected = mode.getValue();
        if (selected instanceof HypixelLongJumpMode) selected.onEnable();
        else if (selected instanceof VanillaLongJumpMode) selected.onEnable();
    }

    @Override public void onDisable() {
        LongJumpMode selected = mode.getValue();
        if (selected instanceof HypixelLongJumpMode) selected.onDisable();
        else if (selected instanceof VanillaLongJumpMode) selected.onDisable();
    }

    @Override public String getSuffix() { return mode.getValue().getName(); }
}
