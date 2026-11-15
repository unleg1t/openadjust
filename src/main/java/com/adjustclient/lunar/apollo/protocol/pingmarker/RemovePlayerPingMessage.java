package com.adjustclient.lunar.apollo.protocol.pingmarker;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.lunar.apollo.protocol.common.*;
import com.adjustclient.util.proto.ProtoReader;

public final class RemovePlayerPingMessage implements ProtoMessage {
    public Object field8;
    public ApolloUuid playerUuid = null;

    public static RemovePlayerPingMessage parse(ProtoReader reader) {
        RemovePlayerPingMessage message = new RemovePlayerPingMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.playerUuid = ApolloUuid.parse(reader.readMessage()); break;
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
