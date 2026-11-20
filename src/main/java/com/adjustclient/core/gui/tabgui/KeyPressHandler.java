package com.adjustclient.core.gui.tabgui;

public interface KeyPressHandler {

    /**
     * @param key the pressed key code ({@code org.lwjgl.input.Keyboard.KEY_*} of the client's shim)
     * @return true when the key was consumed (the Tab GUI then does not navigate)
     */
    boolean keyPressed(int key);
}
