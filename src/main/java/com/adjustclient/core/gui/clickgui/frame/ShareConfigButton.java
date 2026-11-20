package com.adjustclient.core.gui.clickgui.frame;

import com.adjustclient.core.rendering.font.Fonts;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import com.adjustclient.Adjust;
import com.adjustclient.utilities.backend.api.response.BackendResponse;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.managers.ScreenStack;
import com.adjustclient.managers.config.user.UserConfiguration;
import com.adjustclient.core.gui.dialog.DialogBuilder;
import com.adjustclient.core.gui.dialog.DialogScreen;
import com.adjustclient.core.gui.dialog.TextInputElement;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.core.gui.hud.notification.Notifications;
import com.adjustclient.managers.user.ClientUser;
import com.adjustclient.utilities.ErrorReport;
import com.adjustclient.utilities.InitPhase;

public final class ShareConfigButton extends HeaderButton {

    protected HoverEffect hover;

    static final BooleanSupplier NEVER_ACTIVE = () -> false;
    protected ConfigurationEntry owner;
    protected Object unused32;
    protected Object unused36;

    public ShareConfigButton(HoverEffect hover, ConfigurationEntry owner) {
        this.width = 9.5F;
        this.height = 11.0F;
        this.drawImmediately = true;
        this.hover = hover;
        this.owner = owner;
    }

    @Override
    public boolean onClick() {
        com.adjustclient.managers.config.ConfigurationManager configuration = this.owner.configuration;
        DialogBuilder dialog = new DialogBuilder("Share configuration file '" + configuration.name + "'");
        dialog.message.append("Enter the Adjust user to receive ");
        dialog.message.italic = true;                                           // emphasised
        dialog.message.append(configuration.name != null ? configuration.name : "null");
        dialog.message.append(".");
        dialog.addTextInput("Username", "", 16, 128);   // label "Username", Fonts.TAHOMA_12, at most 16 characters
        Function<DialogScreen, Boolean> validator = ShareConfigButton::validateUsername;
        Consumer<DialogScreen> share = result -> {
            String username = lastInputText(result);   // text of the dialog's input
            Adjust client = Adjust.INSTANCE;
            client.field40.lookup(username, profile -> this.onUserLookup(username, profile));
        };
        dialog.addButton("Share", share, validator);
        dialog.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
        dialog.build(133.0F);
        return true;
    }

    private static Boolean validateUsername(DialogScreen dialogState) {
        String text = lastInputText(dialogState);   // text of the dialog's input
        if (text == null || text.length() < 3 || text.length() > 16) {
            return Boolean.FALSE;
        }
        return java.util.regex.Pattern.matches("^[a-zA-Z0-9]*$", text) ? Boolean.TRUE : Boolean.FALSE;
    }

    private static String lastInputText(DialogScreen dialogState) {
        TextInputElement element = dialogState.findTextInput(0);
        return element == null ? null : element.box.input.field.getText();
    }

    private void onUserLookup(String username, Object profile) {
        if (profile != null && ((ClientUser) profile).useClientColor) {
            new NotificationBuilder("Configuration").format("You cannot share a configuration with yourself.").show();
            return;
        }
        new NotificationBuilder("Configuration")
                .addText("Configuration sharing is unavailable offline.", 0xFFBEBCBD)
                .show();
    }

    @SuppressWarnings("unused")
    private void vendorOnUserLookup(String username, Object profile) {
        if (profile == null) {
            NotificationBuilder n = new NotificationBuilder("Configuration");   // new notification "Configuration"
            n = n.addText("No user by the name ", 0xFFBEBCBD);
            n = n.append(username);
            n = n.addText(" exists.", 0xFFBEBCBD);
            n.show();                                               // show
            return;
        }
        if (((ClientUser) profile).useClientColor) {
            new NotificationBuilder("Configuration").format("You cannot share a configuration with yourself.").show();
            return;
        }
        ClientUser target = (ClientUser) profile;
        new com.adjustclient.utilities.backend.api.request.friends.UnblockUserRequest(this.owner.configuration, target)
                .toPacket()
                .sendAsync(response -> {
                    if (response.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.OK) {
                        showNotification(new ErrorReport("Shared configuration {} with {}! They have 30 seconds to accept it.", this.owner.configuration.name, target.name));
                    } else if (response.body instanceof com.adjustclient.utilities.backend.api.response.EnumResponseBody
                            && ((com.adjustclient.utilities.backend.api.response.EnumResponseBody) response.body).value instanceof com.adjustclient.utilities.backend.api.error.ConfigurationError) {
                        var error = (com.adjustclient.utilities.backend.api.error.ConfigurationError) ((com.adjustclient.utilities.backend.api.response.EnumResponseBody) response.body).value;
                        showNotification(error.handler.apply(target.name));
                    } else {
                        showNotification(new ErrorReport("Failed to share configuration with {}.", target.name));
                    }
                });
    }

    private void showMessage(Object answer) {
        ErrorReport report = (ErrorReport) answer;
        NotificationBuilder n = new NotificationBuilder("Configuration");
        n.format(report.message, report.arguments);
        n.show();
    }

    private static void showNotification(ErrorReport message) {
        NotificationBuilder n = new NotificationBuilder("Configuration");
        n.format(message.message, message.arguments);
        n.show();
    }

    @Override
    public void render() {
        Fonts.ICONS_14.drawStringNoShadow("a", this.x + 1.5F, this.y + 4.0F, this.hover.color(190));
    }

    @Override
    public boolean isShown() {
        return this.owner.isHovered();
    }
}
