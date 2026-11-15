package com.adjustclient.setting;

import com.adjustclient.util.ByteUtil;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BooleanSupplier;

public final class StringSetting extends Setting<ArrayList<String>> {
    public boolean field45;
    public final int maxEntries;
    public final int maxLength;
    private Object field56;

    public StringSetting(SettingContainer parent, String name, Collection<String> value, int maxEntries, int maxLength,
                         BooleanSupplier hideIf) {
        super(parent, name, new ArrayList<>(value), hideIf);
        setValueSilently(new ArrayList<>(value));
        this.maxEntries = maxEntries;
        this.maxLength = maxLength;
    }
    public int getMaxEntries() { return maxEntries; }
    public int getMaxLength() { return maxLength; }

    public void read(DataInputStream input) throws IOException {
        getValue().clear();
        int count = ByteUtil.readVarInt(input);
        if (count == 0) getValue().add("");
        else for (int i = 0; i < count; i++) {
            String line = input.readUTF();
            if (i < maxEntries) getValue().add(line);
        }
        notifyListeners(previousValue, getValue());
    }

    public void write(DataOutputStream output) throws IOException {
        ByteUtil.writeVarInt(output, getValue().size());
        for (String line : getValue()) output.writeUTF(line);
    }

    @Override public void reset() {
        getValue().clear();
        getValue().addAll(getDefaultValue());
        previousValue = getValue();
        fireChanged();
    }

    public boolean isChanged() { return true; }
}
