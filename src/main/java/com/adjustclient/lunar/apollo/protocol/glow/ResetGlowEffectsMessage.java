package com.adjustclient.lunar.apollo.protocol.glow;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class ResetGlowEffectsMessage implements ProtoMessage {
    public Object field4;

    public static ResetGlowEffectsMessage parse(ProtoReader reader) {
        ResetGlowEffectsMessage message = new ResetGlowEffectsMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
