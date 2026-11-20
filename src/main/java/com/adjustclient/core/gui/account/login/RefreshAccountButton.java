package com.adjustclient.core.gui.account.login;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.Adjust;
import com.adjustclient.core.gui.account.AccountEntry;

public final class RefreshAccountButton extends AccountButton {

    private Animation spinAnim;

    public RefreshAccountButton(AccountEntry account, ResourceLocation icon) {
        super(account, icon);
    }

    @Override
    public String getTooltip() {
        return "Refreshes the account.";
    }

    @Override
    public int getAlpha(int alpha, int mouseX, int mouseY) {
        if (this.spinAnim == null) {
            this.spinAnim = new Animation(Easing.EASE_OUT_CUBIC, 300L);
            this.spinAnim.value = 1.0F;
        }
        float spinTarget;
        if (!this.enabled) {
            // refreshing: pulse on a 1400 ms cycle
            long now = System.currentTimeMillis();
            spinTarget = (now % 1400L < 800L) ? 1.3F : 0.64999998F;
        } else {
            spinTarget = 1.0F;
        }
        float spin = this.spinAnim.update(spinTarget);
        int withSpin = (int) ((float) alpha * spin);

        if (this.hoverAnim == null) {
            this.hoverAnim = new Animation(Easing.EASE_OUT_CUBIC, 185L);
            this.hoverAnim.value = 1.0F;
        }
        if (this.enabled) {
            float hoverTarget = 1.0F;
            boolean hovered = (float) mouseX > this.x && (float) (this.width + this.x) > (float) mouseX
                    && (float) mouseY > this.y && (float) (this.height + this.y) > (float) mouseY;
            if (hovered) {
                hoverTarget = 1.6F;
            }
            this.hoverAnim.update(hoverTarget);
            int v = (int) ((float) withSpin * this.hoverAnim.value);
            if (v < 0) {
                return 0;
            }
            return Math.min(v, 255);
        }
        this.hoverAnim.value = 1.0F;
        if (withSpin < 0) {
            return 0;
        }
        return Math.min(withSpin, 255);
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
        this.enabled = false;                      // disable while refreshing
        if (this.account == null) {
            return false;
        }
        Consumer<Boolean> onDone = result -> this.enabled = true;
        this.account.requestAccountData(onDone);
        return true;
    }
}
