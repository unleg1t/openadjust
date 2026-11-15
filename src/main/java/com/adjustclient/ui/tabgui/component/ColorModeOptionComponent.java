package com.adjustclient.ui.tabgui.component;

import com.adjustclient.setting.ColorSetting;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.setting.ModeSetting;
import java.awt.Color;
import com.adjustclient.service.util.ServiceUtil;

final class ColorModeOptionComponent extends ModeOptionComponent {

    ColorModeOptionComponent(ColorComponent colorComponent, ModeSetting<ColorSetting.Mode> typeSetting, ColorSetting.Mode option) {
        super(option.getName(), typeSetting, option);
        typeSetting.addListener(() -> {
            if (ColorComponent.syncingSliders) { // set while the picker itself updates the sliders
                return;
            }
            ServiceUtil.markConfigurationDirty();
            ColorSetting colorSetting = (ColorSetting) colorComponent.setting;
            ColorSetting.Mode selected = typeSetting.getValue();
            ColorSetting.Mode mode = selected;
            if (colorSetting.disallowMainColor && selected == ColorSetting.Mode.MAIN) {
                mode = ColorSetting.Mode.PLAIN;
            }
            colorSetting.setMode(mode);
            Color color = (Color) colorSetting.getValue();
            colorSetting.notifyListenersExcept(color, color, colorComponent.changeListener);
        });
    }
}
