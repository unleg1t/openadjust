package com.adjustclient.lunar.apollo.protocol.coloredfire;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.lunar.apollo.protocol.common.*;
import com.adjustclient.util.proto.ProtoReader;

public final class ResetColoredFireMessage implements ProtoMessage {
    public Object field8;
    public ApolloUuid playerUuid = null;

    public static ResetColoredFireMessage parse(ProtoReader reader) {
        ResetColoredFireMessage message = new ResetColoredFireMessage();
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
