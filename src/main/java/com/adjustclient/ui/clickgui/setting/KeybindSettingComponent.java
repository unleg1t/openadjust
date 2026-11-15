package com.adjustclient.ui.clickgui.setting;

import com.adjustclient.bind.Bind;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.setting.KeybindSetting;
import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiScreen;
import com.adjustclient.bind.ScrollKey;
import com.adjustclient.render.animation.TimedFade;
import com.adjustclient.ui.clickgui.ClickGuiScreen;
import com.adjustclient.ui.clickgui.frame.HoverEffect;
import com.adjustclient.ui.clickgui.overlay.BindOverlay;
import com.adjustclient.util.Timer;
import net.minecraft.util.ChatColor;

public final class KeybindSettingComponent extends SettingComponent<KeybindSetting> {

    HoverEffect labelColor;
    String bindText;
    Object obf72;

    public KeybindSettingComponent(KeybindSetting setting) {
        super(setting);
        this.labelColor = new HoverEffect(this, this::hasKeys, HoverEffect.hoveredOf(this), 0.0F, 0.6F);
        this.modifiers.add(this.labelColor);
    }

    boolean hasKeys() {
        Bind bind = (Bind) this.setting.getValue();
        return bind.keys.size() != 0;
    }

    String compose(String bindText) {
        String label = this.getLabel();
        String withColon = label.length() != 0 ? label + ":" : ":";
        float reserved = Fonts.TAHOMA_10.getStringWidth(bindText) + 14.0F;
        String fitted = this.fitLabel(withColon, reserved);
        return fitted + " " + ChatColor.BOLD /* ChatColor control string */ + bindText;
    }

    @Override
    public void renderContent(com.adjustclient.event.Render2DEvent queue, com.adjustclient.util.Vec2f mouse) {
        this.updateBindText();
        String text = this.compose(this.bindText);
        float x = this.bounds.x + 2.0F;
        float y = this.y + 4.0F;
        int color = this.labelColor.color(190);
        Fonts.TAHOMA_10.drawString(text, x, y, color);
    }

    void updateBindText() {
        Bind bind = (Bind) this.setting.getValue();
        if (bind.keys.size() == 0) {
            this.bindText = "None";
            return;
        }
        StringBuilder sb = new StringBuilder();
        Iterator<com.adjustclient.bind.BindKey> it = bind.keys.iterator(); // HashMap / LinkedHashMap key iterator
        while (it.hasNext()) {
            if (Fonts.TAHOMA_10.getStringWidth(sb.toString()) > 30.0F) {
                sb.append("...");
                break;
            }
            com.adjustclient.bind.BindKey key = it.next();
            if (key instanceof ScrollKey) { // scroll key: Scroll Up when the stored direction is 1069
                sb.append(((ScrollKey) key).keyCode() == 1069 ? "Scroll Up" : "Scroll Down");
            } else {
                sb.append(key.getName());
            }
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        this.bindText = sb.toString();
    }

    @Override
    public boolean onMousePressed(com.adjustclient.util.Vec2f mouse, int button, boolean pressed) {
        if (!pressed || button != 0) {
            return false;
        }
        if (GuiScreen.isCtrlKeyDown()) {
            this.openResetPrompt();
            return true;
        }
        Bind bind = (Bind) this.setting.getValue();
        ClickGuiScreen gui = ClickGuiScreen.instance(); // creates + stores the singleton when absent
        String label = java.util.Objects.requireNonNullElse(bind.name, this.getLabel());
        Consumer<Object> onKey = key -> bind.addKey((com.adjustclient.bind.BindKey) key);
        // fade-in 150 ms, pulse 1050 / 500 / 1000 ms, 180 ms debounce timer, the row label and the current keys
        BindOverlay popup = BindOverlay.create(label, onKey, bind.keys);
        gui.addOverlay(popup);                                        // exclusive overlay: only added while no overlay exists
        return false;
    }

    @Override
    public float getHeight() {
        return 9.0F;
    }
}
