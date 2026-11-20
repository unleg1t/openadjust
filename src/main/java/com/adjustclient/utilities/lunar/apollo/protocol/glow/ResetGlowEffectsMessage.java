package com.adjustclient.utilities.lunar.apollo.protocol.glow;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

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
