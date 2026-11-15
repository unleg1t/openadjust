package com.adjustclient.ui.account.repository;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import com.adjustclient.util.Timer;
import com.adjustclient.Adjust;
import com.adjustclient.backend.api.PendingRequest;
import com.adjustclient.backend.api.request.repository.CreateRepositoryRequest;
import com.adjustclient.backend.api.response.BackendResponse;
import com.adjustclient.backend.api.response.ResponseStatus;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.util.HashUtil;
import net.minecraft.util.ChatColor;

public final class CreateRepositoryScreen extends GuiScreen {

    private boolean authorized;
    /** Set by the response callback after a failure: forces the next updateScreen to re-evaluate the Create button. */
    private boolean refreshRequested;
    /** True while a create request is in flight. */
    private boolean busy;
    /** Repository name field (max length 26, focused on init). */
    GuiTextField nameField;
    public GuiScreen parentScreen;
    /** ArrayList that is cleared in initGui; never otherwise used in this class. */
    private ArrayList<Object> obf72;
    /** Error message ("Creation failed!") or null. */
    private String statusMessage;
    private Timer messageTimer;
    /** Alpha of the message (255 -> fades by 10 per tick). */
    private int messageAlpha;
    private Object obf88;

    public CreateRepositoryScreen() {
    }

    @Override
    public void updateScreen() {
        boolean changed = false;
        if (this.statusMessage != null) {
            if (this.messageTimer.hasElapsed(2500L)) {
                this.messageAlpha -= 10;
                if (this.messageAlpha < 2) {
                    this.statusMessage = null;
                    changed = true;
                }
            }
        }
        if (this.refreshRequested) {
            this.refreshRequested = false;
            changed = true;
        }
        boolean state = true; // Local repositories need no backend authorization.
        boolean recompute;
        if (this.authorized == state) {
            recompute = changed;
        } else {
            this.authorized = state;
            if (!state) {
                this.busy = false;
            }
            recompute = true;
        }
        if (recompute) {
            GuiButton createButton = (GuiButton) this.buttonList.get(0);
            boolean enabled;
            if (this.statusMessage != null) {
                enabled = false;
            } else if (this.authorized) {
                if (this.busy) {
                    enabled = false;
                } else {
                    enabled = ServiceUtil.isValidDisplayName(this.nameField.getText());
                }
            } else {
                enabled = false;
            }
            createButton.enabled = enabled;
        }
        this.nameField.updateCursorCounter();
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
        if (button.id != 0) {                                        // Cancel
            if (!this.busy) {
                this.mc.displayGuiScreen(this.parentScreen);
            }
        } else {                                                     // Create
            createRepository(this.nameField.getText());
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.nameField.textboxKeyTyped(typedChar, keyCode)) {
            updateCreateButton();
        }
        if (keyCode == 1) {                                          // ESC
            if (!this.busy) {
                this.mc.displayGuiScreen(this.parentScreen);
            }
        } else if (keyCode == Keyboard.KEY_TAB) {
            this.nameField.setFocused(!this.nameField.isFocused());
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();                                // GuiScreen.b
        this.drawCenteredString(this.fontRendererObj, "Create a new repository", this.width / 2, 17, 0xFFFFFF);
        // Client-modified FontRenderer: a(text, scale) = getStringWidth * scale (float), a(text, color, shadow, x, y, scale)
        String limit = "You have a limit of " + ChatColor.YELLOW + "1000 accounts" + ChatColor.RESET + " per repository.";   // ChatColor constants
        float limitWidth = this.fontRendererObj.getStringWidth(limit, 0.5F);
        this.fontRendererObj.drawString(limit, 0xFFFFFF, true, (float) this.width * 0.5F - limitWidth * 0.5F, 28.5F, 0.5F);
        String nameLabel = "Repository name " + ChatColor.DARK_GRAY + "(" + this.nameField.getText().length() + "/26)";
        this.fontRendererObj.drawString(nameLabel, 10526880, true, (float) (this.width / 2 - 100), 54.0F, 1.0F);
        this.nameField.drawTextBox();
        if (this.statusMessage != null && this.messageAlpha >= 2) {
            GlStateManager.enableBlend();
            this.drawCenteredString(this.fontRendererObj, this.statusMessage, this.width / 2, 40,
                    new Color(190, 50, 50, this.messageAlpha).getRGB());
            GlStateManager.disableBlend();
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.authorized = true;
        if (this.obf72 == null) this.obf72 = new ArrayList<>();
        this.obf72.clear();
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 114, "Create"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 138, "Cancel"));
        this.nameField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 100, 66, 200, 20);
        this.nameField.setMaxStringLength(26);
        this.nameField.setFocused(true);
        updateCreateButton();
    }

    private void createRepository(String name) {
        if (this.busy) {
            return;
        }
        this.busy = true;
        try {
            var store = com.adjustclient.ui.account.LocalAccountStore.INSTANCE;
            store.open(Adjust.INSTANCE.accountManager);
            Adjust.INSTANCE.accountManager.currentRepository = store.create(name);
            store.selected(Adjust.INSTANCE.accountManager.currentRepository);
            this.busy = false;
            this.mc.displayGuiScreen(this.parentScreen);
        } catch (RuntimeException error) {
            this.busy = false;
            this.messageAlpha = 255;
            this.statusMessage = "Creation failed! " + error.getMessage();
            this.messageTimer = new Timer();
            this.refreshRequested = true;
        }
    }

    private void updateCreateButton() {
        GuiButton createButton = (GuiButton) this.buttonList.get(0);
        boolean valid;
        if (this.statusMessage != null) {
            valid = false;
        } else if (this.authorized) {
            if (this.busy) {
                valid = false;
            } else {
                String text = this.nameField.getText();
                int length = text.length();
                if (length < 3) {
                    valid = false;
                } else if (length < 27) {
                    valid = text.matches("^[a-zA-Z0-9-\\s]*$");
                } else {
                    valid = false;
                }
            }
        } else {
            valid = false;
        }
        createButton.enabled = valid;
    }

    private static final class CreateRepositoryCallback implements Consumer<BackendResponse> {
        private final CreateRepositoryScreen screen;

        CreateRepositoryCallback(CreateRepositoryScreen screen) {
            this.screen = screen;
        }

        @Override
        public void accept(BackendResponse response) {
            CreateRepositoryScreen s = this.screen;
            if (response.status != ResponseStatus.OK) {      // not the success status
                s.messageAlpha = 255;
                s.statusMessage = "Creation failed!";
                s.messageTimer.reset();                               // lastMs = System.currentTimeMillis()
                s.refreshRequested = true;
                s.busy = false;
                return;
            }
            s.busy = false;
            if (s.mc.currentScreen != null && s.mc.currentScreen.getClass() == CreateRepositoryScreen.class) {
                s.mc.displayGuiScreen(s.parentScreen);
            }
        }
    }
}
