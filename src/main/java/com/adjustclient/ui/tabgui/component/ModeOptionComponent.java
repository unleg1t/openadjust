package com.adjustclient.ui.tabgui.component;

import com.adjustclient.render.NvgRenderer;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.setting.ModeSetting;
import org.lwjgl.input.Keyboard;
import com.adjustclient.ui.tabgui.KeyPressHandler;
import com.adjustclient.ui.tabgui.LabelNode;
import com.adjustclient.render.font.Fonts;

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
