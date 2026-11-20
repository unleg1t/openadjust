package com.adjustclient.utilities.lunar.apollo.protocol.evnt;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class CharacterAbilityMessage implements ProtoMessage {
    public ApolloCharacterType characterType;
    public List<String> abilities;
    public Object field12;

    public static CharacterAbilityMessage parse(ProtoReader reader) {
        ApolloCharacterType characterType = ApolloCharacterType.CHARACTER_TYPE_UNSPECIFIED;
        List<String> abilities = new ArrayList<>();
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
                case 18 -> abilities.add(reader.readString());
                default -> reader.skipField(tag);
            }
        }
        CharacterAbilityMessage message = new CharacterAbilityMessage();
        message.characterType = characterType;
        message.abilities = abilities;
        return message;
    }
}
