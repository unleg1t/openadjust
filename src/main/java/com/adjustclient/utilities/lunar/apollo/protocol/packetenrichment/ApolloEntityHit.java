package com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloEntityId;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloLocation;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ApolloEntityHit implements ProtoMessage {
    private final ApolloLocation location;
    private final ApolloEntityId entityId;
    private final Object field12 = null;

    public ApolloEntityHit(ApolloLocation location, ApolloEntityId entityId) {
        this.location = location;
        this.entityId = entityId;
    }

    public ApolloLocation getLocation() {
        return location;
    }

    public ApolloEntityId getEntityId() {
        return entityId;
    }

    public static ApolloEntityHit parse(ProtoReader in) {
        ApolloLocation location = null;
        ApolloEntityId entityId = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> location = ApolloLocation.parse(in.readMessage());
                case 18 -> entityId = ApolloEntityId.parse(in.readMessage());
                default -> in.skipField(tag);
            }
        }
        return new ApolloEntityHit(location, entityId);
    }
}
