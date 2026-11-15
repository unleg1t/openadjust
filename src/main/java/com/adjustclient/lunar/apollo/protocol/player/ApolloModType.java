package com.adjustclient.lunar.apollo.protocol.player;

public enum ApolloModType {
    TYPE_UNSPECIFIED(0),
    TYPE_FABRIC_INTERNAL(1),
    TYPE_FABRIC_EXTERNAL(2),
    TYPE_FORGE_INTERNAL(3),
    TYPE_FORGE_EXTERNAL(4),
    UNRECOGNIZED(-1);

    public final int number;
    private final Object field20 = null;

    ApolloModType(int number) {
        this.number = number;
    }
}
