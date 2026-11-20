package com.adjustclient.utilities.lunar.apollo.protocol;

@FunctionalInterface
public interface ProtoParser<T extends ProtoMessage> {
    T parse(byte[] bytes);
}
