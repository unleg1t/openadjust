package com.adjustclient.core.gui.home;

import com.adjustclient.core.gui.dialog.AdjustButton;
import com.adjustclient.core.gui.dialog.AlphaModifier;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.Fonts;

public final class IconActionButton extends HomeWidget {
    public AdjustButton button;
    public Runnable action;
    public Object field28;

    public IconActionButton(String glyph, Runnable action) {
        width = 6.0F;
        height = 6.0F;
        this.action = action;
        button = new AdjustButton(0, 0.0F, 0.0F, 6, 6, glyph,
                (AlphaModifier) alpha -> (int) (alpha * 0.75F));
        button.font = Fonts.ICONS_11;
    }
}
