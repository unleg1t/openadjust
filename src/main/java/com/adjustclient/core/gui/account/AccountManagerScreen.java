package com.adjustclient.core.gui.account;

import java.awt.Color;
import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatColor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.backend.api.BackendRequest;
import com.adjustclient.utilities.backend.api.PendingRequest;
import com.adjustclient.utilities.backend.api.response.BackendResponse;
import com.adjustclient.utilities.backend.api.response.IntResponseBody;
import com.adjustclient.utilities.backend.api.response.ResponseStatus;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.core.gui.account.repository.SelectRepositoryScreen;
import com.adjustclient.core.gui.account.repository.AccountRepository;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.modules.impl.render.streamer.StreamerModule;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.gui.account.login.AddAccountScreen;
import com.adjustclient.core.gui.account.repository.CreateRepositoryScreen;
import com.adjustclient.core.gui.account.repository.RepositoryListScreen;
import com.adjustclient.core.gui.account.repository.RepositoryMembersScreen;
import com.adjustclient.core.gui.account.session.SessionListScreen;
import com.adjustclient.core.gui.account.view.ViewAccountScreen;
import com.adjustclient.core.gui.mainmenu.AdjustMainMenu;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.managers.config.user.UserConfiguration;
import com.adjustclient.core.gui.account.login.CookieFileSelection;

public final class AccountManagerScreen extends GuiScreen {

    public boolean scrollToSelection;
    boolean connected;
    boolean searching;
    GuiButton filterButton;
    public List<AccountRepository> repositories;
    public AccountRepository currentRepository;
    AccountList accountList;
    public GuiTextField searchField;
    /** Screens shared by account and repository subpackages. */
    public CreateRepositoryScreen createRepositoryScreen;
    public RepositoryListScreen repositoryListScreen;
    public AddAccountScreen addAccountScreen;
    public ViewAccountScreen viewAccountScreen;
    public RepositoryMembersScreen membersScreen;
    GuiButton switchButton;
    GuiButton manageButton;
    public AccountEntry selectedAccount;
    public AccountEntry lastAccount;
    public GuiButton loginButton;
    public GuiButton viewButton;
    GuiButton addButton;
    GuiButton createRepositoryButton;
    GuiButton fernanButton;
    GuiButton localtsButton;
    AccountHead sessionHead;
    String sessionLabel;
    List<GuiButton> selectionButtons;
    public List<AccountEntry> selection;
    AccountRepository lastRequestedRepository;
    public boolean searchChanged;

    // ------------------------------------------------------------------------------------------------------------------------------
    // layout
    // ------------------------------------------------------------------------------------------------------------------------------

    void buildGui() {
        if (this.accountList == null) {
            this.accountList = new AccountList(this.mc, this.width, this.height, 32, this.height - 64, 36);
            this.accountList.registerScrollButtons(7, 8);
        }
        this.accountList.setDimensions(this.width, this.height, 32, this.height - 64);
        this.buttonList.clear();
        this.loginButton = new GuiButton(7, this.width / 2 - 154, this.height - 28, 70, 20, "Login");
        this.buttonList.add(this.loginButton);
        this.addButton = new GuiButton(2, this.width / 2 + 4, this.height - 28, 70, 20, "Add");
        this.buttonList.add(this.addButton);
        this.viewButton = new GuiButton(8, this.width / 2 - 74, this.height - 28, 70, 20, "View");
        this.buttonList.add(this.viewButton);
        this.buttonList.add(new GuiButton(0, this.width / 2 + 80, this.height - 28, 75, 20, "Cancel"));
        this.selectionButtons = List.of(
                new GuiButton(10, 3, 4, 60, 20, ChatColor.RED + "Delete"),
                new GuiButton(11, 65, 4, 50, 20, "Move"));
        this.buttonList.addAll(this.selectionButtons);
        this.manageButton = new GuiButton(1, this.width / 2 - 154, this.height - 52, 110, 20, "Manage repository");
        this.buttonList.add(this.manageButton);
        updateManageButton();
        this.switchButton = new GuiButton(6, this.width / 2 - 40, this.height - 52, 80, 20, "Switch");
        this.buttonList.add(this.switchButton);
        updateSwitchButton();
        this.createRepositoryButton = new GuiButton(4, this.width / 2 + 44, this.height - 52, 110, 20, "Create repository");
        if (this.width >= 650)
            this.buttonList.add(new GuiButton(14, 120, 4, 105, 20, "Bulk Import..."));
        // Narrow screens retain every import mode through Add -> Bulk Import.
        this.filterButton = new GuiButton(9, this.width - 207, 4, 95, 20, "");
        applyFilter(UserConfiguration.INSTANCE.banFilter);
        if (this.width >= 501) {
            this.buttonList.add(this.filterButton);
        }
        this.buttonList.add(this.createRepositoryButton);
        updateButtonStates();
        this.createRepositoryButton.enabled = this.connected;
    }

