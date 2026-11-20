package com.adjustclient.core.gui.tabgui.component;

import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.core.gui.tabgui.NodeFactory;

public enum ColorChannel {

    HUE((parent, channel, hsba) -> {
        NumberSetting setting = new NumberSetting(parent, "Hue",
                Float.valueOf(hsba[0]), Float.valueOf(0.0F), Float.valueOf(1.0F), Float.valueOf(0.01F));
        setting.wrap = true;
        String label = setting.getLabel();
        return new ColorSliderComponent(parent, channel, label, setting);
    }),

    SATURATION((parent, channel, hsba) -> {
        NumberSetting setting = new NumberSetting(parent, "Saturation",
                Float.valueOf(hsba[1]), Float.valueOf(0.0F), Float.valueOf(1.0F), Float.valueOf(0.01F));
        setting.wrap = true;
        String label = setting.getLabel();
        return new ColorSliderComponent(parent, channel, label, setting);
    }),

    BRIGHTNESS((parent, channel, hsba) -> {
        NumberSetting setting = new NumberSetting(parent, "Brightness",
                Float.valueOf(hsba[2]), Float.valueOf(0.0F), Float.valueOf(1.0F), Float.valueOf(0.01F));
        setting.wrap = true;
        String label = setting.getLabel();
        return new ColorSliderComponent(parent, channel, label, setting);
    }),

    ALPHA((parent, channel, hsba) -> {
        // the alpha channel uses Integer limits (0..255, step 1) while its value is the Float alpha from hsba[3]
        NumberSetting setting = new NumberSetting(parent, "Alpha",
                Float.valueOf(hsba[3]), Integer.valueOf(0), Integer.valueOf(255), Integer.valueOf(1));
        setting.wrap = true;
        String label = setting.getLabel();
        return new ColorSliderComponent(parent, channel, label, setting);
    });

    final NodeFactory factory;

    ColorChannel(NodeFactory factory) {
        this.factory = factory;
    }
}
