package com.adjustclient.utilities.lunar.apollo.protocol.title;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.*;
import com.adjustclient.utilities.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class DisplayTitleMessage implements ProtoMessage {
    public Object field28;
    public ApolloTitleType type = ApolloTitleType.TITLE_TYPE_UNSPECIFIED;
    public ApolloComponent text = null;
    public ProtoDuration fadeIn = null;
    public ProtoDuration stay = null;
    public ProtoDuration fadeOut = null;
    public String field24 = "";

    public static DisplayTitleMessage parse(ProtoReader reader) {
        DisplayTitleMessage message = new DisplayTitleMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 8: { long value = reader.readEnum(); message.type = value == 0 ? ApolloTitleType.TITLE_TYPE_UNSPECIFIED : value == 1 ? ApolloTitleType.TITLE_TYPE_TITLE : value == 2 ? ApolloTitleType.TITLE_TYPE_SUBTITLE : ApolloTitleType.UNRECOGNIZED; } break;
                case 18: message.text = ApolloComponent.parse(reader.readMessage()); break;
                case 29: reader.readFixed32();
                case 34: message.fadeIn = ProtoDuration.parse(reader.readMessage()); break;
                case 42: message.stay = ProtoDuration.parse(reader.readMessage()); break;
                case 50: message.fadeOut = ProtoDuration.parse(reader.readMessage()); break;
                case 58: message.field24 = reader.readString(); break;
                case 69: reader.readFixed32();
                case 77: reader.readFixed32();
                default: reader.skipField(tag); break;
            }
        }
        return message;
    }
}