    /** Add is enabled while a repository is selected and loaded; Login needs a selected account. */
    void updateButtonStates() {
        for (GuiButton button : this.buttonList)
            if (button.id == 14) button.enabled = this.currentRepository != null;
        if (this.currentRepository != null) {
            this.addButton.enabled = !this.currentRepository.loading;     // not loading
        } else {
            this.addButton.enabled = false;
        }
        if (this.selectedAccount == null) {
            this.loginButton.enabled = false;
        }
    }

    /**
     * "Manage repository" for the owner of the current repository, "View repository" for everybody else; disabled without repository.
     * The member list is iterated under its monitor; the entry of the current user (session name) decides.
     */
    void updateManageButton() {
        if (this.currentRepository == null) {
            this.manageButton.enabled = false;
            this.manageButton.displayString = "View repository";
            return;
        }
        this.manageButton.enabled = true;
        this.manageButton.displayString = "Manage repository";
    }

    // ------------------------------------------------------------------------------------------------------------------------------
    // screen life cycle
    // ------------------------------------------------------------------------------------------------------------------------------

    /**
     * Per tick: follows the backend connection (on disconnect the repository, its accounts and the selection are dropped; the
     * "Create repository" button follows the connection), tracks the search state, clears the selection when no repository is selected,
     * sends the "repository opened" request when the repository changed, refreshes the Manage / Switch / Add / Login buttons and the
     * session head, and finally updates the list and the search cursor.
     */
    @Override
    public void updateScreen() {
        this.connected = true; // Local storage is independent of the backend connection.
        this.createRepositoryButton.enabled = true;
        if (this.scrollToSelection) {
            if (this.selectedAccount != null) {
                scrollTo(this.selectedAccount);
                this.scrollToSelection = false;
            }
        }
        boolean nowSearching = !this.searchField.getText().isEmpty();
        if (this.searching != nowSearching) {
            if (this.searchChanged) {
                this.scrollToSelection = true;
            }
            this.searchChanged = false;
        }
        this.searching = nowSearching;
        if (this.currentRepository == null) {
            selectDefaultRepository();
        }
        if (this.currentRepository != null) {
            if (this.selectedAccount != null) {
                if (this.currentRepository.accounts.indexOf(this.selectedAccount) < 0) {
                    select(null);
                }
            }
            requestRepositoryAccounts();
        } else {
            if (!this.mc.isCallingFromMinecraftThread()) {
                this.mc.addScheduledTask(() -> this.selection.clear());
            } else {
                this.selection.clear();
            }
            if (this.searchField != null && !this.searchField.getText().isEmpty()) {
                this.searchChanged = true;
            }
            this.selectedAccount = null;
            if (this.viewButton != null) {
                this.viewButton.enabled = false;
                this.loginButton.enabled = false;
            }
        }
        if (this.currentRepository != this.lastRequestedRepository) {
            LocalAccountStore.INSTANCE.selected(this.currentRepository);
            this.lastRequestedRepository = this.currentRepository;
        }
        this.manageButton.enabled = ((AccountManagerScreen) Adjust.INSTANCE.accountManager).currentRepository != null;   // the singleton is this screen
        updateSwitchButton();
        updateButtonStates();
        if (this.selectedAccount == null) {
            this.loginButton.enabled = false;
        }
        updateFilteredList();
        this.searchField.updateCursorCounter();
        updateSessionHead();
    }

