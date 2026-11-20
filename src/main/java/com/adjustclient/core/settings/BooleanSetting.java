package com.adjustclient.core.settings;

import com.adjustclient.core.events.EventBus;
import com.adjustclient.core.events.SettingChangeEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.BooleanSupplier;

public final class BooleanSetting extends Setting<Boolean> implements BooleanValue, BooleanSupplier {
    private String onLabel;
    private String offLabel;
    private Object field56;

    public BooleanSetting(SettingContainer parent, String name, boolean value) {
        this(parent, name, value, "On", "Off", null);
    }
    public BooleanSetting(SettingContainer parent, String name, boolean value, BooleanSupplier hideIf) {
        this(parent, name, value, "On", "Off", hideIf);
    }
    public BooleanSetting(SettingContainer parent, String name, boolean value, String onLabel, String offLabel) {
        this(parent, name, value, onLabel, offLabel, null);
    }
    public BooleanSetting(SettingContainer parent, String name, boolean value, String onLabel, String offLabel, BooleanSupplier hideIf) {
        super(parent, name, value, hideIf);
        this.onLabel = onLabel;
        this.offLabel = offLabel;
    }
    public String getOnLabel() { return onLabel; }
    public String getOffLabel() { return offLabel; }
    public BooleanSetting description(String description) { this.description = description; return this; }

    public void read(DataInputStream input) throws IOException {
        Boolean next = input.readBoolean();
        Boolean old = getValue();
        if (setValueSilently(next)) notifyListeners(old, next);
        EventBus.post(new SettingChangeEvent(this, getValue()));
        notifyListenersExcept(previousValue, getValue());
        previousValue = null;
    }

    public void write(DataOutputStream output) throws IOException {
        output.writeBoolean(getValue());
    }

    @Override public boolean getAsBoolean() { return isEnabled(); }

    public Boolean isEnabled() {
        return getValue() && (hideIf == null || !hideIf.getAsBoolean());
    }
}
