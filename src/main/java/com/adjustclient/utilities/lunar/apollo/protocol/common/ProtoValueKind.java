package com.adjustclient.utilities.lunar.apollo.protocol.common;

public enum ProtoValueKind {
    NULL_VALUE(0),
    NUMBER_VALUE(0),
    STRING_VALUE(0),
    BOOL_VALUE(0),
    STRUCT_VALUE(0),
    LIST_VALUE(0),
    NOT_SET(0);

    private final int number;
    private final Object field16 = null;

    ProtoValueKind(int number) {
        this.number = number;
    }

    public int getNumber() { return number; }
}
