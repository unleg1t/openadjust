package com.adjustclient.utilities.lunar.apollo.protocol.limb;

public enum ApolloBodyPart {
    BODY_PART_UNSPECIFIED(0),
    BODY_PART_HEAD(0),
    BODY_PART_TORSO(0),
    BODY_PART_LEFT_ARM(0),
    BODY_PART_RIGHT_ARM(0),
    BODY_PART_LEFT_LEG(0),
    BODY_PART_RIGHT_LEG(0),
    UNRECOGNIZED(0);

    public final int number;
    public final Object field16 = null;

    ApolloBodyPart(int number) {
        this.number = number;
    }
}
