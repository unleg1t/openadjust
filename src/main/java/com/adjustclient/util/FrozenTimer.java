package com.adjustclient.util;

public class FrozenTimer extends Timer {
    @Override public void reset() { throw new IllegalStateException(); }
}
