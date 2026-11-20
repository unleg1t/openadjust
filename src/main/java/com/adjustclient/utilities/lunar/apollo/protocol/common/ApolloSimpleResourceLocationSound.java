package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ApolloSimpleResourceLocationSound implements ProtoMessage {
    private final String resourceLocation;
    private final Object field8 = null;

    public ApolloSimpleResourceLocationSound(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    public static ApolloSimpleResourceLocationSound parse(ProtoReader in) {
        String resourceLocation = "";
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10: resourceLocation = in.readString(); break;
                default: in.skipField(tag);
            }
        }
        return new ApolloSimpleResourceLocationSound(resourceLocation);
    }
}
