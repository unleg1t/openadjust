package com.adjustclient.utilities.script.engine;

import com.adjustclient.core.settings.Setting;
import com.adjustclient.core.settings.SettingContainer;
import com.adjustclient.core.gui.clickgui.ClickGuiItem;
import java.util.ArrayList;
import java.util.List;

public final class ScriptModule implements SettingContainer, ClickGuiItem {
    public Object field4;
    public Object field8;
    public Object field12;
    public Object field16;
    private final List<Setting<?>> settings = new ArrayList<>();

    @Override
    public List<Setting<?>> getSettings() {
        return settings;
    }
}
