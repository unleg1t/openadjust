package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;

public final class ApolloCuboid3D implements ProtoMessage {
    private final Object field4 = null;

    public ApolloCuboid3D() {}

    public static ApolloCuboid3D parse(ProtoReader in) {
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 9:
                case 17:
                case 25:
                case 33:
                case 41:
                case 49:
                    in.readFixed64();
                    break;
                default: in.skipField(tag);
            }
        }
        return new ApolloCuboid3D();
    }
}
