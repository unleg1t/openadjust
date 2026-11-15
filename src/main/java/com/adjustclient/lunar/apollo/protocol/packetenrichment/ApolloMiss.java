package com.adjustclient.lunar.apollo.protocol.packetenrichment;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class ApolloMiss implements ProtoMessage {
    private final Object field4 = null;

    public ApolloMiss() {
    }

    public static ApolloMiss parse(ProtoReader in) {
        for (int tag; (tag = in.readTag()) != 0;) {
            in.skipField(tag);
        }
        return new ApolloMiss();
    }
}
