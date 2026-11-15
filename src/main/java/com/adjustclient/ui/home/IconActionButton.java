package com.adjustclient.ui.home;

import com.adjustclient.ui.dialog.AdjustButton;
import com.adjustclient.ui.dialog.AlphaModifier;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;

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
