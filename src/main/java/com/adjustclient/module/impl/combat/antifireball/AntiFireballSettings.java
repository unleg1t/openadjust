package com.adjustclient.module.impl.combat.antifireball;

import com.adjustclient.module.util.CpsSettings;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.setting.SettingContainer;

final class AntiFireballSettings {
    final CpsSettings cps;
    final NumberSetting range;

    AntiFireballSettings(SettingContainer parent) {
        cps = new CpsSettings(parent, 16, 20);
        range = new NumberSetting(parent, "Range", 6.0, 3.0, 12.0, 0.1).formatter(NumberSetting.METERS);
    }

    double getRange() {
        return range.getValue().doubleValue();
    }
}
