package com.adjustclient.ui.clickgui.frame;

import com.adjustclient.render.font.Fonts;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import com.adjustclient.Adjust;
import com.adjustclient.backend.api.response.BackendResponse;
import com.adjustclient.logging.Logger;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.service.ScreenStack;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.ui.dialog.DialogBuilder;
import com.adjustclient.ui.dialog.DialogScreen;
import com.adjustclient.ui.dialog.TextInputElement;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.ui.hud.notification.Notifications;

public final class RenameConfigButton extends HeaderButton {

    protected HoverEffect hover;

    static final BooleanSupplier NEVER_ACTIVE = () -> false;
    protected ConfigurationEntry owner;
    protected Object unused32;
    protected Object unused36;

    public RenameConfigButton(HoverEffect hover, ConfigurationEntry owner) {
        this.width = 9.5F;
        this.height = 11.0F;
        this.drawImmediately = true;
        this.hover = hover;
        this.owner = owner;
    }

    @Override
    public boolean onClick() {
        com.adjustclient.service.config.ConfigurationManager configuration = this.owner.configuration;
        DialogBuilder dialog = new DialogBuilder("Rename configuration file '" + configuration.name + "'");
        dialog.message.append("Enter a new name for ");
        dialog.message.italic = true;                                          // emphasised
        dialog.message.append(configuration.name != null ? configuration.name : "null");
        dialog.message.append(".");
        dialog.addTextInput("Name", "", 16, 128);
        Function<DialogScreen, Boolean> validator = RenameConfigButton::validateName;
        Consumer<DialogScreen> rename = result -> renameLocal(lastInputText(result));
        dialog.addButton("Rename", rename, validator);
        dialog.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
        dialog.build(133.0F);
        return true;
    }

    private static Boolean validateName(DialogScreen dialogState) {
        String text = lastInputText(dialogState);   // text of the input of the dialog
        if (text == null) {
            return Boolean.FALSE;
        }
        if (text.length() != 0 && text.length() <= 16
                && java.util.regex.Pattern.matches("^[a-zzA-Z0-9 !@#$%^&~*_\\-=+,./\"';:]*$", text)) {
            if (text.startsWith(" ")) {
                return Boolean.FALSE;
            }
            if (text.endsWith(" ")) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private void renameLocal(String newName) {
        com.adjustclient.service.config.ConfigurationService service = Adjust.INSTANCE.screenStack.configurations;
        if (service != null && service.renameLocal(this.owner.configuration, newName)) {
            return;
        }
        NotificationBuilder notification = new NotificationBuilder("Configuration");
        notification.addText("Failed to rename configuration ", 0xFFBEBCBD);
        notification.append(this.owner.configuration.name);
        notification.addText(".", 0xFFBEBCBD);
        HUDModule.INSTANCE_ALT.notifications.add(notification.build());
    }

    @SuppressWarnings("unused")
    private void vendorRename(String newName) {
        var request = new com.adjustclient.backend.api.request.config.GetFeaturedConfigurationsRequest(
                this.owner.configuration, newName);
        request.toPacket().sendAsync(this::vendorOnRenameResult);
    }

    @SuppressWarnings("unused")
    private void vendorOnRenameResult(BackendResponse answer) {
        if (answer.status == com.adjustclient.backend.api.response.ResponseStatus.OK) return;
        NotificationBuilder notification = new NotificationBuilder("Configuration");
        notification.addText("Failed to rename configuration ", 0xFFBEBCBD);
        notification.append(this.owner.configuration.name);
        notification.addText(".", 0xFFBEBCBD);
        HUDModule.INSTANCE_ALT.notifications.add(notification.build());
        Logger.log(com.adjustclient.logging.LogLevel.ERROR, true, "Failed to rename configuration {} due to: {}",
                new Object[] {this.owner.configuration.name, answer.status});
    }

    @Override
    public void render() {
        Fonts.ICONS_14.drawStringNoShadow("b", this.x + 2.0F, this.y + 4.0F, this.hover.color(190));
    }

    @Override
    public boolean isShown() {
        return this.owner.isHovered();
    }

    private static String lastInputText(DialogScreen dialogState) {
        TextInputElement element = dialogState.findTextInput(0);
        return element == null ? null : element.box.input.field.getText();
    }
}
