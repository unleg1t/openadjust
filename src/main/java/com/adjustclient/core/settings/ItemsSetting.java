package com.adjustclient.core.settings;

import com.adjustclient.utilities.ByteUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.BooleanSupplier;

public class ItemsSetting extends Setting<IntList> {
    public final IntArrayList availableIds;
    private Object field52;

    public ItemsSetting(SettingContainer parent, String name, IntList value) { this(parent, name, value, null); }
    public ItemsSetting(SettingContainer parent, String name, IntList value, BooleanSupplier hideIf) {
        super(parent, name, value, hideIf);
        availableIds = new IntArrayList();
    }
    public ItemsSetting(SettingContainer parent, String name, IntList value, IntArrayList availableIds, BooleanSupplier hideIf) {
        super(parent, name, value, hideIf);
        this.availableIds = availableIds;
    }

    public boolean contains(int id) { return getValue().contains(id); }

    public void read(DataInputStream input) throws IOException {
        getValue().clear();
        int count = ByteUtil.readVarInt(input);
        for (int i = 0; i < count; i++) {
            int id = input.readInt();
            if (!getValue().contains(id) && availableIds.contains(id)) getValue().add(id);
        }
        fireChanged();
    }

    public void write(DataOutputStream output) throws IOException {
        ByteUtil.writeVarInt(output, getValue().size());
        for (IntIterator iterator = getValue().iterator(); iterator.hasNext();) output.writeInt(iterator.nextInt());
    }

    public void add(Integer id) {
        if (getValue().contains(id)) return;
        if (!availableIds.contains(id.intValue())) return;
        getValue().add(id);
        notifyListeners(getValue(), getValue());
    }

    public void remove(Integer id) {
        if (!getValue().contains(id)) return;
        getValue().remove(id);
        notifyListeners(getValue(), getValue());
    }

    public boolean isChanged() { return true; }
}
