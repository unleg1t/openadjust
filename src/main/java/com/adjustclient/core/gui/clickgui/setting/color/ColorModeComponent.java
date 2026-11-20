package com.adjustclient.core.gui.clickgui.setting.color;

import com.adjustclient.core.settings.ColorSetting;
import com.adjustclient.core.settings.ModeSetting;
import com.adjustclient.core.settings.Setting;
import com.adjustclient.modules.impl.combat.killaura.target.priority.TargetPriority;
import com.adjustclient.modules.impl.movement.noslowdown.NoSlowMode;
import com.adjustclient.modules.impl.player.nofall.NoFallMode;
import com.adjustclient.core.gui.clickgui.setting.ModeSettingComponent;

public final class ColorModeComponent extends ModeSettingComponent {

    public boolean syncingMode;

    public static ColorModeComponent create(ColorSettingComponent row, ColorSetting colorSetting) {
        ModeSetting<ColorSetting.Mode> type = new ModeSetting<>(row, "Type", colorSetting.mode,
                colorSetting.disallowMainColor
                        ? new ColorSetting.Mode[]{ColorSetting.Mode.PLAIN, ColorSetting.Mode.RAINBOW}
                        : ColorSetting.Mode.values());
        ColorModeComponent component = new ColorModeComponent(type);
        component.syncingMode = false;

        com.adjustclient.core.settings.SettingChangeListener<java.awt.Color> colorListener = (oldColor, newColor) -> {
            component.syncingMode = true;
            ModeSetting modeSetting = (ModeSetting) component.setting;
            ColorSetting.Mode mode = colorSetting.mode;
            if (mode == null) {
                Object value = modeSetting.getValue();
                if (value instanceof TargetPriority || value instanceof NoFallMode || value instanceof NoSlowMode) {
                    component.syncingMode = false;
                    return;
                }
                String name = modeSetting.name != null ? modeSetting.name : "null";
                throw new RuntimeException("?! x1: " + name);
            }
            modeSetting.setValue(mode);
            component.syncingMode = false;
        };
        colorSetting.addListener(colorListener);

        Runnable onTypeChanged = () -> {
            if (component.syncingMode) {
                return; // change came from colorListener
            }
            Object picked = type.getValue();
            if (!(picked instanceof ColorSetting.Mode)) {
                throw new ClassCastException();
            }
            ColorSetting.Mode mode = (ColorSetting.Mode) picked;
            if (colorSetting.disallowMainColor && mode == ColorSetting.Mode.MAIN) {
                mode = ColorSetting.Mode.PLAIN;
            }
            colorSetting.mode = mode;
            if (colorSetting.listeners != null) {
                for (com.adjustclient.core.settings.SettingChangeListener<java.awt.Color> listener : colorSetting.listeners) {
                    if (listener == colorListener) {
                        continue;
                    }
                    try {
                        listener.onChange(colorSetting.getColor(), colorSetting.getColor());
                    } catch (Throwable t) {
                        t.printStackTrace();
                        com.adjustclient.core.logging.Logger.log(com.adjustclient.core.logging.LogLevel.ERROR, true,
                                "Error during property update of '{}': {}", new Object[] {colorSetting.name, t});
                    }
                }
            }
        };
        type.addListener(onTypeChanged);
        return component;
    }

    private ColorModeComponent(ModeSetting<ColorSetting.Mode> type) {
        super(type);
    }
}
