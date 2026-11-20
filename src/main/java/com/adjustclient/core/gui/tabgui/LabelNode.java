package com.adjustclient.core.gui.tabgui;

import com.adjustclient.core.rendering.NvgRenderer;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import java.awt.Color;
import com.adjustclient.core.rendering.font.Fonts;

public abstract class LabelNode extends GuiNode {

    /** The text of the row. Written directly by the subclass constructors (and by e.g. the keybind rows to change the text). */
    protected String label;
    /** Animation (100 ms) behind the text colour fade. */
    protected Animation hoverAnimation;

    protected LabelNode() { }
    protected LabelNode(String label) { this.label = label; }

    public String getLabel() {
        return this.label;
    }

    public int getTextColor(boolean hovered, boolean active) {
        if (TabGui.layingOutNodes) {
            return -1;
        }
        float target = active ? 1.0F : (hovered ? 0.56F : 0.0F);
        if (this.hoverAnimation == null || System.currentTimeMillis() - this.hoverAnimation.lastCall /* lastCall */ >= 501L) {
            this.hoverAnimation = new Animation(Easing.LINEAR, 100L);
            this.hoverAnimation.value = target;
            this.hoverAnimation.restart();          // restart: startTime = now, start = value, finished = false
        }
        int c = (int) (120.0F * this.hoverAnimation.update(target) + 120.0F);
        return new Color(c, c, c).getRGB();
    }

    @Override
    public void render(NvgRenderer dl, Object context, boolean hovered, float x, float y, float width) {
        int color = this.getTextColor(hovered, false);
        dl.color(color);
        dl.text(Fonts.TAHOMA_10, this.getLabel(), x + 2.0F, y + 3.0F);
    }
}
