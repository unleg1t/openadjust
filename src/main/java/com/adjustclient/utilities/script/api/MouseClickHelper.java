package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.MouseClick;
import com.adjustclient.utilities.script.api.SwingMouseClick;

public final class MouseClickHelper {
    public static native SwingMouseClick leftClick();

    public static native MouseClick middleClick();

    public static native SwingMouseClick rightClick();
}

