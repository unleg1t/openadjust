package com.adjustclient.ui.account.login;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import com.adjustclient.ui.account.AccountEntry;

public final class RepositoryStatusEntry extends AccountEntry {

    final com.adjustclient.ui.account.AccountList list;
    public boolean syncing;

    public RepositoryStatusEntry(com.adjustclient.ui.account.AccountList list) {
        this.list = list;
    }

    @Override public boolean mousePressed(int slot, int x, int y, int button, int relativeX, int relativeY) {
        return false;
    }

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight,
                          int mouseX, int mouseY, boolean isSelected) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        int yPos = y + slotHeight / 2 - fr.FONT_HEIGHT / 2;
        String message = this.syncing
                ? "Syncing accounts with server..."
                : "No repository selected";
        long time = Minecraft.getSystemTime() % 1300L;
        if (time >= 650L) {
            time = 1300L - time;
        }
        float cx = (float) mc.currentScreen.width / 2.0F - fr.getStringWidth(message, 1.0F) / 2.0F;
        float pulse = (float) (Math.sin((double) time / 1300.0) * Math.PI + 1.0);
        int alpha = Math.max(0, (int) (pulse * 40.0F));
        int color = new Color(170, 170, 170, alpha).getRGB();
        fr.drawString(message, color, false, cx, (float) yPos, 1.0F);
    }

    public com.adjustclient.ui.account.AccountType getType() { return null; }

    public void deserialize(byte[] data) throws java.io.IOException { throw new java.io.IOException(); }

    public com.adjustclient.auth.LoginResult login() { throw new RuntimeException(); }

    // Placeholder rows have no persisted payload.
    public byte[] serialize() { return new byte[0]; }

}
