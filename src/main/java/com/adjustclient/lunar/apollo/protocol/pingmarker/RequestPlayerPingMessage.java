package com.adjustclient.lunar.apollo.protocol.pingmarker;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.lunar.apollo.protocol.common.ApolloUuid;
import com.adjustclient.lunar.apollo.protocol.common.ApolloLocation;
import com.adjustclient.util.proto.ProtoReader;

public final class RequestPlayerPingMessage implements ProtoMessage {
    public ApolloUuid playerUuid;
    public String markerType = "";
    public ApolloLocation location;
    public ApolloLocation field16;
    public Object field20;

    public static RequestPlayerPingMessage parse(ProtoReader reader) {
        RequestPlayerPingMessage message = new RequestPlayerPingMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.playerUuid = ApolloUuid.parse(reader.readMessage()); break;
                case 18: message.markerType = reader.readString(); break;
                case 26: message.location = ApolloLocation.parse(reader.readMessage()); break;
                case 34: message.field16 = ApolloLocation.parse(reader.readMessage()); break;
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
