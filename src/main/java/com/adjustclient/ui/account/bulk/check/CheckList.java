package com.adjustclient.ui.account.bulk.check;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ChatColor;

public final class CheckList extends GuiListExtended {
    private static CheckEntry fallbackEntry;

    public final List<CheckEntry> entries = new ArrayList<>();
    public final String title;

    public CheckList(Minecraft mc, int width, int height, int top, int bottom, int slotHeight, String title) {
        super(mc, width, height, top, bottom, slotHeight);
        this.title = title;
        this.scrollAnimation = new Animation(Easing.EASE_OUT_EXPO, 400L);
        this.scrollbarHoverAnimation = new Animation(Easing.EASE_OUT_SINE, 230L);
        this.selectedElement = -1;
        this.setHasListHeader(true, (int) ((float) mc.fontRendererObj.FONT_HEIGHT * 1.5F));
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        getListEntry(slotIndex);
        this.selectedElement = slotIndex;
    }

    @Override
    protected void drawListHeader(int insideLeft, int insideTop, Tessellator tessellator) {
        String header = ChatColor.UNDERLINE + "" + ChatColor.BOLD + this.title;
        float centerX = (float) (this.width / 2) + (float) insideLeft;
        float textWidth = this.mc.fontRendererObj.getStringWidth(header, 1.0F);
        float y = Math.min((float) (this.top + 3), (float) insideTop);
        this.mc.fontRendererObj.drawString(header, 0xFFFFFF, false, centerX - textWidth / 2.0F, y, 1.0F);
    }

    @Override
    protected int getSize() {
        return this.entries.size();
    }

    @Override
    protected int getScrollBarX() {
        return this.right - 6;
    }

    @Override
    public IGuiListEntry getListEntry(int index) {
        try {
            return this.entries.get(index);
        } catch (Exception e) {
            if (fallbackEntry == null) {
                fallbackEntry = new CheckEntry("null", "null");
            }
            return fallbackEntry;
        }
    }

    @Override
    public int getListWidth() {
        return this.width;
    }
}
