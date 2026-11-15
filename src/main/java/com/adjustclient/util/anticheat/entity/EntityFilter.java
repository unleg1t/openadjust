package com.adjustclient.util.anticheat.entity;

public final class EntityFilter {
    public static int of(boolean removed, boolean players, boolean hostile, boolean passive,
                         boolean items, boolean teammates, boolean invisibles) {
        return (removed ? 1 : 0) | (players ? 2 : 0) | (hostile ? 4 : 0)
                | (passive ? 8 : 0) | (items ? 16 : 0) | (teammates ? 32 : 0)
                | (invisibles ? 64 : 0);
    }
}
