package com.adjustclient.lunar.apollo.protocol.evnt;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.lunar.apollo.protocol.common.ApolloUuid;
import com.adjustclient.util.proto.ProtoReader;

public final class EventPlayerMessage implements ProtoMessage {
    public ApolloUuid playerUuid;
    public String name;
    public ApolloCharacterType characterType;
    public Object field16;

    public static EventPlayerMessage parse(ProtoReader reader) {
        ApolloUuid playerUuid = null;
        String name = "";
        ApolloCharacterType characterType = ApolloCharacterType.CHARACTER_TYPE_UNSPECIFIED;
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 10 -> playerUuid = ApolloUuid.parse(reader.readMessage());
                case 18 -> name = reader.readString();
                case 24 -> characterType = switch ((int) reader.readEnum()) {
                    case 0 -> ApolloCharacterType.CHARACTER_TYPE_UNSPECIFIED;
                    case 1 -> ApolloCharacterType.CHARACTER_TYPE_ANGEL;
                    case 2 -> ApolloCharacterType.CHARACTER_TYPE_AXEL;
                    case 3 -> ApolloCharacterType.CHARACTER_TYPE_BANJAX;
                    case 4 -> ApolloCharacterType.CHARACTER_TYPE_DAEDRA;
                    case 5 -> ApolloCharacterType.CHARACTER_TYPE_FROSKA;
                    case 6 -> ApolloCharacterType.CHARACTER_TYPE_GRANNY;
                    case 7 -> ApolloCharacterType.CHARACTER_TYPE_GREAT_MAGE;
                    case 8 -> ApolloCharacterType.CHARACTER_TYPE_IGNUS;
                    case 9 -> ApolloCharacterType.CHARACTER_TYPE_TULABOT;
                    case 10 -> ApolloCharacterType.CHARACTER_TYPE_SIR_BLOCK;
                    case 11 -> ApolloCharacterType.CHARACTER_TYPE_HARMONY;
                    default -> ApolloCharacterType.UNRECOGNIZED;
                };
                case 32 -> reader.readEnum();
                default -> reader.skipField(tag);
            }
        }
        EventPlayerMessage message = new EventPlayerMessage();
        message.playerUuid = playerUuid;
        message.name = name;
        message.characterType = characterType;
        return message;
    }
}
