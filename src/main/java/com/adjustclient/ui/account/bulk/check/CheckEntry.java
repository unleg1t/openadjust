package com.adjustclient.ui.account.bulk.check;

import com.adjustclient.render.ColorUtil;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.util.MinecraftInstance;
import java.awt.Color;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.util.ChatColor;

public final class CheckEntry implements MinecraftInstance, GuiListExtended.IGuiListEntry {
    public final String name;
    public final String data;
    public String status = "Unchecked";
    public final Animation highlight = new Animation(Easing.LINEAR, 100L);
    public boolean success;

    public CheckEntry(String name, String data) {
        this.name = name;
        this.data = data;
    }

    @Override
    public void setSelected(int slotIndex, int x, int y) {
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
        this.highlight.update(isSelected ? 1.0F : 0.0F);
        int nameColor = ColorUtil.blend(
                new Color(222, 222, 222, 255),
                new Color(177, 177, 177, 255),
                this.highlight.value
        ).getRGB();
        mc.fontRendererObj.drawStringWithShadow(this.name, (float) (x + 2), (float) y, nameColor);

        String statusText = (this.success ? "" : ChatColor.RED.toString()) + this.status;
        int statusColor = ColorUtil.blend(
                new Color(232, 232, 232, 255),
                new Color(189, 189, 189, 255),
                this.highlight.value
        ).getRGB();
        mc.fontRendererObj.drawStringWithShadow(statusText, (float) (x + 2), (float) (y + 11), statusColor);
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        return false;
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
    }
}
