package com.adjustclient.ui.clickgui.frame;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.event.Render2DEvent;
import com.adjustclient.render.animation.TimedFade;
import com.adjustclient.service.ScreenStack;
import com.adjustclient.service.config.ConfigurationApplier;
import com.adjustclient.service.config.ConfigurationManager;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.ui.clickgui.ClickGuiComponent;
import com.adjustclient.ui.clickgui.ClickGuiScreen;
import com.adjustclient.ui.clickgui.overlay.BindOverlay;
import com.adjustclient.ui.dialog.DialogBuilder;
import com.adjustclient.ui.dialog.DialogScreen;
import com.adjustclient.util.InitPhase;
import com.adjustclient.util.Timer;
import com.adjustclient.util.Vec2f;

public final class ConfigurationEntry extends ClickGuiComponent {

    protected final ConfigurationManager configuration;
    protected final HoverEffect labelHover;

    public ConfigurationEntry(ConfigurationManager configuration) {
        super();
        this.configuration = configuration;
        this.modifiers.add(new HoverBackground(this));                           // 1: hover background (rgb 53,53,53, alpha <= 69)
        BooleanSupplier isLoaded = () -> configuration.id == Adjust.INSTANCE.screenStack.configurations.selected.id;
        this.labelHover = new HoverEffect(this, isLoaded, HoverEffect.hoveredOf(this), 0.2F, 1.0F);
        this.modifiers.add(this.labelHover);
        this.modifiers.add(new LastUsedTooltip(this, configuration, HoverEffect.hoveredOf(this)));
        HoverEffect deleteHover = new HoverEffect(this, DeleteConfigButton.NEVER_ACTIVE, HoverEffect.hoveredOf(this), 0.05F, 1.0F);
        this.modifiers.add(deleteHover);
        HoverEffect renameHover = new HoverEffect(this, RenameConfigButton.NEVER_ACTIVE, HoverEffect.hoveredOf(this), 0.05F, 1.0F);
        this.modifiers.add(renameHover);
        HoverEffect shareHover = new HoverEffect(this, ShareConfigButton.NEVER_ACTIVE, HoverEffect.hoveredOf(this), 0.05F, 1.0F);
        this.modifiers.add(shareHover);
        // 7: header buttons, left to right in the list = right to left on screen (the bar places the first one right-most)
        List<HeaderButton> buttons = new ArrayList<>(Arrays.asList(
                new DeleteConfigButton(deleteHover, this),
                new RenameConfigButton(renameHover, this),
                new ShareConfigButton(shareHover, this)));
        this.modifiers.add(new ButtonBar(this, buttons));
    }

    @Override
    public void renderContent(Render2DEvent queue, Vec2f mouse) {
        float x = this.x + 2.0F;
        float y = this.y + 4.0F;
        int color = this.labelHover.color(190);
        Fonts.TAHOMA_12.drawString(this.configuration.name, x, y, color);
    }

    @Override
    public String getSearchText() {
        return this.configuration.name;
    }

    @Override
    public float getHeight() {
        return 11.0F;
    }

    @Override
    public boolean onMousePressed(Vec2f mouse, int button, boolean pressed) {
        if (button == 0 && pressed) {
            Adjust adjust = Adjust.INSTANCE;
            boolean unsaved = adjust.fileSystem.dirtyTimer != null;
            if (!unsaved) {
                if (adjust.screenStack.configurations.selected.id == this.configuration.id) {
                    return true;                                         // already the loaded configuration
                }
                this.configuration.load();
                return true;
            }
            DialogBuilder dialog = new DialogBuilder("Are you sure?");
            dialog.message.append("You have unsaved changes to ");
            dialog.message.italic = true;
            dialog.message.append(adjust.screenStack.configurations.selected.name);
            dialog.message.append(" and loading a different configuration will overwrite your current changes.");
            Consumer<DialogScreen> save = result -> {
                // saves the loaded configuration through the configuration manager, then runs loadThis (below) when the save finished
                Adjust.INSTANCE.fileSystem.save(null, this::loadThis);
            };
            Consumer<DialogScreen> discard = result -> this.loadThis();
            Function<DialogScreen, Boolean> style = DialogBuilder.ALWAYS_ENABLED;
            dialog.addButton("Save", save, style);
            dialog.addButton("Discard", discard, style);
            dialog.addButton("Cancel", null, style);
            dialog.build();
            return true;
        }
        if (button != 2 || !pressed) {
            return false;
        }
        Bind bind = UserConfiguration.INSTANCE.configurationBinds.getOrCreate(this.configuration.id).bind;   // the Bind of the configuration
        ClickGuiScreen screen = ClickGuiScreen.instance();
        Consumer<Object> addKey = key -> bind.addKey((com.adjustclient.bind.BindKey) key);
        BindOverlay overlay = new BindOverlay(
                new Animation(Easing.LINEAR, 150L),
                new TimedFade(1050L, 500L, 1000L),
                addKey,
                this.configuration.name,
                new Timer());
        overlay.pressedText = BindOverlay.formatKeys(bind.keys);   // pressedText = BindOverlay.formatKeys(bind keys)
        screen.addOverlay(overlay);
        return false;
    }

    private void loadThis() {
        this.configuration.load();
    }
}
