package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ApolloEntityId implements ProtoMessage {
    private final ApolloUuid uuid;
    private final Object field8 = null;

    public ApolloEntityId(ApolloUuid uuid) {
        this.uuid = uuid;
    }

    public ApolloUuid getUuid() {
        return uuid;
    }

    public static ApolloEntityId parse(ProtoReader in) {
        ApolloUuid uuid = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 8: in.readEnum(); break;
                case 18: uuid = ApolloUuid.parse(in.readMessage()); break;
                default: in.skipField(tag);
            }
        }
        return new ApolloEntityId(uuid);
    }
}
