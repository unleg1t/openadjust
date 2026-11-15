package com.adjustclient.lunar.apollo.protocol.packetenrichment;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.lunar.apollo.protocol.common.ApolloPlayerLocation;
import com.adjustclient.lunar.apollo.protocol.common.ApolloUuid;
import com.adjustclient.util.proto.ProtoReader;

public final class ApolloPlayerInfo implements ProtoMessage {
    private final ApolloUuid uuid;
    private final ApolloPlayerLocation location;
    private final Object field12 = null;

    public ApolloPlayerInfo(ApolloUuid uuid, ApolloPlayerLocation location) {
        this.uuid = uuid;
        this.location = location;
    }

    public ApolloUuid getUuid() {
        return uuid;
    }

    public ApolloPlayerLocation getLocation() {
        return location;
    }

    public static ApolloPlayerInfo parse(ProtoReader in) {
        ApolloUuid uuid = null;
        ApolloPlayerLocation location = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> uuid = ApolloUuid.parse(in.readMessage());
                case 18 -> location = ApolloPlayerLocation.parse(in.readMessage());
                case 24 -> in.readEnum();
                case 32 -> in.readEnum();
                case 40 -> in.readEnum();
                case 53 -> in.readFixed32();
                case 61 -> in.readFixed32();
                default -> in.skipField(tag);
            }
        }
        return new ApolloPlayerInfo(uuid, location);
    }
}
