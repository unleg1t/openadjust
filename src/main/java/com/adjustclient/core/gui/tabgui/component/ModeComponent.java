package com.adjustclient.core.gui.tabgui.component;

import com.adjustclient.core.rendering.NvgRenderer;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.core.settings.ModeSetting;
import java.util.List;
import net.minecraft.util.ChatColor;
import org.lwjgl.input.Keyboard;
import com.adjustclient.core.gui.tabgui.KeyPressHandler;
import com.adjustclient.core.gui.tabgui.SettingNode;
import com.adjustclient.core.rendering.font.Fonts;

public final class ModeComponent extends SettingNode<ModeSetting<?>> implements KeyPressHandler {

    public ModeComponent(String label, ModeSetting<?> setting) {
        super(label, setting);
    }

    @Override
    public boolean keyPressed(int key) {
        if (key != Keyboard.KEY_RETURN) {
            return false;
        }
        ModeSetting<?> setting = this.setting;
        List<? extends ModeOption> options = setting.getOptions();
        int index = options.indexOf(setting.getValue());
        if (index == options.size() - 1) {
            setting.setMode(options.getFirst());
        } else {
            setting.setMode(options.get(index + 1));
        }
        return true;
    }

    @Override
    public void render(NvgRenderer dl, Object context, boolean hovered, float x, float y, float width) {
        dl.color(this.getTextColor(hovered, true));
        String text = this.label + ": " + ChatColor.BOLD + this.setting.getValue().getName();
        dl.text(Fonts.TAHOMA_10, text, x + 2.0F, y + 3.0F);
    }
}
