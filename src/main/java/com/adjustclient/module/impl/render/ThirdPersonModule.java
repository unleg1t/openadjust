package com.adjustclient.module.impl.render;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.bind.BindMode;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MouseDeltaEvent;
import com.adjustclient.event.PreClientTickEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.rotation.handler.RenderRotationTracker;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.KeybindSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.util.ScreenUtil;
import com.adjustclient.util.Vec2f;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import com.adjustclient.module.impl.render.freecam.FreecamModule;

public final class ThirdPersonModule extends Module {
    public static ThirdPersonModule INSTANCE;
    public final ExpandSetting freeLook;
    public final KeybindSetting freeLookBind;
    public final BooleanSetting freeLookHold;
    public final BooleanSetting freeLookInvertYaw;
    public final BooleanSetting freeLookInvertPitch;
    public final ExpandSetting viewClip;
    public final BooleanSetting viewClipEnabled;
    public final BooleanSetting viewClipSneakOnly;
    public final NumberSetting distance;
    public float yaw;
    public float pitch;
    public float previousYaw;
    public float previousPitch;
    public boolean freeLooking;

    public ThirdPersonModule() {
        super("Third Person", Category.RENDER);
        INSTANCE = this;
        freeLook = new ExpandSetting(this, "Free Look");
        freeLookHold = new BooleanSetting(freeLook, "Hold", true, "On", "Off");
        Bind cameraBind = Adjust.INSTANCE.bindManager.register("module.thirdperson.freelook", mode -> {
            if (mode == BindMode.RELEASED) {
                if (this.freeLookHold.isEnabled()) setFreeLooking(false);
            } else if (mode == BindMode.PRESSED && isEnabled() && mc.thePlayer != null && !ScreenUtil.isAnyScreenOpen()) {
                setFreeLooking(this.freeLookHold.isEnabled() || !freeLooking);
            }
        }, "Free Look");
        freeLookBind = new KeybindSetting(freeLook, "Bind", cameraBind);
        freeLook.enabledPredicate = () -> !this.freeLookBind.getValue().keys.isEmpty();
        freeLook.getSettings().remove(freeLookHold);
        freeLook.getSettings().add(freeLookHold);
        freeLookInvertYaw = new BooleanSetting(freeLook, "Invert yaw", false, "On", "Off");
        freeLookInvertPitch = new BooleanSetting(freeLook, "Invert pitch", false, "On", "Off");
        viewClip = new ExpandSetting(this, "View Clip");
        viewClip.enabledPredicate = new ThirdPersonViewClipCondition(this);
        viewClipEnabled = new BooleanSetting(viewClip, "Enabled", true, "Yes", "No");
        viewClipSneakOnly = new BooleanSetting(viewClip, "Sneak only", false, "On", "Off");
        distance = new NumberSetting(this, "Distance", 0.0, -2.0, 5.0, 0.25);
        distance.formatter(NumberSetting.METERS);
        setHidden(false);
    }

    public float getYaw() {
        var freecam = FreecamModule.INSTANCE;
        if (freecam.isEnabled() && freecam.camera != null)
            return freecam.camera.rotationYaw;
        if (freeLooking) return yaw;
        Entity entity = mc.getRenderViewEntity();
        if (mc.renderingFrame && mc.cameraOverride != null) entity = mc.cameraOverride;
        if (entity == null) return 0.0F;
        Vec2f rotation = RenderRotationTracker.INSTANCE.rotation;
        return entity instanceof EntityPlayerSP && rotation != null ? rotation.x : entity.rotationYaw;
    }

    public void setFreeLooking(boolean active) {
        if (freeLooking == active) return;
        freeLooking = active;
        if (active) {
            Vec2f rotation = RenderRotationTracker.INSTANCE.rotation;
            previousYaw = yaw = rotation == null ? mc.thePlayer.rotationYaw : rotation.x;
            previousPitch = pitch = rotation == null ? mc.thePlayer.rotationPitch : rotation.y;
            Entity camera = mc.getRenderViewEntity();
            if (mc.renderingFrame && mc.cameraOverride != null) camera = mc.cameraOverride;
            mc.entityRenderer.loadEntityShader(camera);
        } else {
            mc.entityRenderer.loadEntityShader(null);
        }
        mc.renderGlobal.displayListEntitiesDirty = true;
    }

    @EventHandler(priority = 100) public void onMouseInput(MouseDeltaEvent event) {
        if (!isEnabled() || !freeLooking) return;
        float dx = event.sensitivity * event.deltaX;
        float dy = event.sensitivity * event.deltaY;
        if (freeLookInvertYaw.isEnabled()) dx = -dx;
        if (!freeLookInvertPitch.isEnabled()) dy = -dy;
        float oldYaw = yaw;
        float oldPitch = pitch;
        yaw += (float) (dx * 0.15);
        pitch = MathHelper.clamp_float(pitch + (float) (dy * 0.15), -90.0F, 90.0F);
        previousPitch += pitch - oldPitch;
        previousYaw += yaw - oldYaw;
        event.deltaX = 0.0F;
        event.deltaY = 0.0F;
    }

    @EventHandler(priority = 250) public void onGameLoop(PreClientTickEvent event) {
        if (!isEnabled()) return;
        if (mc.thePlayer == null || ScreenUtil.isAnyScreenOpen()) setFreeLooking(false);
        else if (this.freeLookHold.isEnabled()) setFreeLooking(freeLookBind.isDown());
        if (freeLooking) mc.renderGlobal.displayListEntitiesDirty = true;
    }

    @Override public void onDisable() { setFreeLooking(false); }

    public boolean isViewClipEnabled() {
        return isEnabled() && viewClipEnabled.isEnabled()
                && (!viewClipSneakOnly.isEnabled() || mc.gameSettings.keyBindSneak.isKeyDown());
    }
}
