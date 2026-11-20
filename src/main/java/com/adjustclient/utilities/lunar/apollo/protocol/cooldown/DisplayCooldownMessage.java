package com.adjustclient.utilities.lunar.apollo.protocol.cooldown;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.*;
import com.adjustclient.utilities.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class DisplayCooldownMessage implements ProtoMessage {
    public Object field16;
    public String name = "";
    public ProtoDuration duration = null;
    public ApolloIcon icon = null;

    public static DisplayCooldownMessage parse(ProtoReader reader) {
        DisplayCooldownMessage message = new DisplayCooldownMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.name = reader.readString(); break;
                case 18: message.duration = ProtoDuration.parse(reader.readMessage()); break;
                case 26: message.icon = ApolloIcon.parse(reader.readMessage()); break;
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
