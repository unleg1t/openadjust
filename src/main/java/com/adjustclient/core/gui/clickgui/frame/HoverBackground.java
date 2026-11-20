package com.adjustclient.core.gui.clickgui.frame;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import java.awt.Color;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.core.gui.clickgui.ClickGuiComponent;
import com.adjustclient.core.gui.screen.AdjustComponent;
import com.adjustclient.utilities.Vec2f;

public class HoverBackground extends ElementDecorator {

    protected ClickGuiComponent owner;
    public Animation animation;
    protected Object unused12;

    public HoverBackground(ClickGuiComponent owner) {
        this.owner = owner;
        this.unused12 = null;
    }

    @Override
    public void render(ScaledResolution window, Render2DEvent queue, Object drawList, Vec2f mouse, boolean beforeChildren) {
        // (the body ignores all arguments; the phase is BEFORE_CHILDREN so it is called once per frame)
        boolean hovered = this.owner.isHovered();
        if (hovered && this.animation == null) {
            this.animation = new Animation(Easing.LINEAR, 0L);
        }
        if (!hovered && this.animation == null) {
            return;
        }
        this.animation.duration = hovered ? 0L : 60L;                // duration in ms: instant in, 60 ms out
        float value = this.animation.update(this.owner.isHovered() ? 1.0F : 0.0F);
        AdjustComponent bounds = this.owner.bounds;
        float x1 = bounds.x;
        float y1 = this.owner.getY();
        float x2 = bounds.x + bounds.width;
        float y2 = y1 + this.owner.height;                        // y + owner height
        int alpha = MathHelper.clamp_int((int) (value * 69.0F), 0, 255);
        NvgDraw.fillRect(new Color(53, 53, 53, alpha).getRGB(), x1, y1, x2, y2);
        if (this.animation.value != 0.0F) {                      // still visible
            return;
        }
        if (!hovered) {
            this.animation = null;
        }
    }

    @Override
    public void reset() {
        this.animation = null;
    }
}
