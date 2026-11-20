package com.adjustclient.utilities.lunar.apollo.protocol.vignette;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ResetVignetteMessage implements ProtoMessage {
    public Object field4;

    public static ResetVignetteMessage parse(ProtoReader reader) {
        ResetVignetteMessage message = new ResetVignetteMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
