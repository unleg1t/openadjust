package com.adjustclient.ui.account;

import java.awt.Color;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.image.NvgDraw;
import net.minecraft.util.ChatColor;
import com.adjustclient.ui.mainmenu.AdjustMainMenu;
import com.adjustclient.ui.screen.AdjustScreen;

public final class DragDropOverlay {

    public static final DragDropOverlay INSTANCE;

    final Animation fade;
    Object obf8;

    static {
        INSTANCE = new DragDropOverlay();
    }

    private DragDropOverlay() {
        this.fade = new Animation(Easing.LINEAR, 150L);
    }

    public void render() {
        float target = AdjustMainMenu.windowActive ? 0.0F : 1.0F;
        float value = this.fade.update(target);
        if (0.0F >= value) {
            return;
        }
        float height = (float) AdjustScreen.screenResolution.getScaledHeight();
        float width = (float) AdjustScreen.screenResolution.scaledWidth;
        int veilAlpha = (int) (value * 175.0F);
        NvgDraw.fillRect(new Color(28, 28, 28, veilAlpha).getRGB(), 0.0F, 0.0F, width, height);

        int textAlpha = (int) (value * 200.0F);
        int textColor = new Color(189, 189, 190, textAlpha).getRGB();
        String title = ChatColor.BOLD + "Drag and Drop";
        float titleWidth = Fonts.TAHOMA_20.getStringWidth(title);
        Fonts.TAHOMA_20.drawString(title, width / 2.0F - titleWidth * 0.5F, height / 2.3F, textColor);

        String hint = "ZIP & multiple files allowed " + ChatColor.GRAY + "(cookie accounts)";
        float hintWidth = Fonts.TAHOMA_14.getStringWidth(hint);
        Fonts.TAHOMA_14.drawString(hint, width / 2.0F - hintWidth * 0.5F, height / 2.3F + 14.0F, textColor);
    }
}
