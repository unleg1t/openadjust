package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ApolloBlockLocation implements ProtoMessage {
    private final String world;
    private final Object field8 = null;

    public ApolloBlockLocation(String world) {
        this.world = world;
    }

    public String getWorld() {
        return world;
    }

    public static ApolloBlockLocation parse(ProtoReader in) {
        String world = "";
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10: world = in.readString(); break;
                case 16:
                case 24:
                case 32:
                    in.readEnum();
                    break;
                default: in.skipField(tag);
            }
        }
        return new ApolloBlockLocation(world);
    }
}
