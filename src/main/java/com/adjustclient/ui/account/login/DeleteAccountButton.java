package com.adjustclient.ui.account.login;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.util.Timer;
import com.adjustclient.ui.account.AccountEntry;

public final class DeleteAccountButton extends AccountButton {

    private Timer clickTimer;

    public DeleteAccountButton(AccountEntry account, ResourceLocation icon) {
        super(account, icon);
    }

    @Override
    public String getTooltip() {
        return "Deletes the account.";
    }

    @Override
    protected void drawIcon(float px, float py) {
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        Gui.drawTexturedModalRect(px * 2.0F, (py - 0.5F) * 2.0F, 112.0F, 220.0F);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
    }

    @Override
    public int getAlpha(int alpha, int mouseX, int mouseY) {
        float pulse = 1.0F;
        if (this.enabled && this.clickTimer != null) {
            long elapsed = System.currentTimeMillis() - this.clickTimer.lastMs;
            if (elapsed < 400L) {
                if ((double) (int) elapsed >= 200.0) {
                    elapsed = 400L - elapsed;
                }
                pulse = (float) (Math.sin((double) (int) elapsed / 800.0) * Math.PI + 1.0);
            }
        }
        int popped = (int) (pulse * (float) alpha);
        if (this.hoverAnim == null) {
            this.hoverAnim = new Animation(Easing.EASE_OUT_CUBIC, 185L);
            this.hoverAnim.value = 1.0F;
        }
        if (this.enabled) {
            float target = 1.0F;
            boolean hovered = (float) mouseX > this.x && (float) (this.width + this.x) > (float) mouseX
                    && (float) mouseY > this.y && (float) (this.height + this.y) > (float) mouseY;
            if (hovered) {
                target = 1.6F;
            }
            this.hoverAnim.update(target);
            int v = (int) ((float) popped * this.hoverAnim.value);
            if (v < 0) {
                return 0;
            }
            return Math.min(v, 255);
        }
        this.hoverAnim.value = 1.0F;
        if (popped < 0) {
            return 0;
        }
        return Math.min(popped, 255);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (!this.enabled || !this.visible) {
            return false;
        }
        if ((float) mouseX < this.x || (float) mouseY < this.y
                || (float) (this.width + this.x) <= (float) mouseX
                || (float) (this.height + this.y) <= (float) mouseY) {
            return false;
        }
        if (this.clickTimer != null) {
            if (this.clickTimer.lastMs + 700L <= System.currentTimeMillis()) {
                // first click (or timed out): arm the confirm timer
            } else {
                // confirming second click within 700 ms: remove the account
                this.account.delete();
                this.enabled = false;
            }
            this.clickTimer.lastMs = System.currentTimeMillis();
            return true;
        }
        return false; // clickTimer is created by the owning entry
    }
}
