package com.adjustclient.lunar.apollo.protocol.notification;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class ResetNotificationsMessage implements ProtoMessage {
    public Object field4;

    public static ResetNotificationsMessage parse(ProtoReader reader) {
        ResetNotificationsMessage message = new ResetNotificationsMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            reader.skipField(tag);
        }
        return message;
    }
}
