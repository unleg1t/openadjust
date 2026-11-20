package com.adjustclient.core.gui.tabgui;

import com.adjustclient.core.settings.Setting;
import java.util.function.BooleanSupplier;

public abstract class SettingNode<T extends Setting<?>> extends LabelNode {

    /** The edited setting. */
    public T setting;

    protected SettingNode() { }
    protected SettingNode(String label, T setting) {
        super(label);
        this.setting = setting;
    }

    @Override
    public boolean isHidden() {
        BooleanSupplier hideIf = this.setting.hideIf;
        if (hideIf == null) {
            return false;
        }
        return hideIf.getAsBoolean();
    }
}
