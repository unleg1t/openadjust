package com.adjustclient.core.gui.tabgui.component;

import com.adjustclient.core.rendering.NvgRenderer;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.core.settings.ModeSetting;
import org.lwjgl.input.Keyboard;
import com.adjustclient.core.gui.tabgui.KeyPressHandler;
import com.adjustclient.core.gui.tabgui.LabelNode;
import com.adjustclient.core.rendering.font.Fonts;

public class ModeOptionComponent extends LabelNode implements KeyPressHandler {

    final ModeSetting<?> setting;
    final ModeOption option;

    public ModeOptionComponent(String label, ModeSetting<?> setting, ModeOption option) {
        super(label);
        this.setting = setting;
        this.option = option;
    }

    @Override
    public boolean keyPressed(int key) {
        if (key == Keyboard.KEY_RIGHT || key == Keyboard.KEY_RETURN) {
            this.setting.setValue(this.option);
            return true;
        }
        return false;
    }

    @Override
    public void render(NvgRenderer dl, Object context, boolean hovered, float x, float y, float width) {
        boolean selected = this.setting.getValue().equals(this.option);
        dl.color(this.getTextColor(hovered, selected));
        dl.text(Fonts.TAHOMA_10, this.label, x + 2.0F, y + 3.0F);
    }
}
