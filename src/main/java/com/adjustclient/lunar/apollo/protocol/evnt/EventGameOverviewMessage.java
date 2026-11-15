package com.adjustclient.lunar.apollo.protocol.evnt;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class EventGameOverviewMessage implements ProtoMessage {
    public EventTeamStatusMessage teamStatus1;
    public EventTeamStatusMessage teamStatus2;
    public Object field12;

    public static EventGameOverviewMessage parse(ProtoReader reader) {
        EventTeamStatusMessage teamStatus1 = null;
        EventTeamStatusMessage teamStatus2 = null;
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 10 -> teamStatus1 = EventTeamStatusMessage.parse(reader.readMessage());
                case 18 -> teamStatus2 = EventTeamStatusMessage.parse(reader.readMessage());
                case 29 -> reader.readFixed32();
                case 32, 40, 48, 56 -> reader.readEnum();
                default -> reader.skipField(tag);
            }
        }
        EventGameOverviewMessage message = new EventGameOverviewMessage();
        message.teamStatus1 = teamStatus1;
        message.teamStatus2 = teamStatus2;
        return message;
    }
}
