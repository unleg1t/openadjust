package com.adjustclient.module.impl.render.esp.settings;

import com.adjustclient.setting.*;
import java.awt.Color;

public class TntEspSettings extends BorderedEspSettings implements EspBarSettings {
    public final ExpandSetting countdown;
    public final BooleanSetting countdownEnabled;
    public final BooleanSetting countdownThinner;
    public final ModeSetting<EspBarPosition> countdownPosition;
    public final ColorSetting countdownColor;
    public TntEspSettings(SettingContainer parent) {
        super(parent, "TNT", true, false);
        countdown = new ExpandSetting(target, "Countdown"); countdownEnabled = new BooleanSetting(countdown, "Enabled", true, "Yes", "No");
        countdown.enabledPredicate = () -> countdownEnabled.isEnabled();
        countdownThinner = new BooleanSetting(countdown, "Thinner", false); countdownPosition = new ModeSetting<>(countdown, "Position", EspBarPosition.BOTTOM, EspBarPosition.values()); countdownColor = new ColorSetting(countdown, "Color", new Color(199, 88, 77, 255));
    }
    public int getCountdownColor() { return countdownColor.getColor().getRGB(); }
    public boolean isCountdownEnabled() { return countdownEnabled.isEnabled(); }
}
