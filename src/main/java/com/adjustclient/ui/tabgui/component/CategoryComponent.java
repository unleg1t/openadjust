package com.adjustclient.ui.tabgui.component;

import com.adjustclient.module.Category;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import java.awt.Color;
import com.adjustclient.ui.tabgui.LabelNode;
import com.adjustclient.ui.tabgui.TabGui;

public final class CategoryComponent extends LabelNode {

    private final Category category;

    public CategoryComponent(Category category) {
        super("Undefined");
        this.category = category;
    }

    @Override
    public String getLabel() {
        return this.category.getName();
    }

    @Override
    public int getTextColor(boolean hovered, boolean active) {
        if (TabGui.layingOutNodes) {
            return -1;
        }
        float target = hovered ? 1.0F : 0.0F;
        if (this.hoverAnimation == null || System.currentTimeMillis() - this.hoverAnimation.lastCall >= 501L) {
            this.hoverAnimation = new Animation(Easing.LINEAR, 100L);
            this.hoverAnimation.value = target;
            this.hoverAnimation.restart();      // restart: startTime = now, start = value, finished = false
        }
        int c = (int) (120.0F * this.hoverAnimation.update(target) + 120.0F);
        return new Color(c, c, c).getRGB();
    }
}
