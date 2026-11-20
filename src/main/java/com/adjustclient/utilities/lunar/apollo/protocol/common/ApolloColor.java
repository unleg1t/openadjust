package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ApolloColor implements ProtoMessage {
    private final Object field4 = null;

    public ApolloColor() {}

    public static ApolloColor parse(ProtoReader in) {
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 8:
                    in.readEnum();
                    break;
                default: in.skipField(tag);
            }
        }
        return new ApolloColor();
    }
}
