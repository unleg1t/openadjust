package com.adjustclient.core.gui.clickgui.frame;

import com.adjustclient.core.rendering.font.Fonts;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.GuiScreen;
import com.adjustclient.Adjust;
import com.adjustclient.utilities.backend.api.response.BackendResponse;
import com.adjustclient.utilities.backend.api.response.EmptyResponseBody;
import com.adjustclient.utilities.backend.api.response.ResponseStatus;
import com.adjustclient.managers.ScreenStack;
import com.adjustclient.managers.config.user.UserConfiguration;
import com.adjustclient.core.gui.dialog.DialogBuilder;
import com.adjustclient.core.gui.dialog.DialogScreen;

public final class DeleteConfigButton extends HeaderButton {

    protected HoverEffect hover;

    static final BooleanSupplier NEVER_ACTIVE = () -> false;
    protected ConfigurationEntry owner;
    protected Object unused32;
    protected Object unused36;

    public DeleteConfigButton(HoverEffect hover, ConfigurationEntry owner) {
        this.width = 9.5F;
        this.height = 11.0F;
        this.drawImmediately = true;
        this.hover = hover;
        this.owner = owner;
    }

    @Override
    public boolean onClick() {
        com.adjustclient.managers.config.ConfigurationManager configuration = this.owner.configuration;
        if (Adjust.INSTANCE.screenStack.configurations.selected.id == configuration.id) {
            return true;                                   // the loaded configuration cannot be deleted
        }
        if (GuiScreen.isShiftKeyDown()) {
            this.deleteConfiguration();                     // bypass the confirmation
            return true;
        }
        String name = configuration.name != null ? configuration.name : "null";
        DialogBuilder dialog = new DialogBuilder("Delete configuration file '" + configuration.name + "'?");
        dialog.message.append("Are you sure you want to delete ");
        dialog.message.italic = true;                                        // next text is emphasised (ChatStyle italic = TRUE)
        dialog.message.append(name);
        dialog.message.append("? This action cannot be undone.");
        Consumer<DialogScreen> delete = result -> this.deleteConfiguration();
        Function<DialogScreen, Boolean> style = DialogBuilder.ALWAYS_ENABLED;
        dialog.addButton("Delete", delete, style);
        dialog.addButton("Cancel", null, style);
        dialog.build(70.0F);
        return true;
    }

    private void deleteConfiguration() {
        com.adjustclient.managers.config.ConfigurationService service = Adjust.INSTANCE.screenStack.configurations;
        if (service != null) {
            service.deleteLocal(this.owner.configuration);
        }
    }

    @SuppressWarnings("unused")
    private void vendorDeleteConfiguration() {
        new com.adjustclient.utilities.backend.api.request.config.DeleteConfigurationRequest(this.owner.configuration)
                .toPacket().sendAsync(null);
    }

    @Override
    public void render() {
        float x = this.x + 1.75F;
        float y = this.y + 4.0F;
        int color = this.hover.tint(GuiScreen.isShiftKeyDown() ? 255 : 155);   // {red, 155, 155} + 110 * hover, alpha 165 + 48 * hover
        Fonts.ICONS_14.drawStringNoShadow("c", x, y, color);
    }

    @Override
    public boolean isShown() {
        return this.owner.isHovered();
    }
}
