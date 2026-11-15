package com.adjustclient.ui.multisession;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;

public class MultiSessionScreen extends GuiScreen {
    public net.minecraft.client.gui.GuiMultiplayer parent;
    public Object field68;
    public MultiSessionScreen(net.minecraft.client.gui.GuiMultiplayer parent) { this.parent = parent; }
    @Override public void updateScreen() { }
    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (mc.theWorld != null) drawGradientRect(0, 0, width, height, 0xc0101010, 0xc0101010);
        else drawDefaultBackground();
        String title = "Multi-Session";
        fontRendererObj.drawStringWithShadow(title, width / 2 - fontRendererObj.getStringWidth(title) / 2.0F, 10.0F, 0xffffff);
        parent.serverListSelector.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    @Override public void initGui() {
        GuiSlot list = parent.serverListSelector;
        list.setDimensions(width, height, 62, height / 3);
    }
}
