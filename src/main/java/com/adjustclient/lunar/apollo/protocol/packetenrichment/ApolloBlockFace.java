package com.adjustclient.lunar.apollo.protocol.packetenrichment;

public enum ApolloBlockFace {
    DIRECTION_UNSPECIFIED(0),
    DIRECTION_DOWN(0),
    DIRECTION_UP(0),
    DIRECTION_NORTH(0),
    DIRECTION_SOUTH(0),
    DIRECTION_WEST(0),
    DIRECTION_EAST(0),
    UNRECOGNIZED(0);

    public final int number;
    private final Object field16 = null;

    ApolloBlockFace(int number) {
        this.number = number;
    }
}
