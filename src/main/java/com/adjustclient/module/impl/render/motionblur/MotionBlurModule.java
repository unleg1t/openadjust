package com.adjustclient.module.impl.render.motionblur;

import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.setting.NumberSetting;

public final class MotionBlurModule extends Module {
    public static MotionBlurModule INSTANCE;
    public final ModeSetting<MotionBlurMode> mode;
    public final NumberSetting strength;
    public final MotionBlurRenderer renderer;
    public MotionBlurModule() {
        super("Motion Blur", Category.RENDER);
        INSTANCE = this;
        mode = new ModeSetting<>(this, "Mode", MotionBlurMode.PHOSPHOR, MotionBlurMode.values());
        strength = new NumberSetting(this, "Strength", 1, 1, 10, 0.1);
        renderer = new MotionBlurRenderer(this);
    }
    @Override public void onEnable() {
        renderer.destroy();
        renderer.lastFramebuffer = null;
        renderer.lastWorld = null;
        renderer.lastCamera = null;
        renderer.lastTime = 0;
        renderer.active = false;
        renderer.failed = false;
    }
    @Override public void onDisable() {
        renderer.destroy();
        renderer.lastFramebuffer = null;
        renderer.lastWorld = null;
        renderer.lastCamera = null;
        renderer.lastTime = 0;
        renderer.active = false;
        renderer.failed = false;
    }
    public float getStrength() { return strength.getFloat(); }
}
