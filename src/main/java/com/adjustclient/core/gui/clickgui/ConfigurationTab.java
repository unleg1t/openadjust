package com.adjustclient.core.gui.clickgui;

import com.adjustclient.Adjust;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgFontRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import com.adjustclient.utilities.backend.api.PendingRequest;
import com.adjustclient.utilities.backend.api.request.config.CreateConfigurationRequest;
import com.adjustclient.utilities.backend.api.response.BackendResponse;
import com.adjustclient.utilities.backend.api.response.EmptyResponseBody;
import com.adjustclient.utilities.backend.api.response.ResponseStatus;
import com.adjustclient.utilities.backend.api.response.TextResponseBody;
import com.adjustclient.managers.config.ConfigurationManager;
import com.adjustclient.managers.config.ConfigurationService;
import com.adjustclient.modules.impl.render.ClickGui;
import com.adjustclient.core.rendering.font.TextInput;
import com.adjustclient.core.rendering.font.TextInputBox;
import com.adjustclient.core.gui.clickgui.frame.ConfigurationEntry;
import com.adjustclient.core.gui.clickgui.frame.HoverEffect;
import com.adjustclient.core.gui.clickgui.frame.TabHeader;
import com.adjustclient.core.gui.dialog.AdjustButton;
import com.adjustclient.core.gui.dialog.DialogBuilder;
import com.adjustclient.core.gui.dialog.DialogScreen;
import com.adjustclient.core.gui.dialog.TextInputElement;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.core.gui.screen.transition.PopupTransition;
import com.adjustclient.utilities.HashUtil;
import com.adjustclient.utilities.Vec2f;

public final class ConfigurationTab extends ClickGuiTab {
    private TabHeader header;
    private int revision;
    private Object obf80;

    public ConfigurationTab(ClickGuiScreen screen, Vec2f pos) {
        this.entries = new ArrayList<>();
        this.renderList = new ArrayList<>();
        this.rowList = new ArrayList<>();
        this.layoutList = new ArrayList<>();
        this.scrollAnimation = new Animation(Easing.EASE_OUT_EXPO, 135L);
        this.title = "Configuration";
        this.screen = screen;
        this.width = 66.0F;
        this.id = HashUtil.hash("cat.cfg"); // custom hash (seed 64206, *33 + byte over the UTF-16BE bytes); the 7 bytes are stored as immediates
        this.x = pos.x;
        this.y = pos.y;
        TabHeader state = new TabHeader(this);
        this.state = state;
        state.expanded = true;
        state.titleHover = new HoverEffect(state, () -> this.screen.draggedTab == this, HoverEffect.hoveredOf(state), 0.4F, 0.7F);   // active = this tab is dragged
        state.modifiers.add(state.titleHover);
        state.gradientHover = new HoverEffect(state, () -> this.hovered, HoverEffect.hoveredOf(state), 0.5F, 1.0F);                // active = this tab is hovered
        state.modifiers.add(state.gradientHover);
        this.header = state;
        state.titleShift = 8.0F;
        AdjustButton plus = new AdjustButton(0, "+", 6, 6, 26, IconButtonModifier::multiplyAlpha, Fonts.TAHOMA_12);
        state.modifiers.add(new IconButtonModifier(state, plus, ConfigurationTab::openCreateDialog, 0.0F));
        AdjustButton f = new AdjustButton(0, "f", 6, 6, 26, IconButtonModifier::multiplyAlpha, Fonts.ICONS_12);
        state.modifiers.add(new IconButtonModifier(state, f, ConfigurationTab::runLambdaAc70, 8.0F));
        this.entries.add(state);
    }

    private static final byte[] NEW_CONFIG_MARKER = new byte[] { 'N', 'E', 'W', '!' };

    @SuppressWarnings("unused")
    private static void vendorNotifyUnexpectedError(Object source) {
        BackendResponse response = (BackendResponse) source;
        if (response.status == ResponseStatus.OK) {
            return;
        }
        if (response.body == null) {
            throw new NullPointerException();
        }
        String text = response.body instanceof TextResponseBody
                ? ((TextResponseBody) response.body).value
                : "Unexpected error.";
        new NotificationBuilder("Configuration").addText(text, 0xFFBEBCBD).show();
    }

    /** Creates a configuration from dialog input 0 using either the NEW! marker or {@link com.adjustclient.utilities.vfs.VirtualFileSystem#emptyConfig()}. */
    static Object applyConfig(Object dialog, Object request) {
        DialogScreen screen = (DialogScreen) dialog;
        String name = readConfigName(screen);
        byte[] data = ((Boolean) request) ? NEW_CONFIG_MARKER : Adjust.INSTANCE.fileSystem.emptyConfig();
        if (data == null) {
            return null;
        }
        return sendCreateRequest(name, data);
    }

    private static String readConfigName(DialogScreen screen) {
        for (TextInputElement input : screen.textInputs) {
            if (input.index == 0) {
                return input.box.input.field.getText();
            }
        }
        return null;
    }

