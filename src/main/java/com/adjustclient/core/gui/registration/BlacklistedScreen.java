package com.adjustclient.core.gui.registration;

import com.adjustclient.managers.util.ServiceUtil;
import com.adjustclient.utilities.TimeUtil;
import java.awt.Color;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatColor;

public final class BlacklistedScreen extends GuiScreen {
    private final String reason;
    private final long expiresAt;

    public BlacklistedScreen(String reason, long expiresAt) {
        this.reason = reason;
        this.expiresAt = expiresAt;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        System.exit(0);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // Prevent dismissing the blacklist notice with Escape or other keys.
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc.theWorld != null) {
            drawRect(0, 0, this.width, this.height, -1072689136);
        } else {
            this.drawDefaultBackground();
        }

        int centerX = this.width / 2;
        this.drawCenteredString(this.fontRendererObj, "You are blacklisted from Adjust.", centerX, 70, new Color(171, 36, 37, 255).getRGB());
        this.drawCenteredString(this.fontRendererObj, this.reason, centerX, 81, new Color(152, 149, 149, 255).getRGB());

        long now = System.currentTimeMillis();
        if (this.expiresAt == 0L) {
            this.drawCenteredString(this.fontRendererObj, "This ban is permanent.", centerX, 125, new Color(150, 62, 62, 255).getRGB());
        } else if (now < this.expiresAt) {
            String remaining = TimeUtil.timeAgo(now - (this.expiresAt - now), "");
            this.drawCenteredString(this.fontRendererObj, "Expires in " + ChatColor.GRAY + remaining, centerX, 125, new Color(176, 176, 176).getRGB());
        } else {
            this.drawCenteredString(this.fontRendererObj, "Expired", centerX, 125, new Color(176, 176, 176).getRGB());
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, 140, 200, 20, "Exit Adjust"));

        // Clear overlay and reset offline service state; private ServiceUtil/HydraConnection transport fields remain isolated.
        ServiceUtil.connectionStatusOverlay = null;
        ServiceUtil.resetServiceState();
    }
}
