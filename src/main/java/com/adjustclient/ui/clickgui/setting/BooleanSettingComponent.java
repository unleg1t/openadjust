package com.adjustclient.ui.clickgui.setting;

import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.setting.BooleanSetting;
import java.awt.Color;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatColor;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.ui.clickgui.frame.HoverEffect;

public final class BooleanSettingComponent extends SettingComponent<BooleanSetting> {

    HoverEffect labelColor;

    public BooleanSettingComponent(BooleanSetting setting) {
        super(setting);
        this.labelColor = new HoverEffect(this, () -> activePredicate(setting), HoverEffect.hoveredOf(this), 0.0F, 0.6F);
        this.modifiers.add(this.labelColor);
    }

    static boolean activePredicate(BooleanSetting setting) {
        if (!setting.getValue().booleanValue()) {
            return false;
        }
        java.util.function.BooleanSupplier hideIf = setting.hideIf;
        if (hideIf == null) {
            return true;
        }
        return !hideIf.getAsBoolean();
    }

    @Override
    public void renderContent(com.adjustclient.event.Render2DEvent queue, com.adjustclient.util.Vec2f mouse) {
        BooleanSetting setting = this.setting;
        String label = this.getLabel();
        String text = label.length() != 0 ? label + ":" : ":";
        String fitted = this.fitLabel(text, 25.0F);
        boolean enabled = activePredicate(setting);
        String stateLabel = enabled ? setting.getOnLabel() : setting.getOffLabel();
        String full = fitted + " " + ChatColor.BOLD + stateLabel;
        int textColor = this.labelColor.color(190);
        float animated = this.labelColor.k().value;
        Color accent = HUDModule.getHudColor();
        int alpha = (int) (255.0F * animated);
        alpha = Math.max(0, Math.min(255, alpha));
        int boxColor = accent.getAlpha() != alpha
                ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), alpha).getRGB()
                : accent.getRGB();
        this.drawCheckBox(full, textColor, boxColor);
    }

    @Override
    public boolean onMousePressed(com.adjustclient.util.Vec2f mouse, int button, boolean pressed) {
        if (button == 0 && pressed) {
            if (GuiScreen.isCtrlKeyDown()) {
                this.openResetPrompt();
                return true;
            }
            BooleanSetting setting = this.setting;
            boolean enabled = activePredicate(setting);
            setting.setValue(Boolean.valueOf(!enabled)); // only stores/fires when the boxed value differs from the stored one
            return true;
        }
        return false;
    }

    @Override
    public float getHeight() {
        return 9.0F;
    }
}
