package com.adjustclient.core.gui.account.login;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import com.adjustclient.Adjust;
import com.adjustclient.core.gui.account.AccountEntry;

public final class ViewAccountButton extends AccountButton {

    public ViewAccountButton(AccountEntry account, ResourceLocation icon) {
        super(account, icon);
    }

    @Override
    public String getTooltip() {
        return "Views the account.";
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (!this.enabled || !this.visible) {
            return false;
        }
        if ((float) mouseX < this.x
                || (float) (this.width + this.x) <= (float) mouseX
                || (float) mouseY < this.y
                || (float) (this.height + this.y) <= (float) mouseY) {
            return false;
        }
        GuiScreen view = Adjust.INSTANCE.accountManager.viewAccountScreen;
        if (mc != null) {
            mc.displayGuiScreen(view);
            return true;
        }
        return true;
    }
}
