package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ApolloLocation implements ProtoMessage {
    private final String world;
    private final Object field8 = null;

    public ApolloLocation(String world) {
        this.world = world;
    }

    public String getWorld() {
        return world;
    }

    public static ApolloLocation parse(ProtoReader in) {
        String world = "";
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10: world = in.readString(); break;
                case 17:
                case 25:
                case 33:
                    in.readFixed64();
                    break;
                default: in.skipField(tag);
            }
        }
        return new ApolloLocation(world);
    }
}
