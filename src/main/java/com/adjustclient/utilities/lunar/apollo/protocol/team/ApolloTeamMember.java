package com.adjustclient.utilities.lunar.apollo.protocol.team;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.*;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ApolloTeamMember implements ProtoMessage {
    public Object field24;
    public ApolloUuid playerUuid = null;
    public ApolloComponent displayName = null;
    public ApolloLocation location = null;
    public ApolloColor color = null;
    public String field20 = "";

    public static ApolloTeamMember parse(ProtoReader reader) {
        ApolloTeamMember message = new ApolloTeamMember();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.playerUuid = ApolloUuid.parse(reader.readMessage()); break;
                case 18: message.displayName = ApolloComponent.parse(reader.readMessage()); break;
                case 26: message.location = ApolloLocation.parse(reader.readMessage()); break;
                case 34: message.color = ApolloColor.parse(reader.readMessage()); break;
                case 42: message.field20 = reader.readString(); break;
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
