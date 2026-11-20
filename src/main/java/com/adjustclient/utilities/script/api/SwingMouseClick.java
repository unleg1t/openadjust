package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.MouseClick;

public final class SwingMouseClick
extends MouseClick {
    public native boolean clientsideSwing();

    public native void setClientsideSwing(boolean var1);
}

