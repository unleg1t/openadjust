package com.adjustclient.module.impl.render;

import com.adjustclient.event.CancellablePlayerUpdateEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.Render2DEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.ColorUtil;
import com.adjustclient.render.NvgRenderer;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.setting.*;
import java.awt.Color;
import net.minecraft.client.gui.ScaledResolution;

public final class CrosshairModule extends Module {
    public static CrosshairModule INSTANCE;
    public final ModeSetting<CrosshairMode> mode;
    public final ColorSetting color;
    public final ExpandSetting inactivityFade;
    public final BooleanSetting inactivityFadeEnabled;
    public final NumberSetting inactivityAlpha;
    public final NumberSetting inactivityRequirement;
    public final ExpandSetting gap;
    public final NumberSetting movingGap;
    public final NumberSetting normalGap;
    public final NumberSetting width;
    public final NumberSetting thickness;
    public final BooleanSetting dot;
    public final BooleanSetting outline;
    public final Animation gapAnimation;
    public final Animation alphaAnimation;
    public final NvgRenderer renderer;
    public int inactiveSeconds;

    public CrosshairModule() {
        super("Crosshair", Category.RENDER);
        INSTANCE = this;
        setHidden(false);
        mode = new ModeSetting<>(this, "Mode", CrosshairMode.GAPPED, CrosshairMode.values());
        color = new ColorSetting(this, "Color", new Color(0xff435ad1, true), false, false, ColorSetting.Mode.PLAIN, null);
        inactivityFade = new ExpandSetting(this, "Inactivity Fade");
        inactivityFade.enabledPredicate = new CrosshairFadeCondition(this);
        inactivityFadeEnabled = new BooleanSetting(inactivityFade, "Enabled", false, "Yes", "No");
        inactivityAlpha = new NumberSetting(inactivityFade, "Alpha", 100, 0, 255, 1);
        inactivityRequirement = new NumberSetting(inactivityFade, "Requirement", 10, 5, 60, 5)
                .formatter(NumberSetting.TICKS);
        gap = new ExpandSetting(this, "Gap", () -> mode.getValue() != CrosshairMode.GAPPED);
        movingGap = new NumberSetting(gap, "Moving", 2.5F, 1.5F, 4.0F, 0.5F);
        normalGap = new NumberSetting(gap, "Normal", 1.5F, 1.5F, 3.0F, 0.5F);
        width = new NumberSetting(this, "Width", 3.0F, 1.0F, 5.0F, 0.5F,
                () -> mode.getValue() == CrosshairMode.DOT);
        thickness = new NumberSetting(this, "Thickness", 1.0F, 0.5F, 1.0F, 0.5F);
        dot = new BooleanSetting(this, "Dot", true, "On", "Off",
                () -> mode.getValue() != CrosshairMode.GAPPED);
        outline = new BooleanSetting(this, "Outline", true);
        gapAnimation = new Animation(Easing.EASE_OUT_SINE, 100);
        alphaAnimation = new Animation(Easing.LINEAR, 125);
        renderer = new NvgRenderer();
    }

