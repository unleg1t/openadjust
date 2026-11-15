package com.adjustclient.lunar.apollo.protocol.limb;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.lunar.apollo.protocol.common.ApolloUuid;
import com.adjustclient.util.proto.ProtoReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class HideArmorPiecesMessage implements ProtoMessage {
    public ApolloUuid playerUuid;
    public List<ApolloArmorPiece> armorPieces;
    public Object field12;

    public static HideArmorPiecesMessage parse(ProtoReader reader) {
        ApolloUuid playerUuid = null;
        List<ApolloArmorPiece> armorPieces = new ArrayList<>();
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 10 -> playerUuid = ApolloUuid.parse(reader.readMessage());
                case 16 -> {
                    long value = reader.readEnum();
                    armorPieces.add(switch (value >= 0 && value <= 4 ? (int) value : -1) {
                        case 0 -> ApolloArmorPiece.ARMOR_PIECE_UNSPECIFIED;
                        case 1 -> ApolloArmorPiece.ARMOR_PIECE_HELMET;
                        case 2 -> ApolloArmorPiece.ARMOR_PIECE_CHESTPLATE;
                        case 3 -> ApolloArmorPiece.ARMOR_PIECE_LEGGINGS;
                        case 4 -> ApolloArmorPiece.ARMOR_PIECE_BOOTS;
                        default -> ApolloArmorPiece.UNRECOGNIZED;
                    });
                }
                case 18 -> {
                    ByteBuffer bytes = reader.readByteBuffer();
                    ProtoReader packed = new ProtoReader(bytes);
                    while (bytes.hasRemaining()) {
                        long value = packed.readEnum();
                        // ProtoReader owns an independent slice. Walk the already validated
                        // encoding on this view solely to detect the packed field's end.
                        while ((bytes.get() & 0x80) != 0) {}
                        armorPieces.add(switch (value >= 0 && value <= 4 ? (int) value : -1) {
                            case 0 -> ApolloArmorPiece.ARMOR_PIECE_UNSPECIFIED;
                            case 1 -> ApolloArmorPiece.ARMOR_PIECE_HELMET;
                            case 2 -> ApolloArmorPiece.ARMOR_PIECE_CHESTPLATE;
                            case 3 -> ApolloArmorPiece.ARMOR_PIECE_LEGGINGS;
                            case 4 -> ApolloArmorPiece.ARMOR_PIECE_BOOTS;
                            default -> ApolloArmorPiece.UNRECOGNIZED;
                        });
                    }
                }
                default -> reader.skipField(tag);
            }
        }
        HideArmorPiecesMessage message = new HideArmorPiecesMessage();
        message.playerUuid = playerUuid;
        message.armorPieces = armorPieces;
        return message;
    }
}
