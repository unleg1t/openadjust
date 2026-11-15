package com.adjustclient.ui.clickgui.setting.color;

import com.adjustclient.setting.ColorSetting;
import com.adjustclient.ui.clickgui.setting.SettingComponent;
import com.adjustclient.util.Vec2f;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.lwjgl.input.Mouse;
import com.adjustclient.ui.clickgui.ClickGuiComponent;

public final class ColorPicker extends SettingComponent<ColorSetting> {

    private ColorPickerElement[] elements;
    private HueSlider hueSlider;
    private SaturationBrightnessSquare square;
    private ColorPickerElement dragged;
    Color pendingColor;
    private AlphaSlider alphaSlider;

    public ColorPicker(ColorSetting setting) {
        super(setting);
        this.modifiers.clear();
        this.square = new SaturationBrightnessSquare();
        this.hueSlider = new HueSlider();
        List<ColorPickerElement> list = new ArrayList<>();
        list.add(this.square);
        list.add(this.hueSlider);
        if (setting.alpha /* allow alpha */) {
            this.alphaSlider = new AlphaSlider();
            list.add(this.alphaSlider);
        }
        this.elements = list.toArray(new ColorPickerElement[0]);
        for (ColorPickerElement element : this.elements) {
            element.picker = this;
        }
        this.hueSlider.height = 5.0F;                    // the hue and alpha bars are 5 high (the square is 36 high, set when positioned)
        if (this.alphaSlider != null) {
            this.alphaSlider.height = 5.0F;
        }
        Runnable onSettingChanged = () -> {
            if (this.pendingColor != null && this.pendingColor == setting.getColor()) {
                this.pendingColor = null;
                return;
            }
            for (ColorPickerElement element : list) {
                if (!(element instanceof AlphaSlider)) {
                    element.updateFromColor();
                }
            }
        };
        setting.addListener((oldColor, newColor) -> onSettingChanged.run());   // change notification of the colour setting
        onSettingChanged.run();                                                // initial hue / saturation / brightness from the current colour
    }

    ColorSetting getColorSetting() {
        return this.setting;
    }

    HueSlider getHueSlider() {
        return this.hueSlider;
    }

    SaturationBrightnessSquare getSquare() {
        return this.square;
    }

    @Override
    public boolean isVisible(int stamp) {
        if (stamp != this.visibilityStamp) {
            this.visibilityStamp = stamp;
            ClickGuiComponent provider = this.parent;
            this.cachedVisible = provider == null || provider.computeVisible(stamp);
            if (!this.cachedVisible) {
                return false;
            }
        } else if (!this.cachedVisible) {
            return false;
        }
        BooleanSupplier hideIf = this.setting.hideIf;
        if (hideIf != null && hideIf.getAsBoolean()) {
            return false;
        }
        return this.setting.mode == ColorSetting.Mode.PLAIN;
    }

    @Override
    public void renderContent(com.adjustclient.event.Render2DEvent context, Vec2f mouse) {
        if (!Mouse.isButtonDown(0)) {
            this.dragged = null;
        }
        for (ColorPickerElement element : this.elements) {
            ColorPicker p = element.picker;
            if (element instanceof AlphaSlider) {
                element.x = 2.0F + p.x;
                element.y = 2.0F + p.y + p.square.height + 2.0F + 5.0F + 2.0F;
                element.width = p.width - 4.0F;
            } else if (element instanceof SaturationBrightnessSquare) {
                element.x = 2.0F + p.x;
                element.y = 2.0F + p.y;
                element.width = p.width - 4.0F;
                element.height = 36.0F;
            } else if (element instanceof HueSlider) {
                element.x = 2.0F + p.x;
                element.y = 2.0F + p.y + p.square.height + 2.0F;
                element.width = p.width - 4.0F;
            }
            element.render();
            if (element == this.dragged) {
                float x = Math.min(Math.max(mouse.x, element.x), element.x + element.width);
                float y = Math.min(Math.max(mouse.y, element.y), element.y + element.height);
                element.setFromMouse(new Vec2f(x, y));
            }
        }
    }

    /** Left button: starts dragging the part under the mouse. */
    @Override
    public boolean onMousePressed(Vec2f mouse, int button, boolean pressed) {
        if (button == 0) {
            for (ColorPickerElement element : this.elements) {
                if (mouse.x >= element.x && mouse.y >= element.y
                        && mouse.x < element.x + element.width && mouse.y < element.y + element.height) {
                    this.dragged = element;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public float getHeight() {
        return this.setting.alpha ? 54.0F : 47.0F;
    }
}
