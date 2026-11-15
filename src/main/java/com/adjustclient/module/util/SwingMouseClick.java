package com.adjustclient.module.util;

import net.minecraft.client.settings.KeyBinding;

public class SwingMouseClick extends MouseClick {
    public boolean swing;
    public SwingMouseClick(KeyBinding key) { super(key); }

    @Override public void endTick() {
        super.endTick();
        if (key.isKeyDown() || holdTicks == 0) swing = true;
    }
}
