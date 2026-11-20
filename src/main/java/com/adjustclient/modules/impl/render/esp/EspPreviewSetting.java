package com.adjustclient.modules.impl.render.esp;

import com.adjustclient.core.settings.Setting;
import com.adjustclient.core.settings.SettingContainer;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public final class EspPreviewSetting extends Setting<com.adjustclient.modules.impl.render.esp.settings.EntityEspSettings> {
    public Object field48;

    public EspPreviewSetting(SettingContainer parent, com.adjustclient.modules.impl.render.esp.settings.EntityEspSettings holder) {
        super(parent, "Preview", holder, null);
        field48 = holder;
    }

    @Override public void write(DataOutputStream output) { }

    @Override public void read(DataInputStream input) { }

    @Override public boolean isChanged() { return false; }
}
