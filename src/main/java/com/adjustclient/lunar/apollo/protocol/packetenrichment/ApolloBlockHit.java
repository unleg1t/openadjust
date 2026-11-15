package com.adjustclient.lunar.apollo.protocol.packetenrichment;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.lunar.apollo.protocol.common.ApolloBlockLocation;
import com.adjustclient.lunar.apollo.protocol.common.ApolloLocation;
import com.adjustclient.util.proto.ProtoReader;

public final class ApolloBlockHit implements ProtoMessage {
    private final ApolloLocation location;
    private final ApolloBlockLocation blockLocation;
    private final ApolloBlockFace face;
    private final Object field16 = null;

    public ApolloBlockHit(ApolloLocation location, ApolloBlockLocation blockLocation, ApolloBlockFace face) {
        this.location = location;
        this.blockLocation = blockLocation;
        this.face = face;
    }

    public ApolloLocation getLocation() {
        return location;
    }

    public ApolloBlockLocation getBlockLocation() {
        return blockLocation;
    }

    public ApolloBlockFace getFace() {
        return face;
    }

    public static ApolloBlockHit parse(ProtoReader in) {
        ApolloLocation location = null;
        ApolloBlockLocation blockLocation = null;
        ApolloBlockFace face = ApolloBlockFace.DIRECTION_UNSPECIFIED;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> location = ApolloLocation.parse(in.readMessage());
                case 18 -> blockLocation = ApolloBlockLocation.parse(in.readMessage());
                case 24 -> { face = switch ((int) in.readEnum()) {
                    case 0 -> ApolloBlockFace.DIRECTION_UNSPECIFIED;
                    case 1 -> ApolloBlockFace.DIRECTION_DOWN;
                    case 2 -> ApolloBlockFace.DIRECTION_UP;
                    case 3 -> ApolloBlockFace.DIRECTION_NORTH;
                    case 4 -> ApolloBlockFace.DIRECTION_SOUTH;
                    case 5 -> ApolloBlockFace.DIRECTION_WEST;
                    case 6 -> ApolloBlockFace.DIRECTION_EAST;
                    default -> ApolloBlockFace.UNRECOGNIZED;
                }; }
                default -> in.skipField(tag);
            }
        }
        return new ApolloBlockHit(location, blockLocation, face);
    }
}
