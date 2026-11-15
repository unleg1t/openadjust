package com.adjustclient.module.impl.render.tweaks;

import com.adjustclient.util.Timer;

public final class ChatStackEntry {
    public final Timer timer;
    public int count;
    public Object field12;
    public ChatStackEntry() { timer = new Timer(); count = 1; }
}
