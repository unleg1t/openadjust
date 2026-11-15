package com.adjustclient.lunar.apollo.protocol.evnt;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class OverrideHeartTextureMessage implements ProtoMessage {
    public Object field4;

    public static OverrideHeartTextureMessage parse(ProtoReader reader) {
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 8, 16 -> reader.readEnum();
                default -> reader.skipField(tag);
            }
        }
        OverrideHeartTextureMessage message = new OverrideHeartTextureMessage();
        return message;
    }
}
