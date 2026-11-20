package com.adjustclient.utilities.lunar.apollo.protocol.transfer;

public enum ApolloTransferStatus {
    STATUS_UNSPECIFIED(0),
    STATUS_ACCEPTED(1),
    STATUS_REJECTED(2),
    UNRECOGNIZED(-1);

    public final int number;
    private final Object field20 = null;

    ApolloTransferStatus(int number) {
        this.number = number;
    }
}
