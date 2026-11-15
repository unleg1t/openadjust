package com.adjustclient.lunar.apollo.protocol.notification;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.lunar.apollo.protocol.common.*;
import com.adjustclient.util.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class DisplayNotificationMessage implements ProtoMessage {
    public Object field32;
    public String field4 = "";
    public String title = "";
    public String description = "";
    public ProtoDuration duration = null;
    public String field20 = "";
    public String field24 = "";
    public ApolloIcon icon = null;

    public static DisplayNotificationMessage parse(ProtoReader reader) {
        DisplayNotificationMessage message = new DisplayNotificationMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10: message.field4 = reader.readString(); break;
                case 18: message.title = reader.readString(); break;
                case 26: message.description = reader.readString(); break;
                case 34: message.duration = ProtoDuration.parse(reader.readMessage()); break;
                case 42: message.field20 = reader.readString(); break;
                case 50: message.field24 = reader.readString(); break;
                case 58: message.icon = ApolloIcon.parse(reader.readMessage()); break;
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
