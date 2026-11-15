package com.adjustclient.lunar.apollo.protocol.evnt;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.lunar.apollo.protocol.common.ApolloUuid;
import com.adjustclient.lunar.apollo.protocol.common.ApolloColor;
import com.adjustclient.util.proto.ProtoReader;

public final class OverrideCharacterMessage implements ProtoMessage {
    public ApolloCharacterType characterType;
    public ApolloUuid playerUuid;
    public ApolloColor color;
    public String resource;
    public Object field20;

    public static OverrideCharacterMessage parse(ProtoReader reader) {
        ApolloCharacterType characterType = ApolloCharacterType.CHARACTER_TYPE_UNSPECIFIED;
        ApolloUuid playerUuid = null;
        ApolloColor color = null;
        String resource = "";
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 8 -> characterType = switch ((int) reader.readEnum()) {
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
                case 18 -> playerUuid = ApolloUuid.parse(reader.readMessage());
                case 26 -> color = ApolloColor.parse(reader.readMessage());
                case 32 -> reader.readEnum();
                case 42 -> resource = reader.readString();
                default -> reader.skipField(tag);
            }
        }
        OverrideCharacterMessage message = new OverrideCharacterMessage();
        message.characterType = characterType;
        message.playerUuid = playerUuid;
        message.color = color;
        message.resource = resource;
        return message;
    }
}
