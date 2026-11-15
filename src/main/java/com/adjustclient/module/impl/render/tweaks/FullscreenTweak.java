package com.adjustclient.module.impl.render.tweaks;

import com.adjustclient.setting.BooleanSetting;
import com.google.common.util.concurrent.ListenableFutureTask;
import java.util.concurrent.Callable;

public final class FullscreenTweak extends Tweak {
    public static FullscreenTweak INSTANCE;
    public final BooleanSetting borderlessFullscreen;
    public Object field12;
    private static final Callable<Void> REFRESH = () -> {
        if (mc.fullscreen && com.adjustclient.util.ClipboardUtil.sharedWindowHandle != -1L && mc.loadingScreen != null) {
            mc.setFullscreen(true);
        }
        return null;
    };

    public FullscreenTweak(TweaksModule module) {
        super(module);
        INSTANCE = this;
        borderlessFullscreen = new BooleanSetting(module, "Borderless fullscreen", false);
        borderlessFullscreen.addListener((Runnable) () -> {
            synchronized (mc.scheduledTasks) {
                mc.scheduledTasks.add(ListenableFutureTask.create(REFRESH));
            }
        });
    }

    @Override public void onEnable() {
        synchronized (mc.scheduledTasks) {
            mc.scheduledTasks.add(ListenableFutureTask.create(REFRESH));
        }
    }

    @Override public void onDisable() {
        synchronized (mc.scheduledTasks) {
            mc.scheduledTasks.add(ListenableFutureTask.create(REFRESH));
        }
    }

    @Override public boolean enabledSetting() { return module.isEnabled() && borderlessFullscreen.isEnabled(); }
}
