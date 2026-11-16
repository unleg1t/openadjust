package com.adjustclient.ui.account.repository;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatColor;
import org.lwjgl.input.Keyboard;

import com.adjustclient.logging.Logger;
import com.adjustclient.Adjust;
import com.adjustclient.backend.api.BackendRequest;
import com.adjustclient.backend.api.PendingRequest;
import com.adjustclient.backend.api.error.RepositoryInviteError;
import com.adjustclient.backend.api.request.repository.DeleteRepositoryRequest;
import com.adjustclient.backend.api.response.BackendResponse;
import com.adjustclient.backend.api.response.EnumResponseBody;
import com.adjustclient.backend.api.response.ResponseStatus;
import com.adjustclient.backend.transport.ByteCodec;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.render.font.AccentText;
import com.adjustclient.render.font.TextComponent;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.ui.account.AccountManagerScreen;
import com.adjustclient.ui.account.login.CookieFileSelection;
import com.adjustclient.ui.hud.notification.NotificationBuilder;

public final class RepositoryMembersScreen extends GuiScreen {

    /** true while a request of this screen is in flight (all action buttons are disabled meanwhile). */
    private boolean busy;
    /** repository name field (max length 26), read-only for non owners. */
    GuiTextField nameField;
    /** invite / kick user name field (max length 26), only created for the owner. */
    private GuiTextField inviteField;
    public AccountRepository repository;
    private GuiButton confirmButton;
    private GuiButton inviteKickButton;
    /** Name to put into the name field on the next initGui (set by RepositoryListScreen, consumed once). */
    public String pendingName;
    private GuiButton wipeButton;
    private GuiButton deleteButton;
    private GuiButton leaveButton;
    private Object obf100;

    public RepositoryMembersScreen() {
    }

