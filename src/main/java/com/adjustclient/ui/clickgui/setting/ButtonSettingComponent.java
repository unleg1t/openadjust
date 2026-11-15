package com.adjustclient.ui.clickgui.setting;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.setting.ButtonSetting;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.ui.clickgui.frame.HoverEffect;
import net.minecraft.util.ChatColor;

public final class ButtonSettingComponent extends SettingComponent<ButtonSetting> {

    HoverEffect labelColor;

    static final java.util.function.BooleanSupplier ALWAYS_ACTIVE = () -> true;

    public ButtonSettingComponent(ButtonSetting setting) {
        super(setting);
        this.labelColor = new HoverEffect(this, ALWAYS_ACTIVE, HoverEffect.hoveredOf(this), 0.2F, 1.0F);
        this.modifiers.add(this.labelColor);
    }

    @Override
    public void renderContent(com.adjustclient.event.Render2DEvent queue, com.adjustclient.util.Vec2f mouse) {
        String text = ChatColor.BOLD + this.getLabel();
        Color accent = HUDModule.getHudColor();
        Animation animation = this.labelColor.k();
        int alpha = (int) (170.0F * animation.value);
        alpha = Math.max(0, Math.min(255, alpha));
        int color = accent.getAlpha() == alpha ? accent.getRGB()
                : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), alpha).getRGB();
        float centreX = this.width / 2.0F + this.x; // width / 2 + x
        float textY = this.y + 4.0F;
        Fonts.TAHOMA_10.drawString(text, centreX - Fonts.TAHOMA_10.getStringWidth(text) * 0.5F, textY, color);
    }

    @Override
    public boolean onMousePressed(com.adjustclient.util.Vec2f mouse, int button, boolean pressed) {
        if (button != 0 || !pressed) {
            return false;
        }
        if (GuiScreen.isCtrlKeyDown()) {
            return true;
        }
        Minecraft mc = Minecraft.getMinecraft();
        Runnable action = (Runnable) this.setting.getValue();
        if (mc.isCallingFromMinecraftThread()) {
            action.run();
        } else {
            mc.addScheduledTask(action);
        }
        return true;
    }

    @Override
    public float getHeight() {
        return 9.0F;
    }
}
