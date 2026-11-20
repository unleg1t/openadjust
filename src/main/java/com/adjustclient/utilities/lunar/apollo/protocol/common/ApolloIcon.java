package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ApolloIcon implements ProtoMessage {
    private final ApolloItemStackIcon itemStack;
    private final ApolloSimpleResourceLocationIcon simpleResourceLocation;
    private final ApolloAdvancedResourceLocationIcon advancedResourceLocation;
    private final Object field16 = null;

    public ApolloIcon(ApolloItemStackIcon itemStack,
                      ApolloSimpleResourceLocationIcon simpleResourceLocation,
                      ApolloAdvancedResourceLocationIcon advancedResourceLocation) {
        this.itemStack = itemStack;
        this.simpleResourceLocation = simpleResourceLocation;
        this.advancedResourceLocation = advancedResourceLocation;
    }

    public ApolloItemStackIcon getItemStack() { return itemStack; }
    public ApolloSimpleResourceLocationIcon getSimpleResourceLocation() { return simpleResourceLocation; }
    public ApolloAdvancedResourceLocationIcon getAdvancedResourceLocation() { return advancedResourceLocation; }

    public static ApolloIcon parse(ProtoReader in) {
        ApolloItemStackIcon item = null;
        ApolloSimpleResourceLocationIcon simple = null;
        ApolloAdvancedResourceLocationIcon advanced = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10:
                    item = ApolloItemStackIcon.parse(in.readMessage());
                    simple = null;
                    advanced = null;
                    break;
                case 18:
                    simple = ApolloSimpleResourceLocationIcon.parse(in.readMessage());
                    item = null;
                    advanced = null;
                    break;
                case 26:
                    advanced = ApolloAdvancedResourceLocationIcon.parse(in.readMessage());
                    item = null;
                    simple = null;
                    break;
                default: in.skipField(tag);
            }
        }
        return new ApolloIcon(item, simple, advanced);
    }
}