    @Override
    public void updateScreen() {
        this.nameField.updateCursorCounter();
        GuiButton inviteKick = this.inviteKickButton;
        GuiTextField invite = this.inviteField;
        if (invite != null) {
            invite.updateCursorCounter();
        }
        if (inviteKick != null && invite != null) {
            inviteKick.enabled = true;
            String name = invite.getText();
            if (!ServiceUtil.isValidName(name)) {
                inviteKick.enabled = false;
            } else if (name.equals(Adjust.username)) {
                inviteKick.enabled = false;
            } else if (this.repository.hasMember(name)) {
                inviteKick.displayString = "Kick";
            } else {
                inviteKick.displayString = "Invite";
            }
        }
        updateConfirmButton();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }
        switch (button.id) {
            case 0:
                confirmDelete();
                break;
            case 1:
                saveName();
                break;
            case 2:
                confirmWipe();
                break;
            case 3:
                cancel();
                break;
            case 4:
            case 6:
                // Remote-only Leave / Invite / Kick controls are not present for local repositories.
                break;
            default:
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            cancel();
        } else {
            if (this.nameField.isEnabled()) {
                this.nameField.textboxKeyTyped(typedChar, keyCode);
            }
            if (this.inviteField != null) {
                this.inviteField.textboxKeyTyped(typedChar, keyCode);
            }
        }
    }

    private static void onInviteResponse(String name, BackendResponse response) {
        if (response.status == ResponseStatus.OK) {            // success
            NotificationBuilder toast = new NotificationBuilder("Repository Invite");
            toast = toast.addText("You invited ", 0xFFBEBCBD);      // append coloured text
            toast = toast.append(name);                                    // append name component
            toast = toast.addText(" to this repository.", 0xFFBEBCBD);
            toast.show();                                        // show
            return;
        }
        Object payload = response.body;
        if (payload instanceof EnumResponseBody) {
            Object error = ((EnumResponseBody) payload).value;
            if (error instanceof RepositoryInviteError) {
                RepositoryInviteError kind = (RepositoryInviteError) error;
                if (kind == RepositoryInviteError.INVITE_SETTINGS) {
                    NotificationBuilder toast = new NotificationBuilder("Repository Invite");
                    toast.format("You cannot invite {} because of their invite settings.", new Object[] { name });
                    toast.show();
                    return;
                } else if (kind == RepositoryInviteError.ALREADY_USER) {
                    NotificationBuilder toast = new NotificationBuilder("Repository Invite");
                    toast.format("{} is already in this repository.", new Object[] { name });
                    toast.show();
                    return;
                } else if (kind == RepositoryInviteError.ALREADY_INVITED) {
                    NotificationBuilder toast = new NotificationBuilder("Repository Invite");
                    toast.format("You already have an invite pending to {}.", new Object[] { name });
                    toast.show();
                    return;
                }
            }
        }
        // generic failure: "That didn't work. Double check if <name> is correct and online."
        NotificationBuilder toast = new NotificationBuilder("Repository Invite");
        toast.addText("That didn't work. Double check if ", 0xFFBEBCBD)
                .append(name).addText(" is correct and online.", 0xFFBEBCBD);
        toast.show();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.nameField.isEnabled()) {
            this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if (this.inviteField != null) {
            this.inviteField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, isOwner() ? "Manage repository" : "Viewing", this.width / 2, 17, 0xFFFFFF);
        // "<name> <colour>(<n> accounts)" at half scale, centred, y = 28.5
        ChatColor colour = ChatColor.GRAY;
        String summary = this.repository.name + " " + colour + "(" + getAccountCount() + " accounts)";
        float summaryWidth = this.fontRendererObj.getStringWidth(summary, 0.5F);
        this.fontRendererObj.drawString(summary, 0xFFFFFF, true,
                (float) this.width * 0.5F - summaryWidth * 0.5F, 28.5F, 0.5F);
        if (isOwner()) {
            this.fontRendererObj.drawString("Repository name", 10526880, true, (float) (this.width / 2 - 100), 54.0F, 1.0F);
        } else {
            this.nameField.setText(this.repository.name);
            this.fontRendererObj.drawString("Repository name", 10526880, true, (float) (this.width / 2 - 100), 54.0F, 1.0F);
        }
        this.nameField.drawTextBox();
        if (this.inviteField == null) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            return;
        }
        // "Invite/Kick: [<n1>, <n2>, ...]"  (member names joined with a coloured separator)
        String separator = ChatColor.GRAY + ", " + ChatColor.GREEN;
        String members = String.join(separator, this.repository.memberNames);
        String line = "Invite/Kick: [" + ChatColor.GREEN + members + ChatColor.RESET + "]";
        this.fontRendererObj.drawString(line, 10526880, true, (float) (this.width / 2 - 100), 94.0F, 1.0F);
        this.inviteField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        if (this.nameField == null) {
            this.nameField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 100, 66, 200, 20);
            this.nameField.setMaxStringLength(26);
        } else {
            this.nameField.xPosition = this.width / 2 - 100;
            this.nameField.setMaxStringLength(26);                       // (re-truncates the text to 26 characters)
        }
        this.buttonList.clear();
        if (isOwner()) {
            this.nameField.setFocused(true);
            if (this.inviteField == null) {
                this.inviteField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 100, 106, 200, 20);
            }
            this.inviteField.xPosition = this.width / 2 - 100;
            this.inviteField.yPosition = 106;
            this.inviteField.setMaxStringLength(26);
            this.inviteField.setFocused(false);
            this.inviteKickButton = new GuiButton(6, this.width / 2 + 104, 106, 70, 20, "Invite");
            this.buttonList.add(this.inviteKickButton);
            int x = this.width / 2 - 100;
            int y1 = this.height / 2 + 56;
            int y2 = this.height / 2 + 80;
            this.deleteButton = new GuiButton(0, x, y1, 98, 20, ChatColor.RED + "Delete");
            this.buttonList.add(this.deleteButton);
            this.wipeButton = new GuiButton(2, this.width / 2 + 2, y1, 98, 20, ChatColor.RED + "Wipe");
            this.buttonList.add(this.wipeButton);
            this.confirmButton = new GuiButton(1, x, y2, 98, 20, "Confirm");
            this.buttonList.add(this.confirmButton);
            this.buttonList.add(new GuiButton(3, this.width / 2 + 2, y2, 98, 20, "Cancel"));
        } else {
            this.inviteField = null;
            if (this.repository != null) {
                this.nameField.setText(this.repository.name);
            }
            this.nameField.setEnabled(false);
            this.nameField.setFocused(false);
            int x = this.width / 2 - 100;
            int y = this.height / 2;
            this.leaveButton = new GuiButton(4, x, y - 24, 200, 20, ChatColor.RED + "Leave");
            this.buttonList.add(this.leaveButton);
            this.buttonList.add(new GuiButton(3, x, y, 200, 20, "Cancel"));
        }
        if (this.pendingName != null) {
            this.nameField.setText(this.pendingName);
            this.pendingName = null;
        }
        updateConfirmButton();
    }

    private void onKickResponse(String name, BackendResponse response) {
        if (response.status == ResponseStatus.OK) {
            NotificationBuilder toast = new NotificationBuilder("Account Repository");
            toast = toast.addText("You kicked ", 0xFFBEBCBD);
            toast = toast.append(name);
            toast = toast.addText(" from this repository.", 0xFFBEBCBD);
            toast.show();
            return;
        }
        String repo = "AccountRepository{id=" + this.repository.id + ", repositoryName='" + this.repository.name + "'}";
        Logger.log(LogLevel.WARN, true, "Failed to kick '{}' from repository '{}': {}", new Object[] { name, repo, response });
    }

    private void confirmDelete() {
        ChatColor c98 = ChatColor.WHITE;
        ChatColor c60 = ChatColor.GRAY;
        int count = getAccountCount();
        String noun = count != 1 ? "account" + "s" : "account";
        StringBuilder message = new StringBuilder(c98 + "You are deleting a repository with " + c60 + count + " " + noun + ".");
        if (this.repository.memberNames.length >= 2) {
            message.append("\n\nAccessible by ");
            message.append(this.repository.formatMembers());
            message.append(".");
        }
        String title = "Are you sure you want to delete " + c60 + this.repository.name + ChatColor.RESET + "?";
        GuiYesNo screen = new GuiYesNo(new DeleteCallback(this), title, message.toString(),
                I18n.format("gui.yes"), I18n.format("gui.no"), 1337);
        this.mc.displayGuiScreen(screen);
        screen.setButtonDelay(count < 6 ? 10 : 34);
    }

    private void confirmWipe() {
        int count = getAccountCount();
        String noun = count != 1 ? "account" + "s" : "account";
        String title = "Are you sure you want to wipe " + ChatColor.GRAY + this.repository.name + ChatColor.RESET + "?";
        String message = ChatColor.WHITE + "You are deleting " + ChatColor.GRAY + count + " " + noun + ChatColor.RESET + " from this repository.";
        GuiYesNo screen = new GuiYesNo(new WipeCallback(this), title, message,
                I18n.format("gui.yes"), I18n.format("gui.no"), 1337);
        this.mc.displayGuiScreen(screen);
        screen.setButtonDelay(count < 6 ? 10 : 34);
    }

    private void confirmLeave() {
        int count = getAccountCount();
        String title = "Are you sure you want to leave " + ChatColor.GRAY + this.repository.name + ChatColor.RESET + "?";
        GuiYesNo screen = new GuiYesNo(new LeaveCallback(this), title,
                "You will have to be invited back by the repository owner.",
                I18n.format("gui.yes"), I18n.format("gui.no"), 1337);
        this.mc.displayGuiScreen(screen);
        screen.setButtonDelay(count < 6 ? 10 : 34);
    }

    private void saveName() {
        String text = this.nameField.getText();
        if (this.repository.name.equals(text)) {
            close();                                                      // nothing changed
            return;
        }
        if (ServiceUtil.isValidDisplayName(text)) {
            com.adjustclient.ui.account.LocalAccountStore.INSTANCE.rename(this.repository, text);
            close();
        }
    }

    private void updateConfirmButton() {
        GuiButton confirm = this.confirmButton;
        if (confirm == null) {
            return;
        }
        String text = this.nameField.getText();
        if (this.repository.name.equals(text)) {
            confirm.enabled = false;
            return;
        }
        boolean valid;
        if (this.busy) {
            valid = false;
        } else if (isOwner()) {
            valid = ServiceUtil.isValidDisplayName(text);
        } else {
            valid = false;
        }
        confirm.enabled = valid;
    }

    private boolean isOwner() {
        return this.repository != null; // The sole local user owns every local repository.
    }

    private void cancel() {
        String text = this.nameField.getText();
        if (this.repository.name.equals(text)) {
            close();
            return;
        }
        GuiYesNo screen = new GuiYesNo(new DiscardChangesCallback(this), "You have unsaved changes!",
                "Are you sure you want to discard your changes?", 1337);
        this.mc.displayGuiScreen(screen);
    }

    private void close() {
        this.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);
    }

    private int getAccountCount() {
        AccountRepository repo = this.repository;
        if (repo.loaded && !repo.loading) {                              // accounts loaded and not loading
            return repo.accounts.size();
        }
        return repo.accountCount;
    }

    // ---------------------------------------------------------------------------------------------------------------------
    // lambdas

    private static final class DiscardChangesCallback implements GuiYesNoCallback {
        private final RepositoryMembersScreen screen;

        DiscardChangesCallback(RepositoryMembersScreen screen) {
            this.screen = screen;
        }

        @Override
        public void confirmClicked(boolean result, int id) {
            if (result) {
                this.screen.close();
            } else {
                this.screen.mc.displayGuiScreen(((AccountManagerScreen) Adjust.INSTANCE.accountManager).membersScreen);   // back to this screen
            }
        }
    }

    private static final class DeleteCallback implements GuiYesNoCallback {
        private final RepositoryMembersScreen screen;

        DeleteCallback(RepositoryMembersScreen screen) {
            this.screen = screen;
        }

        @Override
        public void confirmClicked(boolean result, int id) {
            RepositoryMembersScreen s = this.screen;
            s.mc.displayGuiScreen(((AccountManagerScreen) Adjust.INSTANCE.accountManager).membersScreen);
            if (!result) {
                return;
            }
            s.busy = true;
            if (s.leaveButton != null) {
                s.leaveButton.enabled = false;
            }
            if (s.confirmButton != null) {
                s.confirmButton.enabled = false;
            }
            if (s.wipeButton != null) {
                s.wipeButton.enabled = false;
            }
            com.adjustclient.ui.account.LocalAccountStore.INSTANCE.delete(Adjust.INSTANCE.accountManager, s.repository);
            s.busy = false;
            s.close();
        }
    }

    private static final class LeaveCallback implements GuiYesNoCallback {
        private final RepositoryMembersScreen screen;

        LeaveCallback(RepositoryMembersScreen screen) {
            this.screen = screen;
        }

        @Override
        public void confirmClicked(boolean result, int id) {
            RepositoryMembersScreen s = this.screen;
            s.mc.displayGuiScreen(((AccountManagerScreen) Adjust.INSTANCE.accountManager).membersScreen);
            if (result) {
                s.busy = true;
                if (s.leaveButton != null) {
                    s.leaveButton.enabled = false;
                }
                if (s.confirmButton != null) {
                    s.confirmButton.enabled = false;
                }
                if (s.wipeButton != null) {
                    s.wipeButton.enabled = false;
                }
                // Remote-only leave is unreachable: the local owner cannot leave their own repository.
                s.busy = false;
                s.close();
            }
        }
    }

    private static final class WipeCallback implements GuiYesNoCallback {
        private final RepositoryMembersScreen screen;

        WipeCallback(RepositoryMembersScreen screen) {
            this.screen = screen;
        }

        @Override
        public void confirmClicked(boolean result, int id) {
            RepositoryMembersScreen s = this.screen;
            s.mc.displayGuiScreen(((AccountManagerScreen) Adjust.INSTANCE.accountManager).membersScreen);
            if (result) {
                s.busy = true;
                if (s.leaveButton != null) {
                    s.leaveButton.enabled = false;
                }
                if (s.confirmButton != null) {
                    s.confirmButton.enabled = false;
                }
                if (s.wipeButton != null) {
                    s.wipeButton.enabled = false;
                }
                com.adjustclient.ui.account.LocalAccountStore.INSTANCE.remove(Adjust.INSTANCE.accountManager,
                        s.repository, s.repository.accounts.toArray(new com.adjustclient.ui.account.AccountEntry[0]));
                s.busy = false;
                s.close();
            }
        }
    }

    private static void onRepositoryUpdated(RepositoryMembersScreen s, BackendResponse response) {
        boolean wasBusy = s.busy;
        s.busy = false;
        if (s.leaveButton != null) {
            s.leaveButton.enabled = true;
        }
        if (s.confirmButton != null) {
            s.confirmButton.enabled = true;
        }
        if (s.wipeButton != null) {
            s.wipeButton.enabled = true;
        }
        if (response.status == ResponseStatus.OK) {
            if (wasBusy) {
                s.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);
            }
            return;
        }
        NotificationBuilder toast = new NotificationBuilder("Repository");
        toast.lifetimeSteps = Integer.valueOf(60);                    // toast colour/icon constant (Integer array element)
        toast = toast.addText("Failed to update repository.", 0xFFBEBCBD);
        toast.show();
    }

    private static final class UpdateCallback3938 implements Consumer<BackendResponse> {
        private final RepositoryMembersScreen screen;

        UpdateCallback3938(RepositoryMembersScreen screen) {
            this.screen = screen;
        }

        @Override
        public void accept(BackendResponse response) {
            onRepositoryUpdated(this.screen, response);
        }
    }

    private static final class UpdateCallback5d58 implements Consumer<BackendResponse> {
        private final RepositoryMembersScreen screen;

        UpdateCallback5d58(RepositoryMembersScreen screen) {
            this.screen = screen;
        }

        @Override
        public void accept(BackendResponse response) {
            onRepositoryUpdated(this.screen, response);
        }
    }

    private static final class UpdateCallback8d2d implements Consumer<BackendResponse> {
        private final RepositoryMembersScreen screen;

        UpdateCallback8d2d(RepositoryMembersScreen screen) {
            this.screen = screen;
        }

        @Override
        public void accept(BackendResponse response) {
            onRepositoryUpdated(this.screen, response);
        }
    }

    private static final class UpdateCallback9f3a implements Consumer<BackendResponse> {
        private final RepositoryMembersScreen screen;

        UpdateCallback9f3a(RepositoryMembersScreen screen) {
            this.screen = screen;
        }

        @Override
        public void accept(BackendResponse response) {
            onRepositoryUpdated(this.screen, response);
        }
    }

    private static final class InviteCallback implements Consumer<BackendResponse> {
        private final String name;

        InviteCallback(String name) {
            this.name = name;
        }

        @Override
        public void accept(BackendResponse response) {
            RepositoryMembersScreen.onInviteResponse(this.name, response);
        }
    }

    private static final class KickCallback implements Consumer<BackendResponse> {
        private final RepositoryMembersScreen screen;
        private final String name;

        KickCallback(RepositoryMembersScreen screen, String name) {
            this.screen = screen;
            this.name = name;
        }

        @Override
        public void accept(BackendResponse response) {
            this.screen.onKickResponse(this.name, response);
        }
    }
}
