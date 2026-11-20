package com.adjustclient.core.settings;

import com.adjustclient.utilities.ByteUtil;
import com.adjustclient.utilities.HashUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class ExpandSetting extends Setting<List<Setting<?>>> implements SettingContainer {
    public BooleanSupplier enabledPredicate;
    public List<Setting<?>> visibleSettings;
    private Object field56;

    public void setActiveSupplier(BooleanSupplier supplier) { enabledPredicate = supplier; }

    public ExpandSetting(SettingContainer parent, String name) { this(parent, name, null); }
    public ExpandSetting(SettingContainer parent, String name, BooleanSupplier hideIf) {
        super(parent, name, new ArrayList<>(), hideIf);
        visibleSettings = Collections.unmodifiableList(value);
    }

    @Override public List<Setting<?>> B_() { return visibleSettings; }

    @Override public void read(DataInputStream input) throws IOException {
        List<Setting<?>> remaining = getChangedSettings();
        int count = ByteUtil.readVarInt(input);
        for (int i = 0; i < count; i++) {
            int hash = input.readInt();
            byte[] bytes = new byte[ByteUtil.readVarInt(input)];
            input.readFully(bytes);
            for (Setting<?> setting : value) {
                if (HashUtil.hash(getDisplayName() + setting.getDisplayName()) != hash) continue;
                setting.read(new DataInputStream(new ByteArrayInputStream(bytes)));
                remaining.remove(setting);
                break;
            }
        }
        for (Setting<?> setting : remaining) setting.reset();
    }

    @Override public void write(DataOutputStream output) throws IOException {
        List<Setting<?>> changed = getChangedSettings();
        ByteUtil.writeVarInt(output, changed.size());
        for (Setting<?> setting : changed) {
            output.writeInt(HashUtil.hash(getDisplayName() + setting.getDisplayName()));
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            setting.write(new DataOutputStream(bytes));
            byte[] serialized = bytes.toByteArray();
            ByteUtil.writeVarInt(output, serialized.length);
            output.write(serialized);
        }
    }

    @Override public void reset() {}

    @Override public boolean isChanged() { return true; }

    public List<Setting<?>> getChangedSettings() {
        List<Setting<?>> changed = new ArrayList<>(value.size());
        for (Setting<?> setting : value) {
            if (setting.getParent() == this && setting.isChanged()) changed.add(setting);
        }
        return changed;
    }

    @Override public List<Setting<?>> getSettings() { return value; }
}
