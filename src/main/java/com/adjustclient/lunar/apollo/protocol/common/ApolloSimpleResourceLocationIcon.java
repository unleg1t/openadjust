package com.adjustclient.lunar.apollo.protocol.common;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class ApolloSimpleResourceLocationIcon implements ProtoMessage {
    private final String resourceLocation;
    private final Object field8 = null;

    public ApolloSimpleResourceLocationIcon(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    public static ApolloSimpleResourceLocationIcon parse(ProtoReader in) {
        String resourceLocation = "";
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10: resourceLocation = in.readString(); break;
                case 16:
                    in.readEnum();
                    break;
                default: in.skipField(tag);
            }
        }
        return new ApolloSimpleResourceLocationIcon(resourceLocation);
    }
}
