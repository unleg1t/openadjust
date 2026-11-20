package com.adjustclient.utilities.lunar.apollo.protocol.transfer;

public enum ApolloPingStatus {
    STATUS_UNSPECIFIED(0),
    STATUS_SUCCESS(1),
    STATUS_TIMED_OUT(2),
    UNRECOGNIZED(-1);

    public final int number;
    private final Object field20 = null;

    ApolloPingStatus(int number) {
        this.number = number;
    }
}