    /**
     * Keeps {@link #sessionHead} / {@link #sessionLabel} in sync with the Minecraft session: when the head still belongs to the session
     * nothing happens; otherwise the head of the selected account with the same name (ignoring case) is shared and its type name
     * shown, or a new head is created for the session and labelled "Session".
     */
    void updateSessionHead() {
        net.minecraft.util.Session session = Minecraft.getMinecraft().getSession();
        AccountHead head = this.sessionHead;
        if (head != null && head.isFor(session.getUsername(), session.getPlayerID())) {
            return;
        }
        AccountEntry selected = this.selectedAccount;
        if (selected == null || selected.username == null || !selected.username.equalsIgnoreCase(session.getUsername())) {
            this.sessionHead = new AccountHead(session.getUsername(), session.getPlayerID());
            this.sessionLabel = "Session";
            return;
        }
        this.sessionHead = selected.head;
        AccountType type = selected.getType();
        this.sessionLabel = type != null ? type.displayName : "Session";
    }

    @Override
    public void onGuiClosed() {
        LocalAccountStore.INSTANCE.save();
        Keyboard.enableRepeatEvents(false);
    }

    void updateSwitchButton() {
        this.switchButton.displayString = "Switch (" + ChatColor.GRAY + this.repositories.size() + ChatColor.RESET + ")";
    }

    // ------------------------------------------------------------------------------------------------------------------------------
    // repositories and selection
    // ------------------------------------------------------------------------------------------------------------------------------

    public AccountRepository getRepository(int id) {
        for (AccountRepository repository : this.repositories) {
            if (repository.id == id) {
                return repository;
            }
        }
        return null;
    }

    void scrollTo(AccountEntry entry) {
        if (this.currentRepository == null) {
            return;
        }
        int index = this.currentRepository.accounts.indexOf(entry);      // entry.equals(element), first match
        AccountList list = this.accountList;
        float scrolled = list.amountScrolled;
        int rows = index < 8 ? 3 : index - 5;
        list.amountScrolled = (float) (int) (list.slotHeight * rows - scrolled) + scrolled;
        bindScroll(list);
        list.initialClickY = -2;
    }

