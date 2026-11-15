package com.adjustclient.ui.tabgui;

public interface MouseInputHandler {

    /**
     * @param pressed true for a button press (a release is ignored by the implementors)
     * @param button  the mouse button code that is turned into the bind key
     * @return true when the input was consumed
     */
    boolean mouseButton(boolean pressed, int button);
}
