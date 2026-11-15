package com.adjustclient.ui.account.login;

import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import com.adjustclient.ui.account.AccountEntry;

public final class LoginAccountButton extends AccountButton {

    public LoginAccountButton(AccountEntry account, ResourceLocation icon) {
        super(account, icon);
    }

    @Override
    public String getTooltip() {
        return null;
    }

    @Override
    protected void drawIcon(float px, float py) {
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        Gui.drawTexturedModalRect(px * 2.0F, py * 2.0F, 0.0F, 0.0F);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
    }
}