    @EventHandler public void onRender2D(Render2DEvent event) {
        if (!isEnabled() || !mc.ingameGUI.showCrosshair()) return;
        double guiScale = mc.gameSettings.guiScale;
        double hudScale = HUDModule.getHudScale();
        ScaledResolution resolution = event.scaledResolution;
        if (hudScale != guiScale) {
            resolution = new ScaledResolution(mc);
            NvgContext.setScale(2.0f);
        }
        renderer.clear();
        float length = width.getFloat();
        float thick = thickness.getFloat();
        float alpha = inactivityFadeEnabled.isEnabled() && inactiveSeconds >= inactivityRequirement.getLong()
                ? inactivityAlpha.getFloat() : 255.0f;
        alphaAnimation.update(alpha);
        gapAnimation.update(getGap());
        float animatedGap = gapAnimation.value;
        int opacity = Math.max(0, Math.min(255, (int) alphaAnimation.value));
        int edge = outline.isEnabled() ? new Color(19, 19, 19, opacity).getRGB() : new Color(0, 0, 0, 0).getRGB();
        int foreground = ColorUtil.withAlpha(color.getColor(), opacity).getRGB();
        float x = (float) (resolution.getScaledWidth_double() / 2.0);
        float y = (float) (resolution.getScaledHeight_double() / 2.0);
        float left = x - 0.5f;
        switch (mode.getValue()) {
            case DOT:
                renderer.color = edge;
                renderer.strokeRect(false, x - 1.0f, y - 0.5f, thick + 1.0f, thick + 1.0f, 0.5f);
                renderer.color = foreground;
                renderer.rect(left, y, thick, thick);
                break;
            case GAPPED:
                float start = x - length - animatedGap - 0.5f;
                renderer.color = edge; renderer.strokeRect(true, start, y, length, thick, 0.5f);
                renderer.color = foreground; renderer.rect(start, y, length, thick);
                start = y - animatedGap - length;
                renderer.color = edge; renderer.strokeRect(true, left, start, thick, length, 0.5f);
                renderer.color = foreground; renderer.rect(left, start, thick, length);
                start = x + animatedGap + thick - 0.5f;
                renderer.color = edge; renderer.strokeRect(true, start, y, length, thick, 0.5f);
                renderer.color = foreground; renderer.rect(start, y, length, thick);
                start = y + animatedGap + thick;
                renderer.color = edge; renderer.strokeRect(true, left, start, thick, length, 0.5f);
                renderer.color = foreground; renderer.rect(left, start, thick, length);
                if (dot.isEnabled()) {
                    renderer.color = edge; renderer.strokeRect(false, x - 1.0f, y - 0.5f, thick + 1.0f, thick + 1.0f, 0.5f);
                    renderer.color = foreground; renderer.rect(left, y, thick, thick);
                }
                break;
            case CROSS:
                float startX = x - length - 0.5f;
                float outerX = startX - 0.5f;
                renderer.color = edge;
                renderer.rect(outerX, y, 0.5f, thick);
                renderer.rect(outerX, y - 0.5f, length, 0.5f);
                renderer.rect(outerX, y + thick, length, 0.5f);
                renderer.color = foreground; renderer.rect(startX, y, length, thick);
                renderer.color = edge;
                float top = y - length;
                renderer.rect(left, top - 0.5f, thick, 0.5f);
                renderer.rect(x - 1.0f, top - 0.5f, 0.5f, length + 0.5f);
                renderer.rect(x + thick - 0.5f, top - 0.5f, 0.5f, length);
                renderer.color = foreground; renderer.rect(left, top, thick, length + 1.0f);
                renderer.color = edge;
                float right = x + thick - 0.5f;
                renderer.rect(right, y - 0.5f, length, 0.5f);
                renderer.rect(right + length, y - 0.5f, 0.5f, thick + 1.0f);
                renderer.rect(right, y + thick, length, 0.5f);
                renderer.color = foreground; renderer.rect(right, y, length, thick);
                renderer.color = edge;
                renderer.rect(x - 1.0f, y + thick, 0.5f, length);
                renderer.rect(x - 1.0f, y + thick + length, thick + 0.5f, 0.5f);
                renderer.rect(x + thick - 0.5f, y + thick, 0.5f, length + 0.5f);
                renderer.color = foreground; renderer.rect(left, y + thick, thick, length);
                break;
        }
        renderer.flush(resolution);
        if (hudScale != guiScale) {
            mc.entityRenderer.setupOverlayRendering(event.scaledResolution);
            NvgContext.setScale((float) event.scaledResolution.getScaleFactor_double());
        }
    }

    @Override public void onEnable() {
        gapAnimation.value = getGap();
        inactiveSeconds = 0;
    }

    @EventHandler public void onTick(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        if (isStationary()) ++inactiveSeconds;
        else inactiveSeconds = 0;
    }

    public static boolean isStationary() {
        return mc.thePlayer == null || mc.thePlayer.movementInput == null
                || (mc.thePlayer.movementInput.moveForward == 0 && mc.thePlayer.movementInput.moveStrafe == 0)
                || Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ) == 0;
    }

    public float getGap() {
        if (!isStationary() && Math.max(0, Math.min(255, (int) alphaAnimation.value)) >= 85) return movingGap.getFloat();
        return normalGap.getFloat();
    }
}
