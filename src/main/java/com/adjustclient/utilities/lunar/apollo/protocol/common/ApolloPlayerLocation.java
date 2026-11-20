package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ApolloPlayerLocation implements ProtoMessage {
    private final ApolloLocation location;
    private final Object field8 = null;

    public ApolloPlayerLocation(ApolloLocation location) {
        this.location = location;
    }

    public ApolloLocation getLocation() {
        return location;
    }

    public static ApolloPlayerLocation parse(ProtoReader in) {
        ApolloLocation location = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10: location = ApolloLocation.parse(in.readMessage()); break;
                case 21:
                case 29: in.readFixed32(); break;
                default: in.skipField(tag);
            }
        }
        return new ApolloPlayerLocation(location);
    }
}
