package com.adjustclient.utilities.lunar.apollo.protocol.evnt;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class EventOverviewMessage implements ProtoMessage {
    public List<EventPlayerMessage> players;
    public Object field8;

    public static EventOverviewMessage parse(ProtoReader reader) {
        List<EventPlayerMessage> players = new ArrayList<>();
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 10 -> players.add(EventPlayerMessage.parse(reader.readMessage()));
                default -> reader.skipField(tag);
            }
        }
        EventOverviewMessage message = new EventOverviewMessage();
        message.players = players;
        return message;
    }
}
