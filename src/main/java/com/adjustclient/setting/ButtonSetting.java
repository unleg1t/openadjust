package com.adjustclient.setting;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.function.BooleanSupplier;

public final class ButtonSetting extends Setting<Runnable> {
    private String buttonText;

    public ButtonSetting(SettingContainer parent, String name, Runnable action) {
        this(parent, name, action, null);
    }

    public ButtonSetting(SettingContainer parent, String name, Runnable action, BooleanSupplier hideIf) {
        super(parent, name, action, hideIf);
        buttonText = name;
    }

    public String getButtonText() { return buttonText; }

    public void read(DataInputStream input) { }

    public void write(DataOutputStream output) { }

    public boolean isChanged() { return false; }
}
