package com.adjustclient.managers.prefs;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StringListPreferenceCodec extends PreferenceCodec<String[]> {
    @Override public byte[] encode() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);
            output.writeInt(value.length);
            for (String entry : value) output.writeUTF(entry);
            return bytes.toByteArray();
        } catch (Exception ignored) {
            return new byte[0];
        }
    }
}
