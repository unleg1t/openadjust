package com.adjustclient.lunar.apollo.protocol.limb;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.lunar.apollo.protocol.common.ApolloUuid;
import com.adjustclient.util.proto.ProtoReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class HideBodyPartMessage implements ProtoMessage {
    public ApolloUuid playerUuid;
    public List<ApolloBodyPart> bodyParts;
    public Object field12;

    public static HideBodyPartMessage parse(ProtoReader reader) {
        ApolloUuid playerUuid = null;
        List<ApolloBodyPart> bodyParts = new ArrayList<>();
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag) {
                case 10 -> playerUuid = ApolloUuid.parse(reader.readMessage());
                case 16 -> {
                    long value = reader.readEnum();
                    bodyParts.add(switch ((int) value) {
                        case 0 -> ApolloBodyPart.BODY_PART_UNSPECIFIED;
                        case 1 -> ApolloBodyPart.BODY_PART_HEAD;
                        case 2 -> ApolloBodyPart.BODY_PART_TORSO;
                        case 3 -> ApolloBodyPart.BODY_PART_LEFT_ARM;
                        case 4 -> ApolloBodyPart.BODY_PART_RIGHT_ARM;
                        case 5 -> ApolloBodyPart.BODY_PART_LEFT_LEG;
                        case 6 -> ApolloBodyPart.BODY_PART_RIGHT_LEG;
                        default -> ApolloBodyPart.UNRECOGNIZED;
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
                        bodyParts.add(switch ((int) value) {
                            case 0 -> ApolloBodyPart.BODY_PART_UNSPECIFIED;
                            case 1 -> ApolloBodyPart.BODY_PART_HEAD;
                            case 2 -> ApolloBodyPart.BODY_PART_TORSO;
                            case 3 -> ApolloBodyPart.BODY_PART_LEFT_ARM;
                            case 4 -> ApolloBodyPart.BODY_PART_RIGHT_ARM;
                            case 5 -> ApolloBodyPart.BODY_PART_LEFT_LEG;
                            case 6 -> ApolloBodyPart.BODY_PART_RIGHT_LEG;
                            default -> ApolloBodyPart.UNRECOGNIZED;
                        });
                    }
                }
                default -> reader.skipField(tag);
            }
        }
        HideBodyPartMessage message = new HideBodyPartMessage();
        message.playerUuid = playerUuid;
        message.bodyParts = bodyParts;
        return message;
    }
}
