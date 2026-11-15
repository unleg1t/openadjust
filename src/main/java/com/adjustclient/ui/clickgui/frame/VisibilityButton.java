package com.adjustclient.ui.clickgui.frame;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.Adjust;

public final class VisibilityButton extends HeaderButton {

    protected HoverEffect hover;
    protected Animation crossFade;
    protected ModuleEntry entry;
    protected Object unused36;

    public VisibilityButton(HoverEffect hover, ModuleEntry entry) {
        this.width = 9.5F;
        this.height = 11.0F;
        this.drawImmediately = true;
        this.hover = hover;
        this.entry = entry;
        this.crossFade = new Animation(Easing.LINEAR, 60L);
    }

    @Override
    public boolean onClick() {
        com.adjustclient.module.Module module = this.entry.module;
        boolean visible = (module.flags & 4) != 0;
        module.flags = (byte) (visible ? module.flags & 0xFB : module.flags | 4);
        Adjust.INSTANCE.fileSystem.markDirty();
        return true;
    }

    @Override
    public void render() {
        boolean visible = (this.entry.module.flags & 4) != 0;
        this.crossFade.value = visible ? 1.0F : 0.0F;
        float x = this.x + 1.5F;
        float v = this.crossFade.value;
        int base = this.hover.color(190);
        int alpha = base >>> 24;
        int rgb = base & 0xFFFFFF;
        int dAlpha = (int) (v * (float) alpha);
        if (dAlpha >= 26) {
            Fonts.ICONS_14.drawStringNoShadow("d", x, this.y + 3.75F, (dAlpha << 24) | rgb);
        }
        int eAlpha = (int) ((1.0F - v) * (float) alpha);
        if (eAlpha >= 26) {
            Fonts.ICONS_14.drawStringNoShadow("e", x, this.y + 3.5F, (eAlpha << 24) | rgb);
        }
    }

    @Override
    public boolean isShown() {
        return this.entry.isHovered();
    }
}
