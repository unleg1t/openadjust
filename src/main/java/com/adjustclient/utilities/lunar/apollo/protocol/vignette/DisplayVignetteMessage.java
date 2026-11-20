package com.adjustclient.utilities.lunar.apollo.protocol.vignette;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class DisplayVignetteMessage implements ProtoMessage {
    public Object field8;
    public String resourceLocation = "";

    public static DisplayVignetteMessage parse(ProtoReader reader) {
        DisplayVignetteMessage message = new DisplayVignetteMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.resourceLocation = reader.readString(); break;
                case 21: reader.readFixed32();
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
