package com.adjustclient.core.gui.account.repository;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

public final class RepositoryList extends GuiListExtended {

    List<?> repositories;
    Object field104;

    RepositoryList(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
    }

    @Override
    protected int getSize() {
        return this.repositories.size();
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return false;
    }

    @Override
    protected int getScrollBarX() {
        return this.width / 2 + 154;
    }

    @Override
    public int getListWidth() {
        return 220;
    }

    @Override
    public IGuiListEntry getListEntry(int index) {
        return (IGuiListEntry) this.repositories.get(index);
    }
}
