package com.adjustclient.core.gui.screen.transition;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import java.awt.Color;

public final class DimTransition extends ScreenTransition {
    public Animation animation;
    public Object field8;

    public DimTransition() { this(new Animation(Easing.LINEAR, 100L)); }
    public DimTransition(Animation animation) { this.animation = animation; }
    public void draw(Object window, float target) {
        float alpha = animation.update(target);
        int opacity = (int) alpha;
        if (opacity == Integer.MIN_VALUE && alpha > 0.0F) opacity = ~opacity;
        int color = new Color(0, 0, 0, opacity + 20).getRGB();
        net.minecraft.client.gui.ScaledResolution resolution = (net.minecraft.client.gui.ScaledResolution) window;
        NvgDraw.fillRect(color, 0.0F, 0.0F, (float) resolution.getScaledWidth(), (float) resolution.getScaledHeight());
    }
}
