package com.adjustclient.lunar.apollo.protocol.paynow;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class OpenPayNowEmbeddedCheckoutMessage implements ProtoMessage {
    private final String field4;
    private final Object field8 = null;

    public OpenPayNowEmbeddedCheckoutMessage(String field4) {
        this.field4 = field4;
    }

    public String getField4() { return field4; }

    public static OpenPayNowEmbeddedCheckoutMessage parse(ProtoReader in) {
        String field4 = "";
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> field4 = in.readString();
                default -> in.skipField(tag);
            }
        }
        return new OpenPayNowEmbeddedCheckoutMessage(field4);
    }
}
