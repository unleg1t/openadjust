package com.adjustclient.utilities.lunar.apollo.protocol.packetenrichment;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class PlayerAttackMessage implements ProtoMessage {
    private final ApolloPacketInfo packetInfo;
    private final ApolloPlayerInfo attacker;
    private final ApolloPlayerInfo target;
    private final Object field16 = null;

    public PlayerAttackMessage(ApolloPacketInfo packetInfo, ApolloPlayerInfo attacker, ApolloPlayerInfo target) {
        this.packetInfo = packetInfo;
        this.attacker = attacker;
        this.target = target;
    }

    public ApolloPacketInfo getPacketInfo() {
        return packetInfo;
    }

    public ApolloPlayerInfo getAttacker() {
        return attacker;
    }

    public ApolloPlayerInfo getTarget() {
        return target;
    }

    public static PlayerAttackMessage parse(ProtoReader in) {
        ApolloPacketInfo packetInfo = null;
        ApolloPlayerInfo attacker = null;
        ApolloPlayerInfo target = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> packetInfo = ApolloPacketInfo.parse(in.readMessage());
                case 18 -> attacker = ApolloPlayerInfo.parse(in.readMessage());
                case 26 -> target = ApolloPlayerInfo.parse(in.readMessage());
                case 33 -> in.readFixed64();
                default -> in.skipField(tag);
            }
        }
        return new PlayerAttackMessage(packetInfo, attacker, target);
    }
}
