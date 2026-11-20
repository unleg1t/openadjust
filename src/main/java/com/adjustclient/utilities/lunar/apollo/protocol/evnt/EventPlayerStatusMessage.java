package com.adjustclient.utilities.lunar.apollo.protocol.evnt;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloUuid;
import com.adjustclient.utilities.proto.ProtoReader;

public final class EventPlayerStatusMessage implements ProtoMessage {
    public ApolloUuid playerUuid;
    public Object field8;

    public static EventPlayerStatusMessage parse(ProtoReader reader) {
        ApolloUuid playerUuid = null;
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 10 -> playerUuid = ApolloUuid.parse(reader.readMessage());
                case 21, 29 -> reader.readFixed32();
                case 32 -> reader.readEnum();
                default -> reader.skipField(tag);
            }
        }
        EventPlayerStatusMessage message = new EventPlayerStatusMessage();
        message.playerUuid = playerUuid;
        return message;
    }
}
