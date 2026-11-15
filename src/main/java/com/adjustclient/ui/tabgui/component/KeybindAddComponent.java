package com.adjustclient.ui.tabgui.component;

import com.adjustclient.bind.Bind;
import java.util.Collections;
import org.lwjgl.input.Keyboard;
import com.adjustclient.bind.KeyboardKey;
import com.adjustclient.bind.MouseKey;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.ui.tabgui.KeyPressHandler;
import com.adjustclient.ui.tabgui.LabelNode;
import com.adjustclient.ui.tabgui.MouseInputHandler;
import com.adjustclient.ui.tabgui.ScrollInputHandler;

public final class KeybindAddComponent extends LabelNode
        implements KeyPressHandler, MouseInputHandler, ScrollInputHandler {

    private final Bind bind;
    public boolean listening;

    public KeybindAddComponent(Bind bind) {
        super("Add keybind...");
        this.bind = bind;
    }

    @Override
    public boolean keyPressed(int key) {
        if (key != Keyboard.KEY_RETURN) {
            if (!this.listening) {
                return false;
            }
            if (this.bind.mutable) {
                KeyboardKey keyboardKey = new KeyboardKey(key);
                this.bind.keys.add(keyboardKey);
                this.bind.onKeysChanged(Collections.emptyList(), Collections.singletonList(keyboardKey));
            }
            ServiceUtil.markConfigurationDirty();
            this.listening = false;
            this.label = "Add keybind...";
            return true;
        }
        boolean wasListening = this.listening;
        this.listening = !wasListening;
        this.label = wasListening ? "Add keybind..." : "Press any key...";
        return true;
    }

    @Override
    public boolean mouseButton(boolean pressed, int button) {
        if (!pressed || !this.listening) {
            return false;
        }
        if (this.bind.mutable) {
            MouseKey mouseKey = new MouseKey(button);
            this.bind.keys.add(mouseKey);
            this.bind.onKeysChanged(Collections.emptyList(), Collections.singletonList(mouseKey));
        }
        ServiceUtil.markConfigurationDirty();
        this.listening = false;
        this.label = "Add keybind...";
        return true;
    }
}
