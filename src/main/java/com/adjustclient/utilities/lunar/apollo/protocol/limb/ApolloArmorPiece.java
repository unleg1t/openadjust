package com.adjustclient.utilities.lunar.apollo.protocol.limb;

public enum ApolloArmorPiece {
    ARMOR_PIECE_UNSPECIFIED(0),
    ARMOR_PIECE_HELMET(0),
    ARMOR_PIECE_CHESTPLATE(0),
    ARMOR_PIECE_LEGGINGS(0),
    ARMOR_PIECE_BOOTS(0),
    UNRECOGNIZED(0);

    public final int number;
    public final Object field16 = null;

    ApolloArmorPiece(int number) {
        this.number = number;
    }
}
