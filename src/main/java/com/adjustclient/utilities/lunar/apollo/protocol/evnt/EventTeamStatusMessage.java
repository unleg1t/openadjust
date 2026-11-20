package com.adjustclient.utilities.lunar.apollo.protocol.evnt;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class EventTeamStatusMessage implements ProtoMessage {
    public Object field4;

    public static EventTeamStatusMessage parse(ProtoReader reader) {
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 13, 21, 29, 37, 45, 53, 61 -> reader.readFixed32();
                default -> reader.skipField(tag);
            }
        }
        EventTeamStatusMessage message = new EventTeamStatusMessage();
        return message;
    }
}
