package com.adjustclient.utilities.lunar.apollo.protocol.cooldown;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ResetCooldownsMessage implements ProtoMessage {
    public Object field4;

    public static ResetCooldownsMessage parse(ProtoReader reader) {
        ResetCooldownsMessage message = new ResetCooldownsMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
