package com.adjustclient.lunar.apollo.protocol.packetenrichment;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class ApolloRayTraceResult implements ProtoMessage {
    private final ApolloBlockHit blockHit;
    private final ApolloEntityHit entityHit;
    private final ApolloMiss miss;
    private final Object field16 = null;

    public ApolloRayTraceResult(ApolloBlockHit blockHit, ApolloEntityHit entityHit, ApolloMiss miss) {
        this.blockHit = blockHit;
        this.entityHit = entityHit;
        this.miss = miss;
    }

    public ApolloBlockHit getBlockHit() {
        return blockHit;
    }

    public ApolloEntityHit getEntityHit() {
        return entityHit;
    }

    public ApolloMiss getMiss() {
        return miss;
    }

    public static ApolloRayTraceResult parse(ProtoReader in) {
        ApolloBlockHit blockHit = null;
        ApolloEntityHit entityHit = null;
        ApolloMiss miss = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> { blockHit = ApolloBlockHit.parse(in.readMessage()); entityHit = null; miss = null; }
                case 18 -> { entityHit = ApolloEntityHit.parse(in.readMessage()); blockHit = null; miss = null; }
                case 26 -> { miss = ApolloMiss.parse(in.readMessage()); blockHit = null; entityHit = null; }
                default -> in.skipField(tag);
            }
        }
        return new ApolloRayTraceResult(blockHit, entityHit, miss);
    }
}
