package com.adjustclient.lunar.apollo.protocol.tebex;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class OpenTebexEmbeddedCheckoutMessage implements ProtoMessage {
    private final String field4;
    private final String field8;
    private final Object field12 = null;

    public OpenTebexEmbeddedCheckoutMessage(String field4, String field8) {
        this.field4 = field4;
        this.field8 = field8;
    }

    public String getField4() { return field4; }

    public String getField8() { return field8; }

    public static OpenTebexEmbeddedCheckoutMessage parse(ProtoReader in) {
        String field4 = "";
        String field8 = "";
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> field4 = in.readString();
                case 18 -> field8 = in.readString();
                default -> in.skipField(tag);
            }
        }
        return new OpenTebexEmbeddedCheckoutMessage(field4, field8);
    }
}
