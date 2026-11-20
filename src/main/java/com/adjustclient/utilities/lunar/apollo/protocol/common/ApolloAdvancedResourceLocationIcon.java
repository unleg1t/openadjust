package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ApolloAdvancedResourceLocationIcon implements ProtoMessage {
    private final String resourceLocation;
    private final Object field8 = null;

    public ApolloAdvancedResourceLocationIcon(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    public static ApolloAdvancedResourceLocationIcon parse(ProtoReader in) {
        String resourceLocation = "";
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10: resourceLocation = in.readString(); break;
                case 21:
                case 29:
                case 37:
                case 45:
                case 53:
                case 61:
                    in.readFixed32();
                    break;
                default: in.skipField(tag);
            }
        }
        return new ApolloAdvancedResourceLocationIcon(resourceLocation);
    }
}
