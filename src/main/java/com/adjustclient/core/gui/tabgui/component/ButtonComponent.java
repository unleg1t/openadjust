package com.adjustclient.core.gui.tabgui.component;

import com.adjustclient.core.settings.ButtonSetting;
import org.lwjgl.input.Keyboard;
import com.adjustclient.core.gui.tabgui.KeyPressHandler;
import com.adjustclient.core.gui.tabgui.LabelNode;

public final class ButtonComponent extends LabelNode implements KeyPressHandler {

    private final String name;
    private final Runnable action;
    private Object obf28;

    public ButtonComponent(String name, Runnable action) {
        super(name.concat("..."));
        this.name = name;
        this.action = action;
    }

    public ButtonComponent(ButtonSetting setting) {
        this(setting.getLabel(), setting.getValue());
    }

    @Override
    public boolean keyPressed(int key) {
        if (key == Keyboard.KEY_RETURN) {
            if (this.action != null) {
                this.action.run();
            }
            return true;
        }
        if (key != Keyboard.KEY_RIGHT) {
            return false;
        }
        if (this.action != null) {
            this.action.run();
        }
        return true;
    }
}
