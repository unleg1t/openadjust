package com.adjustclient.core.settings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ModeSetting<E extends ModeOption> extends Setting<ModeOption> {
    private final List<E> options;
    private Object field52;

    @SafeVarargs public ModeSetting(SettingContainer parent, String name, E value, E... options) {
        this(parent, name, value, null, Arrays.asList(options));
    }
    @SafeVarargs public ModeSetting(SettingContainer parent, String name, E value, BooleanSupplier hideIf, E... options) {
        this(parent, name, value, hideIf, Arrays.asList(options));
    }
    public ModeSetting(SettingContainer parent, String name, E value, Collection<E> options) {
        this(parent, name, value, null, options);
    }
    public ModeSetting(SettingContainer parent, String name, E value, BooleanSupplier hideIf, Collection<E> options) {
        super(parent, name, value, hideIf);
        this.options = new ArrayList<>(options);
    }
    public List<E> getOptions() { return options; }
    @SuppressWarnings("unchecked") public E getValue() { return (E) super.getValue(); }

    public void read(DataInputStream input) throws IOException {
        int hash = input.readInt();
        if (hash != 0) {
            for (E option : options) {
                if (hashName(option.getName()) == hash) {
                    setMode(option);
                    break;
                }
            }
        }
        fireChanged();
    }

    public void write(DataOutputStream output) throws IOException {
        E value = getValue();
        output.writeInt(value == null || value.getName() == null ? 0 : hashName(value.getName()));
    }

    @Override public void setValue(ModeOption value) {
        if (value != null) super.setValue(value);
        else if (getValue() == null) throw new RuntimeException("?! x1: " + getName());
    }

    public static int hashName(String name) {
        int hash = 911;
        for (byte value : name.getBytes(StandardCharsets.UTF_16LE)) hash = hash * 33 + value;
        return hash;
    }

    public void setMode(ModeOption mode) {
        if (mode != null) super.setValue(mode);
        else if (getValue() == null) throw new RuntimeException("?! x1: " + getName());
    }
}
