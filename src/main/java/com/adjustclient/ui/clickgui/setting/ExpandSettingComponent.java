package com.adjustclient.ui.clickgui.setting;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.setting.ExpandSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.ui.clickgui.ClickGuiComponent;
import com.adjustclient.ui.clickgui.frame.ButtonBar;
import com.adjustclient.ui.clickgui.frame.ExpandButton;
import com.adjustclient.ui.clickgui.frame.HeaderButton;
import com.adjustclient.ui.clickgui.frame.HoverEffect;
import com.adjustclient.ui.dialog.AdjustButton;
import com.adjustclient.ui.dialog.DialogBuilder;
import com.adjustclient.ui.dialog.DialogScreen;

public final class ExpandSettingComponent extends SettingComponent<ExpandSetting> {

    HoverEffect labelColor;
    public Animation expandAnimation;
    Object obf72;

    public ExpandSettingComponent(ExpandSetting setting) {
        super(setting);
        this.expandAnimation = new Animation(Easing.LINEAR, 100L);
        this.labelColor = new HoverEffect(this, () -> groupEnabled(setting), HoverEffect.hoveredOf(this), 0.3F, 0.89F);
        this.modifiers.add(this.labelColor);
        // the "+" / "-" button at the right end of the header
        ExpandButton expand = new ExpandButton(this);
        this.modifiers.add(new ButtonBar(this, new ArrayList<HeaderButton>(Collections.<HeaderButton>singletonList(expand))));
    }

    static boolean groupEnabled(ExpandSetting setting) {
        BooleanSupplier condition = setting.enabledPredicate;
        return condition == null || condition.getAsBoolean();
    }

    @Override
    public void renderContent(com.adjustclient.event.Render2DEvent queue, com.adjustclient.util.Vec2f mouse) {
        float target = ClickGuiComponent.isFullyExpanded(this) ? 1.0F : 0.0F;
        this.expandAnimation.update(target);
        String label = this.getLabel();
        float centreX = this.width / 2.0F + this.x;
        float textY = this.y + 4.0F;
        Color accent = HUDModule.getHudColor();
        int grey = this.labelColor.color((int) (55.0F * this.expandAnimation.value + 145.0F));
        double t = Math.max(0.0F, Math.min(1.0F, 0.13F * this.expandAnimation.value));
        double s = 1.0 - t;
        int r = (int) ((double) accent.getRed() * t + (double) ((grey | 0xFF000000) >> 16 & 0xFF) * s);
        int g = (int) ((double) accent.getGreen() * t + (double) (grey >> 8 & 0xFF) * s);
        int b = (int) ((double) accent.getBlue() * t + (double) (grey & 0xFF) * s);
        int a = (int) ((double) accent.getAlpha() * t + s * 255.0);
        int color = new Color(r, g, b, a).getRGB();
        Fonts.TAHOMA_10.drawString(label, centreX - Fonts.TAHOMA_10.getStringWidth(label) * 0.5F, textY, color);
    }

    @Override
    public boolean onMousePressed(com.adjustclient.util.Vec2f mouse, int button, boolean pressed) {
        if (!pressed || button != 0) {
            return false;
        }
        if (GuiScreen.isCtrlKeyDown()) {
            String label = this.getLabel();
            DialogBuilder dialog = new DialogBuilder("Reset setting group " + label + " to default?");
            dialog.message.append("All settings under ");
            dialog.message.italic = true;                                         // the label is emphasised
            dialog.message.append(label);
            dialog.message.append(" will be reset to their defaults if you continue.");
            Consumer<DialogScreen> cancelAction = d -> { };
            Consumer<DialogScreen> resetAction = d -> com.adjustclient.command.impl.ConfigCommand.resetSettings(this.setting); // resetSettings
            dialog.addButton("Cancel", cancelAction, DialogBuilder.ALWAYS_ENABLED);
            dialog.addButton("Reset Settings", resetAction, DialogBuilder.ALWAYS_ENABLED);
            dialog.build();
            return true;
        }
        this.toggleExpanded();
        return true;
    }

    public boolean isHovered() {
        return this.hovered;
    }

    @Override
    public float getHeight() {
        return 9.0F;
    }
}
