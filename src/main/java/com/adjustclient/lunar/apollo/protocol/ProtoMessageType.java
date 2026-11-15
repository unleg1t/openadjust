package com.adjustclient.lunar.apollo.protocol;

import java.util.Objects;

public record ProtoMessageType<T extends ProtoMessage>(String typeName, ProtoParser<T> parser) {

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof ProtoMessageType<?> value)) return false;
        return Objects.equals(parser, value.parser) && Objects.equals(typeName, value.typeName);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(typeName) + Objects.hashCode(parser);
    }

    @Override
    public String toString() {
        return "c[typeName=" + typeName + ", parser=" + parser + "]";
    }
}
