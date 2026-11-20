package com.adjustclient.core.gui.account.login;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import com.adjustclient.core.gui.account.AccountEntry;

public class EmptyRepositoryEntry extends AccountEntry {

    final com.adjustclient.core.gui.account.AccountList list;

    public EmptyRepositoryEntry(com.adjustclient.core.gui.account.AccountList list) {
        this.list = list;
    }

    /**
     * Draws the centred placeholder message in grey (170,170,170).
     */
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight,
                          int mouseX, int mouseY, boolean isSelected) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        String message = (this instanceof NoSearchResultsEntry)
                ? "No accounts matching your search were found"
                : "No accounts exist in this repository";
        float cx = (float) mc.currentScreen.width / 2.0F - fr.getStringWidth(message, 1.0F) / 2.0F;
        float cy = (float) (y + slotHeight / 2 - fr.FONT_HEIGHT / 2);
        int color = new Color(170, 170, 170, 255).getRGB();
        fr.drawString(message, color, false, cx, cy, 1.0F);
    }

    public com.adjustclient.core.gui.account.AccountType getType() { return null; }

    public void deserialize(byte[] data) throws java.io.IOException { throw new java.io.IOException(); }

    public com.adjustclient.utilities.auth.LoginResult login() { throw new RuntimeException(); }

    public byte[] serialize() { return new byte[0]; }

    @Override public boolean mousePressed(int slot, int x, int y, int button, int relativeX, int relativeY) { return false; }
}
