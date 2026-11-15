package com.adjustclient.lunar.apollo.protocol.evnt;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class CloseGuiMessage implements ProtoMessage {
    public ApolloGuiType guiType;
    public Object field8;

    public static CloseGuiMessage parse(ProtoReader reader) {
        ApolloGuiType guiType = ApolloGuiType.GUI_TYPE_UNSPECIFIED;
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 8 -> guiType = switch ((int) reader.readEnum()) {
                    case 0 -> ApolloGuiType.GUI_TYPE_UNSPECIFIED;
                    case 1 -> ApolloGuiType.GUI_TYPE_SUIT;
                    case 2 -> ApolloGuiType.GUI_TYPE_CHARACTER_SELECTION;
                    case 3 -> ApolloGuiType.GUI_TYPE_OBSERVER_UI;
                    case 4 -> ApolloGuiType.GUI_TYPE_WELCOME_UI;
                    case 5 -> ApolloGuiType.GUI_TYPE_ESC;
                    case 6 -> ApolloGuiType.GUI_TYPE_PLAYER_INVENTORY;
                    default -> ApolloGuiType.UNRECOGNIZED;
                };
                default -> reader.skipField(tag);
            }
        }
        CloseGuiMessage message = new CloseGuiMessage();
        message.guiType = guiType;
        return message;
    }
}
