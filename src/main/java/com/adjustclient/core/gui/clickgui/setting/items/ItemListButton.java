package com.adjustclient.core.gui.clickgui.setting.items;

import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.utilities.Vec2f;
import java.awt.Color;
import org.joml.Vector4f;
import com.adjustclient.core.gui.dialog.AdjustButton;

public final class ItemListButton extends ItemListEntry {

    private ItemListComponent owner;
    private AdjustButton model;

    ItemListButton(ItemListComponent owner) {
        this.owner = owner;
        this.model = new AdjustButton(0, "+", 6, 6, 26, alpha -> alpha, com.adjustclient.core.rendering.font.Fonts.TAHOMA_12);
    }

    void setLabel(String label) {
        this.model.displayString = label;
    }

    @Override
    public void draw(Vec2f mouse, com.adjustclient.core.events.Render2DEvent queue, boolean hovered) {
        AdjustButton button = this.model;
        button.x = this.x; // x
        button.y = this.y; // y
        if (!button.visible /* visible */) {
            return;
        }
        float right = (float) button.width + this.x;   // width / height of the model are ints
        float bottom = (float) button.height + this.y;
        boolean over = mouse.x >= this.x && mouse.y >= this.y && right > mouse.x && bottom > mouse.y;
        button.hovered = over;
        float target = button.enabled /* enabled */ && over ? 1.0F : 0.0F;
        button.hoverAnimation.update(target);
        float anim = button.hoverAnimation.value;                   // Animation value
        int alpha = (int) (anim * 25.0F) + 201;
        String label = button.displayString;
        if (label == "Quit Playing" || "Quit Playing".equals(label)) {
            alpha -= 40;
        }
        int fillBase = (int) (anim * 30.0F);
        int shown = button.enabled ? alpha : alpha - 40;
        NvgContext.pushEvenScale();
        Vector4f px = NvgContext.toPixels(button.x, button.y, button.x + (float) button.width, button.y + (float) button.height);
        int grey = button.base;
        int fill = new Color(grey, grey, grey, button.alphaModifier.multiplyAlpha(fillBase + 82)).getRGB();
        NvgDraw.fillRect(fill, px.x, px.y, px.z, px.w);
        float line = NvgContext.scaleFactor == null ? 1.0F
                : (float) (NvgContext.roundScaledPixels ? Math.round(NvgContext.scaleFactor)
                        : (int) NvgContext.scaleFactor.floatValue());
        int outline = new Color(grey, grey, grey, button.alphaModifier.multiplyAlpha(fillBase + 122)).getRGB();
        NvgDraw.strokeRect(true, outline, px.x, px.y, px.z, px.w, line * 0.5F);
        NvgContext.popEvenScale();
        float cx = (float) button.width / 2.0F + button.x;
        if (button.displayString.length() == 1 && !button.font.symbolFont /* has icon */) {
            cx += 0.25F;
        }
        float tx = cx + button.labelOffsetX;
        float ty = ((float) button.height / 2.0F + button.y - 1.5F) + button.labelOffsetY;
        int textColor = new Color(shown, shown, shown, button.alphaModifier.multiplyAlpha(2 * fillBase + 190)).getRGB();
        button.font.drawCenteredString(button.displayString, tx, ty, textColor);
    }
}
