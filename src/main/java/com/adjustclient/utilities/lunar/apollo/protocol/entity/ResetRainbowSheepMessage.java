package com.adjustclient.utilities.lunar.apollo.protocol.entity;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloEntityId;
import com.adjustclient.utilities.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class ResetRainbowSheepMessage implements ProtoMessage {
    public List<ApolloEntityId> entityIds;
    public Object field8;

    public static ResetRainbowSheepMessage parse(ProtoReader reader) {
        List<ApolloEntityId> entityIds = new ArrayList<>();
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 10 -> entityIds.add(ApolloEntityId.parse(reader.readMessage()));
                default -> reader.skipField(tag);
            }
        }
        ResetRainbowSheepMessage message = new ResetRainbowSheepMessage();
        message.entityIds = entityIds;
        return message;
    }
}
