package com.adjustclient.core.settings;

import com.adjustclient.core.bind.Bind;
import com.adjustclient.core.bind.BindKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.function.BooleanSupplier;

public class KeybindSetting extends Setting<Bind> {
    private Object field48;

    public KeybindSetting(SettingContainer parent, String name, Bind bind) { this(parent, name, bind, null); }
    public KeybindSetting(SettingContainer parent, String name, Bind bind, BooleanSupplier hideIf) {
        super(parent, name, bind, hideIf);
    }

    public void read(DataInputStream input) { }
    public void write(DataOutputStream output) { }

    @Override public void reset() {
        Bind bind = getValue();
        if (!bind.mutable || bind.field12 == null) return;
        bind.clearKeys();
        for (BindKey key : bind.field12) bind.setKey(key);
    }

    public boolean isChanged() { return false; }

    public boolean isDown() { return getValue().isDown(); }
}
