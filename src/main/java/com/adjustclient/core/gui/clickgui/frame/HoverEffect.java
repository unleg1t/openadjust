package com.adjustclient.core.gui.clickgui.frame;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import java.awt.Color;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.gui.clickgui.ClickGuiComponent;
import com.adjustclient.utilities.Vec2f;

public class HoverEffect extends ElementDecorator {

    protected ClickGuiComponent owner;
    protected Animation animation;
    protected BooleanSupplier active;
    protected BooleanSupplier hovered;
    protected float hoverValue = 1.0F;
    protected float activeValue = 0.8F;
    protected Object unused28;

    public HoverEffect(ClickGuiComponent owner, BooleanSupplier active, BooleanSupplier hovered) {
        this.owner = owner;
        this.animation = null;
        this.active = active;
        this.hovered = hovered;
        this.unused28 = null;
    }

    public HoverEffect(ClickGuiComponent owner, BooleanSupplier active, BooleanSupplier hovered, float hoverValue, float activeValue) {
        this(owner, active, hovered);
        this.hoverValue = hoverValue;
        this.activeValue = activeValue;
    }

    public static BooleanSupplier hoveredOf(ClickGuiComponent owner) {
        return () -> owner.isHovered();
    }

    @Override
    public void render(ScaledResolution window, Render2DEvent queue, Object drawList, Vec2f mouse, boolean beforeChildren) {
        float delta = Math.max(0.0F, this.activeValue - this.hoverValue);
        float target = this.hovered.getAsBoolean() ? this.hoverValue : 0.0F;
        if (this.active.getAsBoolean()) {
            target = delta + target;
        }
        if (this.animation == null) {
            this.animation = new Animation(Easing.LINEAR, 95L);
            this.animation.duration = 0L;      // snap on creation
            this.render(window, queue, drawList, mouse, beforeChildren);
            this.animation.duration = 95L;
        }
        this.animation.update(target);
    }

    @Override
    public void reset() {
        this.animation = null;
    }

    public Animation k() {
        if (this.animation != null) {
            return this.animation;
        }
        this.animation = new Animation(Easing.LINEAR, 95L);
        this.animation.duration = 0L;
        this.render(null, null, null, null, true);
        this.animation.duration = 95L;
        return this.animation;
    }

    public int color(int base) {
        float v = this.k().value;
        int channel = MathHelper.clamp_int((int) (110.0F * v + (float) base), 0, 255);
        int alpha = MathHelper.clamp_int((int) (48.0F * v + (float) (base + 10)), 0, 255);
        return new Color(channel, channel, channel, alpha).getRGB();
    }

    public int withAlpha(Color base) {
        this.k();
        float v = this.animation.value;
        int alpha = MathHelper.clamp_int((int) (255.0F * v), 0, 255);
        int rgb = base.getRGB();
        return alpha == (rgb >>> 24) ? rgb : (alpha << 24) | (rgb & 0xFFFFFF);
    }

    public int tint(int red) {
        int[] channels = {red, 155, 155};
        float v = this.k().value;
        for (int i = 0; i < 3; i++) {
            channels[i] = MathHelper.clamp_int((int) (110.0F * v + (float) channels[i]), 0, 255);
        }
        int alpha = MathHelper.clamp_int((int) (48.0F * v + 165.0F), 0, 255);
        return new Color(channels[0], channels[1], channels[2], alpha).getRGB();
    }

    public int accent(int maxAlpha) {
        Color accent = HUDModule.getHudColor();
        float v = this.k().value;
        int alpha = MathHelper.clamp_int((int) ((float) maxAlpha * v), 0, 255);
        int rgb = accent.getRGB();
        return alpha == (rgb >>> 24) ? rgb : (alpha << 24) | (rgb & 0xFFFFFF);
    }
}
