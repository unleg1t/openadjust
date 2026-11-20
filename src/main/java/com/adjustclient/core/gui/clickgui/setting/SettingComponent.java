package com.adjustclient.core.gui.clickgui.setting;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgFontRenderer;
import com.adjustclient.core.settings.Setting;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import com.adjustclient.modules.impl.movement.inventorymove.InventoryMoveModule;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.rendering.font.TextComponent;
import com.adjustclient.core.rendering.font.TextInput;
import com.adjustclient.core.gui.clickgui.ClickGuiComponent;
import com.adjustclient.core.gui.clickgui.frame.HoverBackground;
import com.adjustclient.core.gui.clickgui.frame.Tooltip;
import com.adjustclient.core.gui.dialog.AdjustButton;
import com.adjustclient.core.gui.dialog.DialogBuilder;
import com.adjustclient.core.gui.dialog.DialogScreen;
import com.adjustclient.core.gui.dialog.TextInputElement;
import com.adjustclient.core.gui.screen.AdjustComponent;

public abstract class SettingComponent<T extends Setting<?>> extends ClickGuiComponent {

    protected final T setting;
    protected final HoverBackground hover;
    private boolean fullLabelTooltipAdded;

    public SettingComponent(T setting) {
        super();
        this.setting = setting;
        this.hover = new HoverBackground(this);
        this.modifiers.add(this.hover); // children.add(hover)
        String description = setting.description;
        if (description != null) {
            this.modifiers.add(new Tooltip(this, description, this::isHovered));
        }
    }

    public boolean isVisible(int stamp) {
        if (stamp == this.visibilityStamp) {
            if (!this.cachedVisible) {
                return false;
            }
        } else {
            this.visibilityStamp = stamp;
            ClickGuiComponent parent = this.parent;
            boolean visible = parent == null ? true : parent.computeVisible(stamp);
            this.cachedVisible = visible;
            if (!visible) {
                return false;
            }
        }
        BooleanSupplier hideIf = this.setting.hideIf;
        if (hideIf == null) {
            return true;
        }
        return !hideIf.getAsBoolean();
    }

    public String getLabel() {
        return this.setting.getLabel();
    }

    @Override
    public String getSearchText() {
        return this.getLabel();
    }

    protected String fitLabel(String text, float reservedWidth) {
        float maxWidth = this.bounds.width - reservedWidth;
        if (maxWidth < 0.0F) { // (also normalises -0.0F)
            maxWidth = 0.0F;
        }
        NvgFontRenderer font = Fonts.TAHOMA_10;
        String fitted = font.trimStringToWidth(text, maxWidth, false); // trimStringToWidth
        if (fitted == text || fitted.equals(text)) {
            return fitted;
        }
        if (!this.fullLabelTooltipAdded) {
            this.fullLabelTooltipAdded = true;
            String full = text.substring(0, text.length() - 1).replace("Dist.", "Distance");
            this.modifiers.add(new Tooltip(this, 700L, full, null));
        }
        String trimmed = fitted.trim();
        if (trimmed.length() == 0) {
            return "...";
        }
        return trimmed + "...";
    }

    protected void drawCheckBox(String text, int textColor, int boxFill) {
        float x1 = this.bounds.x + 2.0F;
        float y1 = this.y + 2.0F;
        float x2 = x1 + 5.5F;
        float y2 = y1 + 5.5F;
        com.adjustclient.core.rendering.font.image.NvgDraw.fillRect(0x620F0F13, x1, y1, x2, y2);
        com.adjustclient.core.rendering.font.image.NvgDraw.fillRect(boxFill, x1 + 0.5F, y1 + 0.5F, x2 - 0.5F, y2 - 0.5F);
        Fonts.TAHOMA_10.drawString(text, x1 + 7.0F, this.y + 4.0F, textColor);
    }

    protected void openValuePrompt(Consumer<String> onSet) {
        String label = this.getLabel();
        DialogBuilder dialog = new DialogBuilder("Set value of " + label);   // 'Set value of '
        dialog.message.append("Enter a new value for ");
        dialog.message.italic = true;                                         // the label is emphasised
        dialog.message.append(label);
        dialog.message.append(".");
        dialog.addTextInput("Value", "", 10, 120);   // text input "Value" (Tahoma 12, 10 characters, 120 wide); build() focuses it and releases the held keys
        Consumer<DialogScreen> setAction = d -> {
            TextInputElement row = d.findTextInput(0);
            String text = row == null ? null : row.box.input.field.getText();
            if (text == null) {
                com.adjustclient.core.gui.hud.notification.NotificationBuilder notification =
                        new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Invalid Value");
                notification.lifetimeSteps = 57;
                notification.addText("No value could be found, that's weird!", 0xFFBEBCBD).show();
            } else {
                onSet.accept(text);
            }
        };
        dialog.addButton("Set", setAction, DialogBuilder.ALWAYS_ENABLED);
        dialog.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
        dialog.build(150.0F);
    }

    public void openResetPrompt() {
        String label = this.getLabel();
        DialogBuilder dialog = new DialogBuilder("Reset setting " + label + " to default?");
        dialog.message.append("The setting ");
        dialog.message.italic = true;                                         // the label is emphasised
        dialog.message.append(label);
        dialog.message.append(" will be reset to default if you continue.");
        Consumer<DialogScreen> cancelAction = d -> { };
        Consumer<DialogScreen> resetAction = d -> this.setting.reset();
        dialog.addButton("Cancel", cancelAction, DialogBuilder.ALWAYS_ENABLED);
        dialog.addButton("Reset Setting", resetAction, DialogBuilder.ALWAYS_ENABLED);
        dialog.build();
    }
}
