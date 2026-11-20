package com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class PlayerChatOpenMessage implements ProtoMessage {
    private final ApolloPlayerInfo playerInfo;
    private final ApolloPacketInfo packetInfo;
    private final Object field12 = null;

    public PlayerChatOpenMessage(ApolloPlayerInfo playerInfo, ApolloPacketInfo packetInfo) {
        this.playerInfo = playerInfo;
        this.packetInfo = packetInfo;
    }

    public ApolloPlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public ApolloPacketInfo getPacketInfo() {
        return packetInfo;
    }

    public static PlayerChatOpenMessage parse(ProtoReader in) {
        ApolloPlayerInfo playerInfo = null;
        ApolloPacketInfo packetInfo = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> playerInfo = ApolloPlayerInfo.parse(in.readMessage());
                case 18 -> packetInfo = ApolloPacketInfo.parse(in.readMessage());
                default -> in.skipField(tag);
            }
        }
        return new PlayerChatOpenMessage(playerInfo, packetInfo);
    }
}
