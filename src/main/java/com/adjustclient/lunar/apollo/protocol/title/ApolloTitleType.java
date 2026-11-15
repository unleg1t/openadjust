package com.adjustclient.lunar.apollo.protocol.title;

public enum ApolloTitleType {
    TITLE_TYPE_UNSPECIFIED, // wire 0, also the absent-field default
    TITLE_TYPE_TITLE, // wire 1
    TITLE_TYPE_SUBTITLE, // wire 2
    UNRECOGNIZED; // every other wire number

    public int field12;
    public Object field16;
}