    private void bindScroll(AccountList list) {
        list.bindAmountScrolled();
        Animation scroller = list.scrollAnimation;
        if (scroller == null) {
            return;
        }
        int size = list.getSize();
        float target = scroller.value;
        if (target < 0.0F) {
            target = 0.0F;
        } else {
            int max = list.headerPadding + list.slotHeight * size - (list.bottom - list.top) + 4;
            if (max < 1) {
                max = 0;
            }
            if (target > (float) max) {
                target = (float) max;
            }
        }
        scroller.value = target;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        AccountList list = this.accountList;
        if (keyCode == Keyboard.KEY_DOWN) {
            list.amountScrolled += (float) (2 * list.slotHeight / 3) * list.scrollMultiplier;
            list.initialClickY = -2;
            bindScroll(list);
            return;
        }
        if (keyCode == Keyboard.KEY_UP) {
            list.amountScrolled -= (float) (2 * list.slotHeight / 3) * list.scrollMultiplier;
            list.initialClickY = -2;
            bindScroll(list);
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (!this.selection.isEmpty()) {
                this.selection.clear();
                return;
            }
            if (this.searchField.getText().isEmpty()) {
                super.keyTyped(typedChar, keyCode);
                return;
            }
        }
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_ESCAPE) {
            this.searchField.setText("");
            this.searchField.setCursorPositionEnd();
            this.searchField.setFocused(false);
            return;
        }
        if (typedChar != 167 && typedChar >= 32 && typedChar != 127) {
            if (!this.searchField.isFocused()) {
                this.searchField.cursorCounter = 0;
            }
            this.searchField.setFocused(true);
        }
        this.searchField.textboxKeyTyped(typedChar, keyCode);
    }

    /** Shows {@code "Filter: " + filter.displayName} on the filter button. */
    void applyFilter(BanFilter filter) {
        this.filterButton.displayString = "Filter: " + filter.displayName;
    }

    // ------------------------------------------------------------------------------------------------------------------------------
    // buttons
    // ------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }
        if (this.switchButton == button) {
            this.mc.displayGuiScreen(this.repositoryListScreen);
            return;
        }
        switch (button.id) {
            case 0: {                                                                // Cancel
                if (this.mc.thePlayer != null) {
                    this.mc.displayGuiScreen(null);
                } else {
                    this.mc.displayGuiScreen(new AdjustMainMenu());
                }
                break;
            }
            case 1: {                                                                // Manage repository / View repository
                this.membersScreen.repository = this.currentRepository;
                this.membersScreen.pendingName = this.currentRepository.name;
                this.mc.displayGuiScreen(this.membersScreen);
                break;
            }
            case 2:                                                                  // Add
                this.mc.displayGuiScreen(this.addAccountScreen);
                break;
            case 4: {                                                                // Create repository
                this.createRepositoryScreen.parentScreen = this;
                this.mc.displayGuiScreen(this.createRepositoryScreen);
                break;
            }
            case 7:                                                                  // Login
                if (this.selectedAccount != null) {
                    this.selectedAccount.login(this.mc);
                }
                break;
            case 8:                                                                  // View
                this.mc.displayGuiScreen(this.viewAccountScreen);
                break;
            case 9: {                                                                // ban filter: next BanFilter (wraps around)
                BanFilter[] values = BanFilter.values();
                int next = UserConfiguration.INSTANCE.banFilter.ordinal() + 1;
                BanFilter nextFilter = values.length > next ? values[next] : values[0];
                UserConfiguration.INSTANCE.banFilter = nextFilter;
                applyFilter(nextFilter);
                break;
            }
            case 10:                                                                 // Delete (multi selection)
                confirmDelete();
                break;
            case 11:                                                                 // Move (multi selection)
                removeAccounts(this.selection.toArray(new AccountEntry[0]));
                break;
            case 12:                                                                 // "Fernan"
                break;
            case 13:                                                                 // "Localts"
                break;
            case 14:
                if (currentRepository != null) this.mc.displayGuiScreen(new com.adjustclient.core.gui.account.importer.BulkImportScreen(
                        this, com.adjustclient.core.gui.account.importer.ImportMode.COOKIE, addAccountScreen));
                break;
            default:
                break;
        }
    }

    public void removeAccounts(AccountEntry[] accounts) {
        if (accounts.length == 0 || this.currentRepository == null) {
            return;
        }
        AccountRepository source = this.currentRepository;
        AccountManagerScreen manager = (AccountManagerScreen) Adjust.INSTANCE.accountManager;
        SelectRepositoryScreen screen = new SelectRepositoryScreen(this, manager.currentRepository,
                target -> moveAccounts(source, accounts, target),
                new ArrayList<>(manager.selection));
        Minecraft.getMinecraft().displayGuiScreen(screen);
    }

    /** Sends the "move accounts" request (source repository, target repository, accounts); {@code target == null} (nothing selected) does nothing. */
    static void moveAccounts(AccountRepository source, AccountEntry[] accounts, AccountRepository target) {
        if (target == null) {
            return;
        }
        LocalAccountStore.INSTANCE.move(Adjust.INSTANCE.accountManager, source, target, accounts);
    }

    public void accountDeleted(int repositoryId, int accountId) {
        AccountRepository repository = null;
        for (AccountRepository candidate : this.repositories) {
            if (candidate.id == repositoryId) {
                repository = candidate;
                break;
            }
        }
        if (repository == null) {
            return;
        }
        AccountEntry entry = null;
        for (AccountEntry candidate : repository.accounts) {
            if (candidate.id == accountId) {
                entry = candidate;
                break;
            }
        }
        if (entry == null) {
            return;
        }
        final AccountRepository deletedFrom = repository;
        final AccountEntry deleted = entry;
        Minecraft mc = this.mc;
        if (mc.currentScreen == this.viewAccountScreen && deleted == this.selectedAccount) {
            mc.displayGuiScreen(this);
        }
        if (deleted == this.selectedAccount) {
            if (!mc.isCallingFromMinecraftThread()) {
                mc.addScheduledTask(() -> this.selection.clear());
            } else {
                this.selection.clear();
            }
            if (this.searchField != null && !this.searchField.getText().isEmpty()) {
                this.searchChanged = true;
            }
            this.selectedAccount = null;
            if (this.viewButton != null) {
                this.viewButton.enabled = false;
                this.loginButton.enabled = false;
            }
        }
        if (!mc.isCallingFromMinecraftThread()) {
            mc.addScheduledTask(() -> {
                deletedFrom.accounts.remove(deleted);
                this.selection.remove(deleted);
            });
        } else {
            deletedFrom.accounts.remove(deleted);
            this.selection.remove(deleted);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------
    // drawing and mouse
    // ------------------------------------------------------------------------------------------------------------------------------

    public void drawTitle(String title, int width) {
        this.drawCenteredString(this.fontRendererObj, title, width / 2, 10, 0xFFFFFF);
        String status;
        if (!this.connected) {
            status = ChatColor.RED + "Not connected to master";
        } else if (this.currentRepository == null) {
            status = ChatColor.RED + "No repository selected";
        } else {
            String members = String.join(ChatColor.GRAY + ", " + ChatColor.GREEN, this.currentRepository.memberNames);
            String memberText = ChatColor.GREEN + members + ChatColor.RESET;
            status = this.currentRepository.name + ChatColor.RESET + " [" + memberText + "]";
            if (isFilterActive()) {
                AccountRepository repository = this.currentRepository;
                int total;
                if (repository.loaded && !repository.loading) {     // loaded and not loading
                    total = repository.accounts.size();
                } else {
                    total = repository.accountCount;                       // account count reported by the backend
                }
                List<AccountEntry> filtered = this.accountList.filteredAccounts;
                int shown = filtered != null ? filtered.size() : total;
                status = status + ChatColor.GRAY + " (Viewing " + shown + "/" + total + ")";
            }
        }
        FontRenderer fr = this.mc.fontRendererObj;
        float centerX = (float) width * 0.5F;
        float textWidth = fr.getStringWidth(status, 0.5F);                             // scaled string width
        fr.drawString(status, 0xFFFFFF, true, centerX - textWidth * 0.5F, 21.5F, 0.5F);   // (text, colour, shadow, x, y, scale)
    }

    /**
     * Left click on the "View All Sessions" link plays the button sound and opens the session list; every other click goes to the
     * GuiScreen (buttons), the search field and the account list.
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && isOverViewAllSessions(mouseX, mouseY)) {
            this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            this.mc.displayGuiScreen(new SessionListScreen());
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
        this.accountList.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Draws the list, the title ("Account Manager"), the search field, hides / shows the Delete and Move buttons depending on the multi
     * selection (selection count text, or the "View All Sessions" link when nothing is selected), the drag and drop overlay and finally
     * the buttons.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.accountList.drawScreen(mouseX, mouseY, partialTicks);
        drawTitle("Account Manager", this.width);
        drawSearchField(mouseX, mouseY);
        boolean hasSelection = this.selection.size() != 0;
        this.selectionButtons.get(0).visible = hasSelection;                          // Delete
        this.selectionButtons.get(1).visible = hasSelection;                          // Move
        if (hasSelection) {
            drawSelectionCount();
        } else {
            drawSessionInfo(mouseX, mouseY);
        }
        DragDropOverlay.INSTANCE.render();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    boolean isOverViewAllSessions(int mouseX, int mouseY) {
        if (mouseX < 0) {
            return false;
        }
        FontRenderer fr = this.mc.fontRendererObj;
        float width = fr.getStringWidth("View All Sessions", 1.0F);
        if (mouseY < 21 || mouseY >= 32) {
            return false;
        }
        return (float) (int) width + 3.0F >= (float) mouseX;
    }

    void drawSessionInfo(int mouseX, int mouseY) {
        if (this.sessionHead != null) {
            this.sessionHead.c();
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            this.sessionHead.draw(3.0F, 3.0F);
            FontRenderer fr = this.mc.fontRendererObj;
            String label = this.sessionLabel;
            int grey = new Color(135, 135, 135, 255).getRGB();
            GlStateManager.enableAlpha();
            fr.renderString(label, grey, true, 43.0F, 26.0F, 1.0F);
            fr.renderString(label, grey, false, 42.0F, 25.0F, 1.0F);
            GL11.glScalef(2.0F, 2.0F, 2.0F);
            String name = StreamerModule.maskNameIfEnabled(this.sessionHead.name);        // Streamer mode alias of the session name
            int white = new Color(220, 221, 222, 255).getRGB();
            GlStateManager.enableAlpha();
            fr.renderString(name, white, true, 22.0F, 4.0F, 1.0F);
            fr.renderString(name, white, false, 21.0F, 3.0F, 1.0F);
            int shade = isOverViewAllSessions(mouseX, mouseY) ? 235 : 171;
            fr.drawString(ChatColor.UNDERLINE + "View All Sessions", new Color(shade, shade, shade).getRGB(), true, 2.0F, 20.0F, 1.0F);
        }
    }

    void drawSearchField(int mouseX, int mouseY) {
        GuiTextField field = this.searchField;
        if (field.getVisible() && field.isEnabled()) {
            if ((float) mouseX >= (float) field.xPosition && (float) (field.xPosition + field.width) > (float) mouseX
                    && (float) mouseY >= (float) field.yPosition && (float) (field.yPosition + field.height) > (float) mouseY) {
                RenderUtil.cursor = 221186;                                           // GLFW_IBEAM_CURSOR
            }
        }
        field.drawTextBox();
        if (!field.isFocused() && field.getText().isEmpty()) {
            this.fontRendererObj.drawString("Search", 7368816, true,
                    (float) (field.xPosition + 4), (float) (field.yPosition + (field.height - 8) / 2), 1.0F);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------
    // life cycle (continued), selection and repositories
    // ------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void initGui() {
        LocalAccountStore.INSTANCE.open(this);
        if (com.adjustclient.utilities.ClipboardUtil.sharedWindowHandle != -1L && com.adjustclient.utilities.ClipboardUtil.sharedWindowHandle != 0L)
            org.lwjgl.glfw.GLFW.glfwSetDropCallback(com.adjustclient.utilities.ClipboardUtil.sharedWindowHandle, AccountFileDropHandler.INSTANCE);
        this.connected = true;
        if (createRepositoryScreen == null) createRepositoryScreen = new CreateRepositoryScreen();
        if (repositoryListScreen == null) repositoryListScreen = new RepositoryListScreen();
        if (addAccountScreen == null) addAccountScreen = new AddAccountScreen();
        if (viewAccountScreen == null) viewAccountScreen = new ViewAccountScreen();
        if (membersScreen == null) membersScreen = new RepositoryMembersScreen();
        Keyboard.enableRepeatEvents(true);
        buildGui();
        AccountList list = this.accountList;
        if (list != null && list.scrollAnimation == null) {
            list.scrollAnimation = new Animation(Easing.EASE_OUT_EXPO, 420L);
            list.scrollbarHoverAnimation = new Animation(Easing.EASE_OUT_SINE, 250L);
        }
        if (this.searchField == null) {
            this.searchField = new GuiTextField(33, this.mc.fontRendererObj, 30, 6, 100, 16);
        }
        ScaledResolution resolution = new ScaledResolution(this.mc);
        this.searchField.xPosition = resolution.getScaledWidth() - this.searchField.width - this.searchField.yPosition;
        this.accountList.scrollMultiplier = 4.8F;
        this.searchField.setFocused(false);
        updateSessionHead();
    }

    /**
     * Makes {@code entry} the selected account (null = none): the multi selection is cleared (on the Minecraft thread, through
     * {@code mc.addScheduledTask} when called from another thread), the "search changed" flag is set while a search text exists and the
     * View / Login buttons are enabled only for a non-null entry.
     */
    public void select(AccountEntry entry) {
        if (!this.mc.isCallingFromMinecraftThread()) {
            this.mc.addScheduledTask(() -> this.selection.clear());
        } else {
            this.selection.clear();
        }
        if (this.searchField != null && !this.searchField.getText().isEmpty()) {
            this.searchChanged = true;
        }
        this.selectedAccount = entry;
        if (this.viewButton != null) {
            this.viewButton.enabled = entry != null;
            this.loginButton.enabled = entry != null;
        }
    }

    /** Adds {@code entry} to the multi selection unless it is already in it. */
    void addToSelection(AccountEntry entry) {
        if (!this.selection.contains(entry)) {
            this.selection.add(entry);
        }
    }

    public void requestRepositoryAccounts() {
        AccountRepository repository = this.currentRepository;
        if (repository != null) {
            repository.loading = false;
            repository.loaded = true;
            repository.accountCount = repository.accounts.size();
        }
    }

    public void selectDefaultRepository() {
        LocalAccountStore.INSTANCE.open(this);
    }

    /** True when the selected account is the account of the current Minecraft session (same name and same uuid). */
    public boolean isSessionAccountSelected() {
        if (this.selectedAccount == null || this.currentRepository == null) {
            return false;
        }
        net.minecraft.util.Session session = this.mc.getSession();
        return java.util.Objects.equals(this.selectedAccount.username, session.getUsername())
                && java.util.Objects.equals(this.selectedAccount.uuid, session.getPlayerID());
    }

    // ------------------------------------------------------------------------------------------------------------------------------
    // frame rate and mouse input
    // ------------------------------------------------------------------------------------------------------------------------------

    public int getMaxFps() {
        if (!AdjustMainMenu.windowActive) {
            return 60;
        }
        return Math.min(175, Minecraft.getMinecraft().gameSettings.limitFramerate);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.accountList.handleMouseInput();
    }

    // ------------------------------------------------------------------------------------------------------------------------------
    // search / ban filter
    // ------------------------------------------------------------------------------------------------------------------------------

    void updateFilteredList() {
        String query = this.searchField.getText();
        BanFilter banFilter = UserConfiguration.INSTANCE.banFilter;
        if (query.isEmpty() && banFilter == BanFilter.ALL) {
            this.accountList.filteredAccounts = null;
            return;
        }
        List<AccountEntry> filtered = this.accountList.filteredAccounts;
        if (filtered == null) {
            filtered = new ArrayList<>();
            this.accountList.filteredAccounts = filtered;
        } else {
            filtered.clear();
        }
        if (!query.isEmpty()) {
            String lowerQuery = query.toLowerCase(Locale.getDefault());
            if (this.currentRepository != null) {
                for (AccountEntry entry : this.currentRepository.accounts) {
                    if (entry.username.toLowerCase(Locale.getDefault()).contains(lowerQuery)) {
                        filtered.add(entry);
                    }
                }
            }
        } else if (this.currentRepository != null) {
            filtered.addAll(this.currentRepository.accounts);
        }
        filtered.removeIf(UserConfiguration.INSTANCE.banFilter.hidePredicate);
    }

    /** True while the list is filtered: the search text is not empty or the saved ban filter is not {@link BanFilter#ALL}. */
    boolean isFilterActive() {
        if (!this.searchField.getText().isEmpty()) {
            return true;
        }
        return UserConfiguration.INSTANCE.banFilter != BanFilter.ALL;
    }

    // ------------------------------------------------------------------------------------------------------------------------------
    // deleting accounts
    // ------------------------------------------------------------------------------------------------------------------------------

    /** Asks for confirmation before the multi selection is deleted from the current repository (GuiYesNo, id 1337). */
    void confirmDelete() {
        AccountEntry[] accounts = this.selection.toArray(new AccountEntry[0]);
        if (accounts.length == 0) {
            return;
        }
        AccountRepository repository = this.currentRepository;
        String text = "Are you sure you want to delete " + ChatColor.GRAY + accounts.length + ChatColor.RESET + " account"
                + (accounts.length == 1 ? "" : "s") + " from " + ChatColor.GRAY + repository.name + ChatColor.RESET + "?";
        GuiYesNo yesNo = new GuiYesNo((result, id) -> {
            this.mc.displayGuiScreen((GuiScreen) Adjust.INSTANCE.accountManager);
            if (result) LocalAccountStore.INSTANCE.remove(this, repository, accounts);
        }, text, "You are deleting multiple accounts at once.", 1337);
        yesNo.returnScreen = this;
        this.mc.displayGuiScreen(yesNo);
    }

    void drawSelectionCount() {
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        FontRenderer fr = this.mc.fontRendererObj;
        int count = this.selection.size();
        String text = "Selected " + ChatColor.WHITE + count + ChatColor.RESET + " account" + (count == 1 ? "" : "s");
        GlStateManager.enableAlpha();
        fr.renderString(text, 0xFF878787, true, 10.0F, 53.0F, 1.0F);
        fr.renderString(text, 0xFF878787, false, 9.0F, 52.0F, 1.0F);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
    }

}
