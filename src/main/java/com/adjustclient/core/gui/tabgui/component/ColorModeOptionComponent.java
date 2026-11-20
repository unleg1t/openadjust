package com.adjustclient.core.gui.tabgui.component;

import com.adjustclient.core.settings.ColorSetting;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.core.settings.ModeSetting;
import java.awt.Color;
import com.adjustclient.managers.util.ServiceUtil;

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
