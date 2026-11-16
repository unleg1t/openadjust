package com.adjustclient.ui.account.session;

import com.adjustclient.Adjust;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.service.session.SessionStats;
import com.adjustclient.ui.mainmenu.AdjustMainMenu;
import java.awt.Color;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatColor;
import org.lwjgl.input.Keyboard;

public final class SessionListScreen extends GuiScreen {
    public static String selectedName = "";

    private SessionList sessionList;
    public SessionEntry selectedEntry;
    private GuiButton deleteButton;

    public SessionListScreen() {
        this.mc = Minecraft.getMinecraft();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 6) {
            this.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.sessionList.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc.theWorld != null) {
            Gui.drawRect(0, 0, this.width, this.height, 0xC0101010);
        } else {
            this.drawBackground(0);
        }
        this.sessionList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, "Session List", this.width / 2, 10, 0xFFFFFF);

        int count = this.sessionList.getSize();
        String subtitle = "Viewing " + ChatColor.DARK_AQUA + count + ChatColor.RESET
                + " past account session" + (count == 1 ? "" : "s");
        float centerX = (float) this.width * 0.5F;
        float subtitleWidth = this.fontRendererObj.getStringWidth(subtitle, 0.5F);
        int subtitleColor = new Color(180, 185, 183, 255).getRGB();
        this.mc.fontRendererObj.drawString(subtitle, subtitleColor, true, centerX - subtitleWidth * 0.5F, 21.5F, 0.5F);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        if (Adjust.INSTANCE != null && Adjust.INSTANCE.sessionService != null && Adjust.INSTANCE.sessionService.sessions != null) {
            Map<UUID, SessionStats> sessions = Adjust.INSTANCE.sessionService.sessions;
            synchronized (sessions) {
                this.sessionList = new SessionList(this.mc, this.width, this.height, 32, this.height - 64, 32, sessions.values());
            }
        } else {
            this.sessionList = new SessionList(this.mc, this.width, this.height, 32, this.height - 64, 32, Collections.emptyList());
        }

        if (selectedName == null || selectedName.isEmpty() || !this.sessionList.selectByName(this, selectedName)) {
            if (!this.sessionList.selectByName(this, Minecraft.getMinecraft().getSession().getUsername())
                    && !this.sessionList.entries.isEmpty()) {
                SessionEntry first = this.sessionList.entries.get(0);
                this.selectedEntry = first;
                selectedName = first.stats.name;
                this.sessionList.scrollTo(first);
            }
        }

        if (this.sessionList.scrollAnimation == null) {
            this.sessionList.scrollMultiplier = 2.5F;
            this.sessionList.scrollAnimation = new Animation(Easing.EASE_OUT_EXPO, 420L);
            this.sessionList.scrollbarHoverAnimation = new Animation(Easing.EASE_OUT_SINE, 250L);
        }
        this.sessionList.setDimensions(this.width, this.height, 32, this.height - 40);
        this.sessionList.registerScrollButtons(7, 8);

        this.deleteButton = new GuiButton(1, this.width / 2 - 154, this.height - 28, 110, 20, ChatColor.RED + "Delete Selected");
        this.buttonList.add(this.deleteButton);
        this.buttonList.add(new GuiButton(6, this.width / 2 - 40, this.height - 28, 80, 20, "Cancel"));
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.sessionList.handleMouseInput();
    }

    public int getMaxFps() {
        if (!AdjustMainMenu.windowActive) {
            return 60;
        }
        return Math.min(175, Minecraft.getMinecraft().gameSettings.limitFramerate);
    }
}
