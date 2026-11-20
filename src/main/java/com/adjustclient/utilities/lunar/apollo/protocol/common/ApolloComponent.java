package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class ApolloComponent implements ProtoMessage {
    private final String content;
    private final ApolloColor color;
    private final List<ApolloTextDecoration> decorations;
    private final List<ApolloComponent> children;
    private final Object field20 = null;

    public ApolloComponent(String content, ApolloColor color,
                           List<ApolloTextDecoration> decorations,
                           List<ApolloComponent> children) {
        this.content = content;
        this.color = color;
        this.decorations = decorations;
        this.children = children;
    }

    public String getContent() { return content; }
    public ApolloColor getColor() { return color; }
    public List<ApolloTextDecoration> getDecorations() { return decorations; }
    public List<ApolloComponent> getChildren() { return children; }

    public static ApolloComponent parse(ProtoReader in) {
        List<ApolloTextDecoration> decorations = new ArrayList<>();
        List<ApolloComponent> children = new ArrayList<>();
        String content = "";
        ApolloColor color = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10: content = in.readString(); break;
                case 18: color = ApolloColor.parse(in.readMessage()); break;
                case 24:
                    switch ((int) in.readEnum()) {
                        case 0: decorations.add(ApolloTextDecoration.TEXT_DECORATOR_UNSPECIFIED); break;
                        case 1: decorations.add(ApolloTextDecoration.TEXT_DECORATOR_OBFUSCATED); break;
                        case 2: decorations.add(ApolloTextDecoration.TEXT_DECORATOR_BOLD); break;
                        case 3: decorations.add(ApolloTextDecoration.TEXT_DECORATOR_STRIKETHROUGH); break;
                        case 4: decorations.add(ApolloTextDecoration.TEXT_DECORATOR_UNDERLINED); break;
                        case 5: decorations.add(ApolloTextDecoration.TEXT_DECORATOR_ITALIC); break;
                        default: decorations.add(ApolloTextDecoration.UNRECOGNIZED);
                    }
                    break;
                case 26:
                    ByteBuffer bytes = in.readByteBuffer();
                    ProtoReader packed = new ProtoReader(bytes);
                    while (bytes.hasRemaining()) {
                        int value = (int) packed.readEnum();
                        // ProtoReader slices its input. Advance this independent view
                        // over the validated varint to detect the packed payload's end.
                        while ((bytes.get() & 0x80) != 0) {}
                        switch (value) {
                            case 0: decorations.add(ApolloTextDecoration.TEXT_DECORATOR_UNSPECIFIED); break;
                            case 1: decorations.add(ApolloTextDecoration.TEXT_DECORATOR_OBFUSCATED); break;
                            case 2: decorations.add(ApolloTextDecoration.TEXT_DECORATOR_BOLD); break;
                            case 3: decorations.add(ApolloTextDecoration.TEXT_DECORATOR_STRIKETHROUGH); break;
                            case 4: decorations.add(ApolloTextDecoration.TEXT_DECORATOR_UNDERLINED); break;
                            case 5: decorations.add(ApolloTextDecoration.TEXT_DECORATOR_ITALIC); break;
                            default: decorations.add(ApolloTextDecoration.UNRECOGNIZED);
                        }
                    }
                    break;
                case 34: children.add(ApolloComponent.parse(in.readMessage())); break;
                default: in.skipField(tag);
            }
        }
        return new ApolloComponent(content, color, decorations, children);
    }
}
