package com.adjustclient.lunar.apollo.protocol.packetenrichment;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class PlayerUseItemMessage implements ProtoMessage {
    private final ApolloPacketInfo packetInfo;
    private final ApolloPlayerInfo playerInfo;
    private final Object field12 = null;

    public PlayerUseItemMessage(ApolloPacketInfo packetInfo, ApolloPlayerInfo playerInfo) {
        this.packetInfo = packetInfo;
        this.playerInfo = playerInfo;
    }

    public ApolloPacketInfo getPacketInfo() {
        return packetInfo;
    }

    public ApolloPlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public static PlayerUseItemMessage parse(ProtoReader in) {
        ApolloPacketInfo packetInfo = null;
        ApolloPlayerInfo playerInfo = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> packetInfo = ApolloPacketInfo.parse(in.readMessage());
                case 18 -> playerInfo = ApolloPlayerInfo.parse(in.readMessage());
                case 24 -> in.readEnum();
                default -> in.skipField(tag);
            }
        }
        return new PlayerUseItemMessage(packetInfo, playerInfo);
    }
}
