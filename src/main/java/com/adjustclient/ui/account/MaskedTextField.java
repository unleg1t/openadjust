package com.adjustclient.ui.account;

import net.minecraft.client.gui.GuiTextField;

public class MaskedTextField extends GuiTextField {

    Object obf80;

    public MaskedTextField(int id, net.minecraft.client.gui.FontRenderer font, int x, int y, int width, int height) {
        super(id, font, x, y, width, height);
    }

    @Override
    public String getDisplayText() {
        if (!this.maskText) {
            return this.text;
        }
        int length = this.text.length();
        if (length == 0) {
            return "";
        }
        if (length == 1) {
            return "*";
        }
        return "*".repeat(length);
    }
}
