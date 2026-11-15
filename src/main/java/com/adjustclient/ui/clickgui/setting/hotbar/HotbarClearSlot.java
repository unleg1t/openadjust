package com.adjustclient.ui.clickgui.setting.hotbar;

import com.adjustclient.render.font.Fonts;
import com.adjustclient.ui.clickgui.ClickGuiComponent;

public final class HotbarClearSlot extends HotbarSlot {

    HotbarClearSlot(ClickGuiComponent owner) {
        super(owner, 9);
    }

    @Override
    public void drawContent(com.adjustclient.event.Render2DEvent queue) {
        float anim = this.colorBehaviour.k().value;
        int a = (int) (25.0F * anim) + 50;
        if (anim > 1.0F) {
            a = 75;
        }
        float cx = this.width / 2.0F + this.x;
        float cy = (this.height / 2.0F + this.y) - 2.0F;
        double t = 0.35F * anim + 0.55F;
        t = Math.max(0.0, Math.min(1.0, t));
        double inv = 1.0 - t;
        int red = (int) (inv * 195.0 + t * 195.0);
        int green = (int) (inv * 195.0 + t * 40.0);
        int blue = (int) (inv * 195.0 + t * 54.0);
        int alpha = (int) (inv * (2 * a) + t * (3 * a));
        int color = new java.awt.Color(red, green, blue, alpha).getRGB();
        Fonts.ICONS_14.drawString("c", cx - Fonts.ICONS_14.getStringWidth("c") * 0.5F, cy, color);
    }
}
