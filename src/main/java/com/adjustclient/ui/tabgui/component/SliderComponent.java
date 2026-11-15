package com.adjustclient.ui.tabgui.component;

import com.adjustclient.render.NvgRenderer;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.setting.NumberSetting;
import java.awt.Color;
import org.lwjgl.input.Keyboard;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.ui.tabgui.KeyEventHandler;
import com.adjustclient.ui.tabgui.KeyPressHandler;
import com.adjustclient.ui.tabgui.SettingNode;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.ui.tabgui.TabGui;

public class SliderComponent extends SettingNode<NumberSetting> implements KeyPressHandler, KeyEventHandler {

    Animation fillAnimation;
    float lastFill = -1.0F;

    public SliderComponent(String label, NumberSetting setting) {
        super(label, setting);
    }

    @Override
    public boolean keyPressed(int key) {
        NumberSetting setting = this.setting;
        if (key == Keyboard.KEY_HOME) {
            setting.setValue(setting.getMin());
            return true;
        }
        if (key == Keyboard.KEY_END) {
            setting.setValue(setting.getMax());
            return true;
        }
        if (key == Keyboard.KEY_DOWN) {
            setting.decrement();
            return true;
        }
        if (key == Keyboard.KEY_UP || key == Keyboard.KEY_RIGHT) {
            setting.increment();
            return true;
        }
        return key == Keyboard.KEY_LSHIFT || key == Keyboard.KEY_RSHIFT;
    }

    @Override
    public void keyEvent(Keyboard.KeyEvent event) {
        boolean pressed = event.state;
        int key = event.key;
        if (key == Keyboard.KEY_RIGHT) {
            Keyboard.enableRepeatEvents(pressed);
        } else if (key == Keyboard.KEY_DOWN || key == Keyboard.KEY_UP) {
            Keyboard.enableRepeatEvents(pressed);
        } else if (key == Keyboard.KEY_LEFT && pressed) {
            Keyboard.enableRepeatEvents(false);
        }
    }

    @Override
    public void render(NvgRenderer dl, Object context, boolean hovered, float x, float y, float width) {
        NumberSetting setting = this.setting;
        float trackWidth = width - 3.0F;
        // filled fraction = (value - min) / (max - min); every Number is converted through Double/Integer/Float -> float
        float value = setting.getValue().floatValue();
        float min = setting.getMin().floatValue();
        float max = setting.getMax().floatValue();
        float fraction = (value - min) / (max - min);
        float fill = trackWidth * fraction;

        float shownFill;
        if (TabGui.layingOutNodes) {
            shownFill = fill;
        } else if (this.fillAnimation == null) {
            if (this.lastFill != -1.0F && this.lastFill != fill) {
                this.fillAnimation = new Animation(Easing.EASE_OUT_SINE, 110L);
                this.fillAnimation.value = fill;
                this.fillAnimation.update(fill);
                shownFill = this.fillAnimation.value;
            } else {
                shownFill = fill;
            }
        } else {
            this.fillAnimation.update(fill);
            shownFill = this.fillAnimation.value;
        }
        this.lastFill = shownFill;

        // track
        dl.color(new Color(47, 47, 49, 255).getRGB());
        dl.rect(x + 1.5F, y + 6.5F, trackWidth, 0.5F);

        Color accent = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        dl.color(accent.getRGB());
        dl.rect(x + 1.5F, y + 6.5F, shownFill, 0.5F);

        // value text over the slider
        dl.color(this.getTextColor(hovered, false));
        String valueText;
        if (setting.getFormatter() != null) {
            valueText = setting.getFormatter().transform(setting);
        } else {
            valueText = NumberSetting.FORMAT.format(((Number) setting.getValue()).doubleValue());
        }
        dl.text(Fonts.TAHOMA_10, valueText, x + 2.0F, y + 2.5F);
    }
}
