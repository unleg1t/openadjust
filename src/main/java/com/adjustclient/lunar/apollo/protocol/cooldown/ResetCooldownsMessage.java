package com.adjustclient.lunar.apollo.protocol.cooldown;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

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
