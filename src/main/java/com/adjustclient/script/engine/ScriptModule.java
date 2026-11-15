package com.adjustclient.script.engine;

import com.adjustclient.setting.Setting;
import com.adjustclient.setting.SettingContainer;
import com.adjustclient.ui.clickgui.ClickGuiItem;
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
