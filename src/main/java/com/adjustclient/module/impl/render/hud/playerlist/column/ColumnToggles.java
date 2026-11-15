package com.adjustclient.module.impl.render.hud.playerlist.column;

import com.adjustclient.setting.BooleanSetting;

import java.util.Map;

public final class ColumnToggles {

    public Map<PlayerListColumn, BooleanSetting> settings;

    public ColumnToggles(com.adjustclient.setting.ExpandSetting parent, Runnable changed) {
        settings = new java.util.HashMap<>();
        for (PlayerListColumn column : PlayerListColumn.COLUMNS) {
            if (!column.isToggleable()) continue;
            BooleanSetting setting = new BooleanSetting(parent, column.getName(), column.isEnabledByDefault());
            setting.addListener(changed);
            settings.put(column, setting);
        }
    }

    public boolean isEnabled(PlayerListColumn column) {
        BooleanSetting setting = this.settings.get(column);
        if (setting == null) {
            return true;
        }
        return setting.getValue();
    }
}
