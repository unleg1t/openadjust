package com.adjustclient.ui.account.repository;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;

import com.adjustclient.Adjust;
import com.adjustclient.ui.account.AccountManagerScreen;

public class SelectRepositoryScreen extends RepositoryListScreen {

    /** Repository that is current when the screen opens (hidden from the list). */
    private AccountRepository currentRepository;
    private Consumer<Object> selectionHandler;
    /** Accounts to re-add to the account manager list when the screen is left (ArrayList). */
    private List<com.adjustclient.ui.account.AccountEntry> accounts;
    /** "Select repository" button (id 0). */
    private GuiButton selectButton;
    /** Set by {@link #close()} on first use so that it runs once. */
    private boolean closed;

    public SelectRepositoryScreen() {
    }

    public SelectRepositoryScreen(net.minecraft.client.gui.GuiScreen parent, AccountRepository current,
            Consumer<AccountRepository> handler, List<com.adjustclient.ui.account.AccountEntry> accounts) {
        this.parentScreen = parent;
        this.currentRepository = current;
        this.selectionHandler = value -> handler.accept((AccountRepository) value);
        this.accounts = new java.util.ArrayList<>(accounts);
    }

    @Override
    public void updateScreen() {
        AccountManagerScreen screen = Adjust.INSTANCE.accountManager;
        AccountRepository selected = screen.currentRepository;
        if (selected != null) {
            this.selectButton.enabled = this.currentRepository != selected;
        } else {
            this.selectButton.enabled = false;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            if (button.id >= 0 && button.id < 2) {
                if (button.id == 1) Adjust.INSTANCE.accountManager.currentRepository = null;
                close();
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode != Keyboard.KEY_ESCAPE) {
            return;
        }
        AccountManagerScreen screen = Adjust.INSTANCE.accountManager;
        if (screen.currentRepository != null) {
            screen.currentRepository = null;                            // drop the selection
            screen.selection.clear();
        }
        close();
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.selectButton = new GuiButton(0, this.width / 2 - 90, this.height - 28, 110, 20, "Select repository");
        this.buttonList.add(this.selectButton);
        this.buttonList.add(new GuiButton(1, this.width / 2 + 24, this.height - 28, 50, 20, "Cancel"));
        this.list.repositories = this.list.repositories.stream()
                .filter(entry -> entry != this.currentRepository)
                .toList();
    }

    private void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        AccountManagerScreen screen = Adjust.INSTANCE.accountManager;
        AccountRepository selected = screen.currentRepository;
        this.selectionHandler.accept(selected);
        List<AccountRepository> repositories = screen.repositories;
        AccountRepository current = this.currentRepository;
        if (repositories.contains(current)) {
            if (current != screen.currentRepository) {
                screen.currentRepository = current;
                screen.selection.clear();
            }
            for (com.adjustclient.ui.account.AccountEntry account : this.accounts)
                if (current.accounts.contains(account)) screen.selection.add(account);
        }
        com.adjustclient.ui.account.LocalAccountStore.INSTANCE.selected(screen.currentRepository);
        this.mc.displayGuiScreen(this.parentScreen);
    }

}
