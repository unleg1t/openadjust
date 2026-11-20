package com.adjustclient.core.gui.clickgui.frame;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import org.joml.Vector4f;
import com.adjustclient.core.gui.clickgui.ClickGuiComponent;
import com.adjustclient.core.gui.clickgui.ClickGuiScreen;
import com.adjustclient.core.gui.clickgui.SearchBar;

public final class ExpandButton extends HeaderButton {

    public ClickGuiComponent parent;
    protected HoverEffect hover;
    public Animation fade;
    protected Object unused36;

    public ExpandButton(ClickGuiComponent parent) {
        this.drawImmediately = true;
        this.width = 9.5F;                 // field initialisers (width / height) ...
        this.height = 11.0F;
        this.fade = new Animation(Easing.LINEAR, 60L);
        this.parent = parent;
        this.width = 6.0F;                 // ... overwritten by the constructor body
        this.height = 8.0F;
        this.hover = new HoverEffect(parent, () -> false, HoverEffect.hoveredOf(parent), 1.0F, 0.8F);
        parent.modifiers.add(this.hover);      // the hover animator becomes a decorator of the parent
        this.updateFade();
    }

    @Override
    public boolean onClick() {
        this.parent.toggleExpanded();
        return true;
    }

    private boolean isParentOpen() {
        ClickGuiComponent p = this.parent;
        com.adjustclient.core.gui.clickgui.ClickGuiTab root = p.owner;
        if (root != null) {
            ClickGuiScreen screen = root.screen;
            SearchBar search = screen.searchBar;
            if (search.isSearching()) {                 // search active and query not empty
                return search.hasMatchingChild(p);
            }
            return p.isFullyExpanded();
        }
        return p.isFullyExpanded();
    }

    private float updateFade() {
        return this.fade.update(this.isParentOpen() ? 1.0F : 0.0F);
    }

    @Override
    public void render() {
        float x = this.x + 0.5F;
        float y = this.y + 2.0F;
        int textColor = this.hover.color(120);              // grey 120 + 110 * v, alpha 130 + 48 * v
        float fadeValue = this.updateFade();
        boolean fading = !this.fade.finished;                // animation not finished
        if (fading) {
            // the clip shrinks from 4.0 to 0.5 high and moves down 1.5 as the parent opens: "+" -> "-"
            NvgDraw.pushScissor(x, y + 1.5F * fadeValue, this.width, 4.0F - fadeValue * 3.5F);
        }
        boolean open = this.isParentOpen();
        if (!open || fading) {                               // the "+" is only drawn while the parent is closed or the fade is running
            Fonts.TAHOMA_13.drawString("+", x, y, textColor);
        }
        if (open || fading) {
            int barAlpha = (int) ((float) (textColor >>> 24) * this.fade.value);
            if (barAlpha >= 26) {
                NvgContext.pushEvenScale();
                NvgContext.roundScaledPixels = true;
                Vector4f p = NvgContext.toPixels(x + 0.5F, y + 1.5F, (x + this.width) - 2.0F, y + 2.0F);
                Float scale = NvgContext.scaleFactor;              // NvgContext.scaleFactor
                int pixels = scale == null ? 1 : (NvgContext.roundScaledPixels ? Math.round(scale) : scale.intValue());
                float t = (float) pixels * 0.5F;
                int rgb = textColor & 0xFFFFFF;
                int barColor = (barAlpha << 24) | rgb;
                int shadowColor = (textColor & 0xFF000000) | ((rgb & 0xFCFCFC) >> 2);   // darker copy, original alpha
                NvgDraw.fillRect(shadowColor, p.x + t, p.y + t, p.z + t, p.w + t);       // shadow, shifted by t
                NvgDraw.fillRect(barColor, p.x, p.y, p.z, p.w);                          // the bar
                NvgContext.roundScaledPixels = false;
                NvgContext.popEvenScale();
            }
        }
        if (fading) {
            NvgDraw.popScissor();                           // epilogue: pop the scissor pushed above
        }
    }
}
