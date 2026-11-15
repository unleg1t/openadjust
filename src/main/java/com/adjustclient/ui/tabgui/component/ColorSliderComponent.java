package com.adjustclient.ui.tabgui.component;

import com.adjustclient.setting.ColorSetting;
import com.adjustclient.setting.NumberSetting;
import java.awt.Color;
import java.util.List;

final class ColorSliderComponent extends SliderComponent {

    final ColorChannel channel;

    ColorSliderComponent(ColorComponent parent, ColorChannel channel, String label, NumberSetting setting) {
        super(label, setting);
        this.channel = channel;
        setting.addListener(() -> {
            if (ColorComponent.syncingSliders) {
                return;
            }
            ColorSetting colorSetting = (ColorSetting) parent.setting;
            boolean hasAlpha = colorSetting.hasAlpha();
            float[] hsba = new float[(hasAlpha ? 1 : 0) + 3];
            List<?> sliders = parent.getSettings();
            hsba[0] = ((NumberSetting) sliders.get(1)).getValue().floatValue();
            for (int j = 1; j < hsba.length; j++) {
                hsba[j] = ((NumberSetting) sliders.get(j + 1)).getValue().floatValue();
            }
            int rgb = Color.HSBtoRGB(hsba[0], hsba[1], hsba[2]);
            Color color;
            if (hasAlpha) {
                color = new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, (int) hsba[3]);
            } else {
                color = new Color(rgb);
            }
            colorSetting.setValue(color);
        });
    }
}
