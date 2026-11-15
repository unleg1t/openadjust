package com.adjustclient.ui.tabgui.component;

import com.adjustclient.render.NvgRenderer;
import com.adjustclient.setting.BooleanSetting;
import net.minecraft.util.ChatColor;
import org.lwjgl.input.Keyboard;
import com.adjustclient.ui.tabgui.KeyPressHandler;
import com.adjustclient.ui.tabgui.SettingNode;
import com.adjustclient.render.font.Fonts;

public final class BooleanComponent extends SettingNode<BooleanSetting> implements KeyPressHandler {

    public BooleanComponent(String label, BooleanSetting setting) {
        super(label, setting);
    }

    @Override
    public boolean keyPressed(int key) {
        if (key == Keyboard.KEY_RIGHT || key == Keyboard.KEY_RETURN) {
            BooleanSetting setting = this.setting;
            setting.setValue(Boolean.valueOf(!setting.getAsBoolean()));
            return true;
        }
        return false;
    }

    @Override
    public void render(NvgRenderer dl, Object context, boolean hovered, float x, float y, float width) {
        BooleanSetting setting = this.setting;
        boolean on = setting.getAsBoolean();
        dl.color(this.getTextColor(hovered, on));
        String text = this.label + ": " + ChatColor.BOLD
                + (setting.getAsBoolean() ? setting.getOnLabel() : setting.getOffLabel());
        dl.text(Fonts.TAHOMA_10, text, x + 2.0F, y + 3.0F);
    }
}
