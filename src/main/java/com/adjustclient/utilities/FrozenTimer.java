package com.adjustclient.utilities;

public class FrozenTimer extends Timer {
    @Override public void reset() { throw new IllegalStateException(); }
}
