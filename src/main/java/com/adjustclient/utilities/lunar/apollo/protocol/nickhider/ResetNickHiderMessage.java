package com.adjustclient.utilities.lunar.apollo.protocol.nickhider;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ResetNickHiderMessage implements ProtoMessage {
    public Object field4;

    public static ResetNickHiderMessage parse(ProtoReader reader) {
        for (int tag; (tag = reader.readTag()) != 0;) {
            reader.skipField(tag);
        }
        ResetNickHiderMessage message = new ResetNickHiderMessage();
        return message;
    }
}
