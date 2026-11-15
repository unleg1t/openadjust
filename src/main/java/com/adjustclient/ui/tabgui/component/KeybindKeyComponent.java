package com.adjustclient.ui.tabgui.component;

import com.adjustclient.bind.Bind;
import java.util.Collections;
import org.lwjgl.input.Keyboard;
import com.adjustclient.Adjust;
import com.adjustclient.bind.BindKey;
import com.adjustclient.bind.KeyboardKey;
import com.adjustclient.bind.MouseKey;
import com.adjustclient.bind.ScrollKey;
import com.adjustclient.ui.tabgui.KeyPressHandler;
import com.adjustclient.ui.tabgui.LabelNode;
import com.adjustclient.ui.tabgui.MouseInputHandler;
import com.adjustclient.ui.tabgui.ScrollInputHandler;

public final class KeybindKeyComponent extends LabelNode
        implements KeyPressHandler, MouseInputHandler, ScrollInputHandler {

    private final Bind bind;
    public final BindKey key;
    public boolean listening;

    public KeybindKeyComponent(String label, Bind bind, BindKey key) {
        super(label);
        this.bind = bind;
        this.key = key;
    }

    private void updateLabel() {
        if (this.listening) {
            this.label = "Press any key...";
        } else if (this.key instanceof ScrollKey scroll) {
            this.label = scroll.keyCode() == 1069 ? "Scroll Up" : "Scroll Down";
        } else {
            this.label = this.key.getName();
        }
    }

    @Override
    public boolean keyPressed(int key) {
        if (key == Keyboard.KEY_RETURN) {
            this.listening = true;
            this.label = "Press any key...";
            return true;
        }
        if (!this.listening) {
            return false;
        }
        this.bind.removeKey(this.key);
        if (key != Keyboard.KEY_DELETE && key != Keyboard.KEY_BACK) {
            if (this.bind.mutable) {
                KeyboardKey newKey = new KeyboardKey(key); // keyboard key
                this.bind.keys.add(newKey);
                this.bind.onKeysChanged(Collections.emptyList(), Collections.singletonList(newKey));
            }
        }
        Adjust.INSTANCE.fileSystem.markDirty();
        this.updateLabel();
        return true;
    }

    @Override
    public boolean mouseButton(boolean pressed, int button) {
        if (!pressed || !this.listening) {
            return false;
        }
        if (this.bind.mutable) {
            this.bind.keys.remove(this.key);
            this.bind.onKeysChanged(Collections.singletonList(this.key), Collections.emptyList());
        }
        if (this.bind.mutable) {
            MouseKey newKey = new MouseKey(button); // mouse key
            this.bind.keys.add(newKey);
            this.bind.onKeysChanged(Collections.emptyList(), Collections.singletonList(newKey));
        }
        Adjust.INSTANCE.fileSystem.markDirty();
        this.updateLabel();
        return true;
    }
}
