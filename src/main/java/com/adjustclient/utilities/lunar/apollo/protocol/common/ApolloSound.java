package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ApolloSound implements ProtoMessage {
    private final ApolloSimpleResourceLocationSound simpleResourceLocationSound;
    private final Object field8 = null;

    public ApolloSound(ApolloSimpleResourceLocationSound simpleResourceLocationSound) {
        this.simpleResourceLocationSound = simpleResourceLocationSound;
    }

    public ApolloSimpleResourceLocationSound getSimpleResourceLocationSound() {
        return simpleResourceLocationSound;
    }

    public static ApolloSound parse(ProtoReader in) {
        ApolloSimpleResourceLocationSound sound = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            if (tag == 10) {
                sound = ApolloSimpleResourceLocationSound.parse(in.readMessage());
            } else {
                in.skipField(tag);
            }
        }
        return new ApolloSound(sound);
    }
}
