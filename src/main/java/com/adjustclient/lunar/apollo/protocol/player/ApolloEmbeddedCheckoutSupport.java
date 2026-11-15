package com.adjustclient.lunar.apollo.protocol.player;

public enum ApolloEmbeddedCheckoutSupport {
    EMBEDDED_CHECKOUT_SUPPORT_UNSPECIFIED(0),
    EMBEDDED_CHECKOUT_SUPPORT_OVERLAY(1),
    EMBEDDED_CHECKOUT_SUPPORT_WINDOW(2),
    UNRECOGNIZED(-1);

    public final int number;
    private final Object field20 = null;

    ApolloEmbeddedCheckoutSupport(int number) {
        this.number = number;
    }
}
