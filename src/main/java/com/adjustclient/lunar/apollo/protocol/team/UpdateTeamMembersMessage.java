package com.adjustclient.lunar.apollo.protocol.team;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class UpdateTeamMembersMessage implements ProtoMessage {
    public List<ApolloTeamMember> members = new ArrayList<>();
    public Object field8;

    public static UpdateTeamMembersMessage parse(ProtoReader reader) {
        UpdateTeamMembersMessage message = new UpdateTeamMembersMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            if (tag == 10) {
                message.members.add(ApolloTeamMember.parse(reader.readMessage()));
            } else {
                reader.skipField(tag);
            }
        }
        return message;
    }
}
