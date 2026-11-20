package com.adjustclient.core.settings;

import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.rendering.ColorUtil;
import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class ColorSetting extends Setting<Color> {
    public boolean alpha;
    public boolean disallowMainColor;
    public Mode mode;
    private Mode defaultMode;
    private Object field56;

    public ColorSetting(SettingContainer parent, String name, Color color) {
        this(parent, name, color, null);
    }
    public ColorSetting(SettingContainer parent, String name, Color color, BooleanSupplier hideIf) {
        this(parent, name, color, true, false, Mode.PLAIN, hideIf);
    }
    public ColorSetting(SettingContainer parent, String name, Color color, boolean alpha, boolean disallowMainColor,
                        Mode mode, BooleanSupplier hideIf) {
        super(parent, name, color, hideIf);
        this.alpha = alpha;
        this.disallowMainColor = disallowMainColor;
        this.mode = this.defaultMode = disallowMainColor && mode == Mode.MAIN ? Mode.PLAIN : mode;
    }
    public Mode getMode() { return mode; }
    public boolean hasAlpha() { return alpha; }
    public Mode getDefaultMode() { return defaultMode; }
    public void setMode(Mode mode) { this.mode = disallowMainColor && mode == Mode.MAIN ? Mode.PLAIN : mode; }

    public void read(DataInputStream input) throws IOException {
        Color old = getValue();
        Color next = new Color(input.readInt(), alpha);
        setValueSilently(next);
        notifyListeners(old, next);
        if (input.available() > 0) {
            int hash = input.readInt();
            if (hash == 0) return;
            for (Mode option : Mode.values()) {
                if (ModeSetting.hashName(option.getName()) == hash) {
                    setMode(option);
                    break;
                }
            }
        } else mode = Mode.PLAIN;
        notifyListeners(previousValue, getValue());
    }

    public void write(DataOutputStream output) throws IOException {
        output.writeInt(getValue().getRGB());
        output.writeInt(ModeSetting.hashName(mode.getName()));
    }

    @Override public void reset() {
        super.setValue(getDefaultValue());
        setMode(defaultMode);
    }

    public boolean isChanged() {
        return ((!Objects.equals(getValue(), getDefaultValue())) && !field44) || mode != defaultMode;
    }

    public Color getColor() {
        if (mode == Mode.RAINBOW) {
            Color rainbow = ColorUtil.rainbow(0.0f, 0.45f, 0.8f);
            return alpha ? ColorUtil.withAlpha(rainbow, getValue().getAlpha()) : rainbow;
        }
        if (mode == Mode.MAIN) return HUDModule.getHudColor();
        return getValue();
    }

    public enum Mode implements ModeOption {
        PLAIN("Plain"), RAINBOW("Rainbow"), MAIN("Main Color");
        private final String name;
        private Object field20;
        Mode(String name) { this.name = name; }
        @Override public String getName() { return name; }
    }
}
