package com.adjustclient.lunar.apollo.protocol.evnt;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.lunar.apollo.protocol.common.ApolloUuid;
import com.adjustclient.util.proto.ProtoReader;

public final class OverrideCosmeticResourcesMessage implements ProtoMessage {
    public ApolloUuid playerUuid;
    public ApolloCharacterType characterType;
    public String resource3;
    public String resource4;
    public String resource5;
    public Object field24;

    public static OverrideCosmeticResourcesMessage parse(ProtoReader reader) {
        ApolloUuid playerUuid = null;
        ApolloCharacterType characterType = ApolloCharacterType.CHARACTER_TYPE_UNSPECIFIED;
        String resource3 = "";
        String resource4 = "";
        String resource5 = "";
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 10 -> playerUuid = ApolloUuid.parse(reader.readMessage());
                case 16 -> characterType = switch ((int) reader.readEnum()) {
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
                case 26 -> resource3 = reader.readString();
                case 34 -> resource4 = reader.readString();
                case 42 -> resource5 = reader.readString();
                default -> reader.skipField(tag);
            }
        }
        OverrideCosmeticResourcesMessage message = new OverrideCosmeticResourcesMessage();
        message.playerUuid = playerUuid;
        message.characterType = characterType;
        message.resource3 = resource3;
        message.resource4 = resource4;
        message.resource5 = resource5;
        return message;
    }
}
