package com.adjustclient.core.gui.account;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

import com.adjustclient.Adjust;
import com.adjustclient.core.gui.account.repository.AccountRepository;
import com.adjustclient.core.gui.account.login.EmptyRepositoryEntry;
import com.adjustclient.core.gui.account.login.NoSearchResultsEntry;
import com.adjustclient.core.gui.account.login.RepositoryStatusEntry;

public final class AccountList extends GuiListExtended {

    final Minecraft minecraft;
    final RepositoryStatusEntry statusEntry;
    final NoSearchResultsEntry noResultsEntry;
    final EmptyRepositoryEntry emptyEntry;
    List<AccountEntry> filteredAccounts;
    Object field120;

    AccountList(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
        this.minecraft = mc;
        this.statusEntry = new RepositoryStatusEntry(this);
        this.noResultsEntry = new NoSearchResultsEntry(this);
        this.emptyEntry = new EmptyRepositoryEntry(this);
    }

    protected void beforeDrawList() {
    }

    protected void drawSelectionBox(int mouseX, int mouseY, float insideLeft, float insideTop) {
        AccountManagerScreen manager = (AccountManagerScreen) Adjust.INSTANCE.accountManager;
        AccountRepository repository = manager.currentRepository;
        if (repository != null && !repository.loading && repository.accounts.size() == 0) {
            this.emptyEntry.drawEntry(0, (int) insideLeft, (int) insideTop, 305, this.slotHeight - 4, mouseX, mouseY, false);
            return;
        }
        super.drawSelectionBox(mouseX, mouseY, insideLeft, insideTop);
    }

    /** 0 without repository, 1 (the status row) while it is loading, else the filter result size (1 = "no results" row when empty) or the repository size. */
    @Override
    protected int getSize() {
        AccountManagerScreen manager = (AccountManagerScreen) Adjust.INSTANCE.accountManager;
        AccountRepository repository = manager.currentRepository;
        if (repository == null) {
            return 0;
        }
        if (repository.loading) {                                  // repository is still loading
            return 1;
        }
        if (this.filteredAccounts != null) {
            int size = this.filteredAccounts.size();
            return size == 0 ? 1 : size;
        }
        return repository.accounts.size();
    }

    /** A row is highlighted when it is the account manager's selected account (never the status row). */
    @Override
    protected boolean isSelected(int slotIndex) {
        AccountManagerScreen manager = (AccountManagerScreen) Adjust.INSTANCE.accountManager;
        IGuiListEntry entry = getListEntry(slotIndex);
        if (entry == this.statusEntry) {
            return false;
        }
        return entry == manager.selectedAccount;
    }

    @Override
    protected int getScrollBarX() {
        return this.width / 2 + 154;
    }

    @Override
    public int getListWidth() {
        return 305;
    }

    /**
     * Row {@code index}: the status row without repository / while loading (its "syncing" flag is set when a repository exists); with a
     * filter the filtered account (or the "no results" row for an empty result); otherwise the repository account, falling back to the
     * status row when the index is out of range or the element is not an account.
     */
    @Override
    public IGuiListEntry getListEntry(int index) {
        AccountManagerScreen manager = (AccountManagerScreen) Adjust.INSTANCE.accountManager;
        AccountRepository repository = manager.currentRepository;
        if (repository == null || repository.loading) {
            this.statusEntry.syncing = repository != null;
            return this.statusEntry;
        }
        if (this.filteredAccounts != null) {
            if (this.filteredAccounts.size() == 0) {
                return this.noResultsEntry;
            }
            return this.filteredAccounts.get(index);
        }
        try {
            return (AccountEntry) repository.accounts.get(index);
        } catch (RuntimeException e) {                                              // null list, index out of range, not an account
            return this.statusEntry;
        }
    }
}
