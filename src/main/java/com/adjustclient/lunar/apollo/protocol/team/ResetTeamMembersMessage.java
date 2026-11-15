package com.adjustclient.lunar.apollo.protocol.team;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class ResetTeamMembersMessage implements ProtoMessage {
    public Object field4;

    public static ResetTeamMembersMessage parse(ProtoReader reader) {
        ResetTeamMembersMessage message = new ResetTeamMembersMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
