package com.adjustclient.modules.impl.render.tweaks;

import com.adjustclient.utilities.Timer;

public final class ChatStackEntry {
    public final Timer timer;
    public int count;
    public Object field12;
    public ChatStackEntry() { timer = new Timer(); count = 1; }
}
