package com.adjustclient.utilities.lunar.apollo.protocol.staffmod;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class DisableStaffModsMessage implements ProtoMessage {
    private final List<ApolloStaffMod> staffMods;
    private final Object field8 = null;

    public DisableStaffModsMessage(List<ApolloStaffMod> staffMods) {
        this.staffMods = staffMods;
    }

    public List<ApolloStaffMod> getStaffMods() { return staffMods; }

    public static DisableStaffModsMessage parse(ProtoReader in) {
        List<ApolloStaffMod> staffMods = new ArrayList<>();
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 8 -> {
                    long value = in.readEnum();
                    staffMods.add(value == 0 ? ApolloStaffMod.STAFF_MOD_UNSPECIFIED
                            : value == 1 ? ApolloStaffMod.STAFF_MOD_XRAY
                            : ApolloStaffMod.UNRECOGNIZED);
                }
                case 10 -> {
                    ByteBuffer buffer = in.readByteBuffer();
                    ProtoReader packed = new ProtoReader(buffer);
                    int remaining = buffer.remaining();
                    while (remaining > 0) {
                        int next = packed.readBool();
                        --remaining;
                        long value = next & 0x7f;
                        int count = 1;
                        while ((next & 0x80) != 0) {
                            if (count >= 10) throw packed.truncated("Malformed varint");
                            next = packed.readBool();
                            --remaining;
                            value |= (long) (next & 0x7f) << (7 * count++);
                        }
                        // The packed path narrows to int; the unpacked path compares all 64 bits.
                        int number = (int) value;
                        staffMods.add(number == 0 ? ApolloStaffMod.STAFF_MOD_UNSPECIFIED
                                : number == 1 ? ApolloStaffMod.STAFF_MOD_XRAY
                                : ApolloStaffMod.UNRECOGNIZED);
                    }
                }
                default -> in.skipField(tag);
            }
        }
        return new DisableStaffModsMessage(staffMods);
    }
}
