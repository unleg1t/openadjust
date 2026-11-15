package com.adjustclient.ui.tabgui.component;

import com.adjustclient.module.Module;
import com.adjustclient.render.NvgRenderer;
import org.lwjgl.input.Keyboard;
import com.adjustclient.ui.tabgui.KeyPressHandler;
import com.adjustclient.ui.tabgui.LabelNode;
import com.adjustclient.render.font.Fonts;

public final class ModuleComponent extends LabelNode implements KeyPressHandler {

    public final Module module;

    public ModuleComponent(Module module) {
        super(module.getName());
        this.module = module;
    }

    @Override
    public boolean keyPressed(int key) {
        if (key != Keyboard.KEY_RETURN) {
            return false;
        }
        this.module.setEnabled(!this.module.isEnabled(), true);
        return true;
    }

    @Override
    public void render(NvgRenderer dl, Object context, boolean hovered, float x, float y, float width) {
        dl.color(this.getTextColor(hovered, this.module.isEnabled()));
        dl.text(Fonts.TAHOMA_10, this.label, x + 2.0F, y + 3.0F);
    }
}
