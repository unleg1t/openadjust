package com.adjustclient.core.gui.account;

import java.awt.Color;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.utilities.Timer;
import com.adjustclient.core.gui.account.login.AccountButton;
import com.adjustclient.core.gui.screen.AdjustScreen;

public final class HoverTooltip {

    Timer hoverTimer;
    Animation fade;
    float width;
    float height;
    Object obf20;

    public HoverTooltip(float width, float height) {
        this.hoverTimer = new Timer();
        this.width = width;
        this.height = height;
    }

    public void draw(String text, int mouseX, int mouseY, int alpha, float x, float y, int delay) {
        boolean inside = (float) mouseX > x && x + this.width > (float) mouseX && (float) mouseY > y && y + this.height > (float) mouseY;
        float shown = 0.0F;
        if (inside && mouseY >= 33 && windowHeight() - 64 > mouseY) {
            if (this.hoverTimer.lastMs + delay <= System.currentTimeMillis()) {
                if (this.fade == null) {
                    this.fade = new Animation(Easing.LINEAR, 90L);
                }
                this.fade.update(1.0F);
            }
        } else {
            this.hoverTimer.lastMs = System.currentTimeMillis();
            if (this.fade != null) {
                this.fade.update(shown);
            }
        }
        if (this.fade == null) {
            return;
        }
        float visible = this.fade.value;
        if (shown == visible) {
            return;
        }
        int textAlpha = (int) ((float) alpha * visible);
        float boxX = (float) (mouseX + 5);
        float boxY = (float) (mouseY - 6);
        float boxWidth = Fonts.TAHOMA_10.getStringWidth(text) + 2.0F;
        AccountButton.drawBackground(textAlpha, boxX, boxY, boxWidth, 6.5F);              // background box
        Fonts.TAHOMA_10.drawString(text, (float) (mouseX + 6), (float) (mouseY - 6) + 2.5F,
                new Color(233, 235, 234, Math.max(0, Math.min(255, textAlpha))).getRGB());
    }

    private static int windowHeight() {
        return AdjustScreen.screenResolution.getScaledHeight();
    }
}
