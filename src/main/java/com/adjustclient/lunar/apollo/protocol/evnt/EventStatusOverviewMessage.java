package com.adjustclient.lunar.apollo.protocol.evnt;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class EventStatusOverviewMessage implements ProtoMessage {
    public List<EventPlayerStatusMessage> playerStatuses;
    public List<EventPlayerStatusMessage> otherPlayerStatuses;
    public Object field12;

    public static EventStatusOverviewMessage parse(ProtoReader reader) {
        List<EventPlayerStatusMessage> playerStatuses = new ArrayList<>();
        List<EventPlayerStatusMessage> otherPlayerStatuses = new ArrayList<>();
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 10 -> playerStatuses.add(EventPlayerStatusMessage.parse(reader.readMessage()));
                case 18 -> otherPlayerStatuses.add(EventPlayerStatusMessage.parse(reader.readMessage()));
                default -> reader.skipField(tag);
            }
        }
        EventStatusOverviewMessage message = new EventStatusOverviewMessage();
        message.playerStatuses = playerStatuses;
        message.otherPlayerStatuses = otherPlayerStatuses;
        return message;
    }
}
