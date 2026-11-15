package com.adjustclient.script.api;

import com.adjustclient.script.api.MouseClick;
import com.adjustclient.script.api.SwingMouseClick;

public final class MouseClickHelper {
    public static native SwingMouseClick leftClick();

    public static native MouseClick middleClick();

    public static native SwingMouseClick rightClick();
}

