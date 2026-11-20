package com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class PlayerUseItemBucketMessage implements ProtoMessage {
    private final ApolloPacketInfo packetInfo;
    private final ApolloPlayerInfo playerInfo;
    private final ApolloRayTraceResult rayTraceResult;
    private final Object field16 = null;

    public PlayerUseItemBucketMessage(ApolloPacketInfo packetInfo, ApolloPlayerInfo playerInfo, ApolloRayTraceResult rayTraceResult) {
        this.packetInfo = packetInfo;
        this.playerInfo = playerInfo;
        this.rayTraceResult = rayTraceResult;
    }

    public ApolloPacketInfo getPacketInfo() {
        return packetInfo;
    }

    public ApolloPlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public ApolloRayTraceResult getRayTraceResult() {
        return rayTraceResult;
    }

    public static PlayerUseItemBucketMessage parse(ProtoReader in) {
        ApolloPacketInfo packetInfo = null;
        ApolloPlayerInfo playerInfo = null;
        ApolloRayTraceResult rayTraceResult = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> packetInfo = ApolloPacketInfo.parse(in.readMessage());
                case 18 -> playerInfo = ApolloPlayerInfo.parse(in.readMessage());
                case 26 -> rayTraceResult = ApolloRayTraceResult.parse(in.readMessage());
                default -> in.skipField(tag);
            }
        }
        return new PlayerUseItemBucketMessage(packetInfo, playerInfo, rayTraceResult);
    }
}
