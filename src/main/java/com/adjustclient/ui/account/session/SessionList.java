package com.adjustclient.ui.account.session;

import com.adjustclient.service.session.SessionStats;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.util.MathHelper;

public final class SessionList extends GuiListExtended {
    public final List<SessionEntry> entries;

    public SessionList(Minecraft mc, int width, int height, int top, int bottom, int slotHeight, Collection<SessionStats> stats) {
        super(mc, width, height, top, bottom, slotHeight);
        this.entries = stats.stream().map(SessionEntry::new).toList();
    }

    public void scrollTo(SessionEntry entry) {
        if (this.entries == null) {
            return;
        }
        int index = this.entries.indexOf(entry);
        int targetRows = Math.max(3, index - 5);
        int delta = (int) ((float) (this.slotHeight * targetRows) - this.amountScrolled);
        this.amountScrolled += (float) delta;
        this.bindAmountScrolled();
        if (this.scrollAnimation != null) {
            this.scrollAnimation.value = MathHelper.clamp_float(this.scrollAnimation.value, 0.0F, (float) this.func_148135_f());
        }
        this.initialClickY = -2;
    }

    public boolean selectByName(SessionListScreen screen, String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        for (SessionEntry entry : this.entries) {
            if (name.equals(entry.stats.name)) {
                screen.selectedEntry = entry;
                SessionListScreen.selectedName = entry.stats.name;
                scrollTo(entry);
                return true;
            }
        }
        return false;
    }

    @Override
    protected int getSize() {
        return this.entries.size();
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
    public IGuiListEntry getListEntry(int index) {
        return this.entries.get(index);
    }

    @Override
    public int getListWidth() {
        return 220;
    }
}
