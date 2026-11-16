package com.adjustclient.ui.account.repository;

import java.io.IOException;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import com.adjustclient.Adjust;
import com.adjustclient.ui.account.AccountManagerScreen;

public class RepositoryListScreen extends GuiScreen {

    protected RepositoryList list;
    /** "Manage repository" button (id 1), enabled while a repository is selected. */
    protected GuiButton manageButton;
    protected GuiScreen parentScreen;

    public RepositoryListScreen() {
    }

    @Override
    public void updateScreen() {
        AccountManagerScreen screen = Adjust.INSTANCE.accountManager;
        this.manageButton.enabled = screen.currentRepository != null;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }
        AccountManagerScreen screen = Adjust.INSTANCE.accountManager;
        switch (button.id) {
            case 1: {
                RepositoryMembersScreen members = (RepositoryMembersScreen) screen.membersScreen;
                members.repository = screen.currentRepository;
                members.pendingName = screen.currentRepository.name;
                this.mc.displayGuiScreen(members);
                break;
            }
            case 6: // Cancel
                this.mc.displayGuiScreen(screen);
                break;
            case 4: {
                CreateRepositoryScreen create = (CreateRepositoryScreen) screen.createRepositoryScreen;
                create.parentScreen = this;
                this.mc.displayGuiScreen(create);
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode != 1) {                                           // ESC
            return;
        }
        this.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.list.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);               // button loop for mouseButton == 0
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc.theWorld != null) {
            Gui.drawRect(0, 0, this.width, this.height, 0xC0101010);
        } else {
            this.drawBackground(0);
        }
        this.list.drawScreen(mouseX, mouseY, partialTicks);
        String title;
        if (this instanceof SelectRepositoryScreen) {
            title = "Select Repository";
        } else {
            title = "Switch Repository";
        }
        Adjust.INSTANCE.accountManager.drawTitle(title, this.width);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        com.adjustclient.ui.account.LocalAccountStore.INSTANCE.open(Adjust.INSTANCE.accountManager);
        this.buttonList.clear();
        if (this.list == null) {
            this.list = new RepositoryList(this.mc, this.width, this.height, 32, this.height - 64, 32);
            this.list.repositories = Adjust.INSTANCE.accountManager.repositories;
        }
        this.list.setDimensions(this.width, this.height, 32, this.height - 40);
        this.list.registerScrollButtons(7, 8);
        this.manageButton = new GuiButton(1, this.width / 2 - 154, this.height - 28, 110, 20, "Manage repository");
        this.buttonList.add(this.manageButton);
        this.buttonList.add(new GuiButton(6, this.width / 2 - 40, this.height - 28, 80, 20, "Cancel"));
        this.buttonList.add(new GuiButton(4, this.width / 2 + 44, this.height - 28, 110, 20, "Create repository"));
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.list.handleMouseInput();
    }
}
