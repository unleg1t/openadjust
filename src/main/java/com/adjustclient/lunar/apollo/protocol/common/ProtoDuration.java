package com.adjustclient.lunar.apollo.protocol.common;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class ProtoDuration implements ProtoMessage {
    private final Object field4 = null;

    public ProtoDuration() {}

    public static ProtoDuration parse(ProtoReader in) {
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 8:
                case 16:
                    in.readEnum();
                    break;
                default: in.skipField(tag);
            }
        }
        return new ProtoDuration();
    }
}
