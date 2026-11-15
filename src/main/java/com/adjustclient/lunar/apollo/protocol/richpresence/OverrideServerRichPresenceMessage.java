package com.adjustclient.lunar.apollo.protocol.richpresence;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class OverrideServerRichPresenceMessage implements ProtoMessage {
    public Object field28;
    public String field4 = "";
    public String field8 = "";
    public String field12 = "";
    public String field16 = "";
    public String field20 = "";
    public String field24 = "";

    public static OverrideServerRichPresenceMessage parse(ProtoReader reader) {
        OverrideServerRichPresenceMessage message = new OverrideServerRichPresenceMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.field4 = reader.readString(); break;
                case 18: message.field8 = reader.readString(); break;
                case 26: message.field12 = reader.readString(); break;
                case 34: message.field16 = reader.readString(); break;
                case 42: message.field20 = reader.readString(); break;
                case 50: message.field24 = reader.readString(); break;
                case 56: reader.readEnum();
                case 64: reader.readEnum();
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
