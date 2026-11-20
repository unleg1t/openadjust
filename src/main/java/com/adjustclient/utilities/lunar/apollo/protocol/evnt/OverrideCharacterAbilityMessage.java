package com.adjustclient.utilities.lunar.apollo.protocol.evnt;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class OverrideCharacterAbilityMessage implements ProtoMessage {
    public List<CharacterAbilityMessage> characterAbilities;
    public Object field8;

    public static OverrideCharacterAbilityMessage parse(ProtoReader reader) {
        List<CharacterAbilityMessage> characterAbilities = new ArrayList<>();
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 10 -> characterAbilities.add(CharacterAbilityMessage.parse(reader.readMessage()));
                default -> reader.skipField(tag);
            }
        }
        OverrideCharacterAbilityMessage message = new OverrideCharacterAbilityMessage();
        message.characterAbilities = characterAbilities;
        return message;
    }
}
