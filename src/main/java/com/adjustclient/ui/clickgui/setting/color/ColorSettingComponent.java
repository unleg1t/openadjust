package com.adjustclient.ui.clickgui.setting.color;

import com.adjustclient.setting.ColorSetting;
import com.adjustclient.setting.Setting;
import com.adjustclient.setting.SettingContainer;
import com.adjustclient.ui.clickgui.setting.SettingComponent;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.util.Vec2f;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiScreen;
import com.adjustclient.ui.clickgui.frame.ButtonBar;
import com.adjustclient.ui.clickgui.frame.ExpandButton;
import com.adjustclient.ui.clickgui.frame.HeaderButton;
import com.adjustclient.ui.clickgui.frame.HoverEffect;
import com.adjustclient.ui.clickgui.setting.BooleanSettingComponent;

public final class ColorSettingComponent extends SettingComponent<ColorSetting> implements SettingContainer {

    private HoverEffect swatch;
    private HoverEffect hoverColor;
    private List<Setting<?>> settings;
    private Object unused76;

    public ColorSettingComponent(ColorSetting setting) {
        super(setting);
        this.settings = new ArrayList<>();
        BooleanSupplier isOpen = () -> this.isFullyExpanded();
        BooleanSupplier always = () -> true;
        this.swatch = new HoverEffect(this, always, HoverEffect.hoveredOf(this), 0.0F, 0.79F);
        this.modifiers.add(this.swatch);
        this.hoverColor = new HoverEffect(this, isOpen, HoverEffect.hoveredOf(this), 0.2F, 1.0F);
        this.modifiers.add(this.hoverColor);
        // the "+" / "-" button at the right end of the row
        ExpandButton expand = new ExpandButton(this);
        this.modifiers.add(new ButtonBar(this, new ArrayList<HeaderButton>(Collections.<HeaderButton>singletonList(expand))));
    }

    private void applyText(String text) {
        int argb;
        try {
            argb = Integer.decode(text.toLowerCase());
        } catch (NumberFormatException e) {
            new NotificationBuilder("Invalid Value")
                    .addText("Value ", 0xFFBEBCBD)
                    .append(text)
                    .addText(" could not be converted into a color.", 0xFFBEBCBD)
                    .show();
            return;
        }
        ColorSetting setting = this.setting;
        if (!setting.alpha /* has alpha */) {
            argb |= 0xFF000000;
        }
        Color old = setting.getValue();
        setting.previousValue = old;               // previousValue
        Color color = new Color(argb, true);
        setting.value = color;
        setting.notifyListeners(old, color);
    }

    @Override
    public void renderContent(com.adjustclient.event.Render2DEvent queue, Vec2f mouse) {
        String label = this.getLabel();
        int textColor = this.hoverColor.color(190);
        int swatchFill = this.swatch.withAlpha(this.setting.getColor());
        this.drawCheckBox(label, textColor, swatchFill);
    }

    @Override
    public boolean onMousePressed(Vec2f mouse, int button, boolean pressed) {
        if (!pressed) {
            if (button != 2) {
                return false;
            }
            Consumer<String> onSet = this::applyText;
            this.openValuePrompt(onSet);
            return false;
        }
        if (button == 0) {
            if (GuiScreen.isCtrlKeyDown()) {
                this.openResetPrompt();
            } else {
                this.toggleExpanded();
            }
            return true;
        }
        return false;
    }

    public List<Setting<?>> getSettings() {
        return this.settings;
    }

    @Override
    public float getHeight() {
        return 9.0F;
    }
}
