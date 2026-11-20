package com.adjustclient.modules.impl.combat.antifireball;

import com.adjustclient.modules.util.CpsSettings;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.core.settings.SettingContainer;

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
