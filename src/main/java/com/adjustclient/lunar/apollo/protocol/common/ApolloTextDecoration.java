package com.adjustclient.lunar.apollo.protocol.common;

public enum ApolloTextDecoration {
    TEXT_DECORATOR_UNSPECIFIED(0),
    TEXT_DECORATOR_OBFUSCATED(0),
    TEXT_DECORATOR_BOLD(0),
    TEXT_DECORATOR_STRIKETHROUGH(0),
    TEXT_DECORATOR_UNDERLINED(0),
    TEXT_DECORATOR_ITALIC(0),
    UNRECOGNIZED(0);

    private final int number;
    private final Object field16 = null;

    ApolloTextDecoration(int number) {
        this.number = number;
    }

    public int getNumber() { return number; }
}