    private static Object sendCreateRequest(String name, byte[] data) {
        ConfigurationService registry = Adjust.INSTANCE.screenStack.configurations;
        if (registry == null) return null;
        ConfigurationManager created = registry.createLocal(name, data);
        if (created == null) {
            new NotificationBuilder("Configuration")
                    .addText("Could not create configuration ", 0xFFBEBCBD).append(String.valueOf(name)).addText(".", 0xFFBEBCBD)
                    .show();
        } else {
            new NotificationBuilder("Configuration")
                    .addText("Created configuration ", 0xFFBEBCBD).append(created.name).addText(".", 0xFFBEBCBD)
                    .show();
        }
        return created;
    }

    @SuppressWarnings("unused")
    private static Object vendorSendCreateRequest(String name, byte[] data) {
        return new CreateConfigurationRequest(name, data).toPacket().sendAsync(ConfigurationTab::vendorOnRequestDone);
    }

    @Override
    public TabHeader getState() {
        return header;
    }

    @Override
    public ArrayList<ClickGuiComponent> getEntries() {
        com.adjustclient.Adjust.INSTANCE.screenStack.configurations.pollExternalChanges();
        if (com.adjustclient.Adjust.INSTANCE.screenStack.configurations.catalogHash != this.revision) {
            refreshEntries();
        }
        return entries;
    }

    /** Rebuilds the rows from the configuration registry and marks the parent screen dirty for re-layout. */
    public void refreshEntries() {
        ConfigurationService registry = Adjust.INSTANCE.screenStack.configurations;
        this.revision = registry.catalogHash;
        entries.clear();
        entries.add(header);
        for (ConfigurationManager configuration : registry.configurations) {
            entries.add(new ConfigurationEntry(configuration));
        }
        this.screen.layoutDirty = true;
    }

    public static void openCreateDialog() {
        DialogBuilder dialog = new DialogBuilder("Create a new configuration file");
        dialog.message.append("Enter a name for your new configuration:");
        dialog.addTextInput("Name", "", 16, 128);
        Function<DialogScreen, Boolean> valid = d -> {
            TextInputElement input = d.findTextInput(0);
            return input != null && validName(input.box.input.field.getText());
        };
        dialog.addButton("Create", ConfigurationTab::createConfig, valid);
        dialog.addButton("Create Empty", ConfigurationTab::createEmptyConfig, valid);
        dialog.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);               // always enabled
        dialog.bottomInset = 7;
        dialog.build(133.0F);   // shown on top of the Click GUI; build() focuses the name field and releases the held keys
    }

    /** Name validator: non-empty, at most 16 chars, allowed characters only, no leading or trailing spaces. */
    static boolean validName(String name) {
        if (name == null || name.isEmpty() || name.length() > 16) {
            return false;
        }
        return Pattern.matches("^[a-zzA-Z0-9 !@#$%^&~*_\\-=+,./\"';:]*$", name)
                && !name.startsWith(" ")
                && !name.endsWith(" ");
    }

    /** "Create": reads the entered name from the dialog and sends a create-configuration request with the NEW! marker. */
    static void createConfig(Object dialog) {
        if (!(dialog instanceof DialogScreen)) {
            throw new ClassCastException();
        }
        String name = readConfigName((DialogScreen) dialog);
        sendCreateRequest(name, NEW_CONFIG_MARKER);
    }

    /** "Create Empty": creates a local configuration using the empty snapshot bytes. */
    static void createEmptyConfig(Object dialog) {
        DialogScreen screen = (DialogScreen) dialog;
        TextInputElement input = screen.findTextInput(0);
        String name = input == null ? null : input.box.input.field.getText();
        byte[] cfg = com.adjustclient.Adjust.INSTANCE.fileSystem.emptyConfig();
        if (cfg == null) return;
        sendCreateRequest(name, cfg);
    }

    @SuppressWarnings("unused")
    private static void vendorCreateEmptyConfig(Object dialog) {
        DialogScreen screen = (DialogScreen) dialog;
        TextInputElement input = screen.findTextInput(0);
        String name = input == null ? null : input.box.input.field.getText();
        byte[] cfg = com.adjustclient.Adjust.INSTANCE.fileSystem.emptyConfig();
        if (cfg == null) return;
        Object request = new com.adjustclient.utilities.backend.api.request.config.CreateConfigurationRequest(name, cfg).toPacket();
        ((PendingRequest) request).sendAsync(ConfigurationTab::vendorOnRequestDone);
    }

    @SuppressWarnings("unused")
    private static void vendorOnRequestDone(Object result) {
        if (result != null && !(result instanceof BackendResponse)) {
            throw new ClassCastException();
        }
        vendorNotifyUnexpectedError(result);
    }

    static void onDialogResult(Object dialog, Object ok) {
        if ((dialog != null && !(dialog instanceof DialogScreen)) || (ok != null && !(ok instanceof Boolean))) {
            throw new ClassCastException();
        }
        applyConfig(dialog, ok);
    }

    /** After a configuration was created on the server: refreshes stale rows and appends a new {@link ConfigurationEntry}. */
    void onConfigCreated(Object config) {
        if (!(config instanceof ConfigurationManager)) {
            throw new ClassCastException();
        }
        if (Adjust.INSTANCE.screenStack.configurations.catalogHash != this.revision) {
            refreshEntries();
        }
        this.entries.add(new ConfigurationEntry((ConfigurationManager) config));
    }

    static void runLambdaAc70() {
        ClickGui.openGlobalConfigurations();
    }
}
