package com.adjustclient.core.gui.clickgui.overlay;

import net.minecraft.client.gui.ScaledResolution;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.gui.clickgui.ClickGuiScreen;
import com.adjustclient.core.gui.clickgui.Modifier;
import com.adjustclient.core.bind.KeyboardKey;
import com.adjustclient.core.bind.MouseKey;
import com.adjustclient.core.bind.ScrollKey;
import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.animation.TimedFade;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.Vec2f;
import java.util.function.Consumer;
import net.minecraft.util.ChatColor;

public class BindOverlay extends Modifier {
    private Animation fade;
    private TimedFade pulse;
    private Consumer<Object> callback;
    private String name;
    private Timer debounce;
    public String pressedText;
    private Object pressedKeys;

    public BindOverlay(Animation fade, TimedFade pulse, Consumer<Object> callback, String name, Timer debounce) {
        this.fade = fade;
        this.pulse = pulse;
        this.callback = callback;
        this.name = name;
        this.debounce = debounce;
    }

    public static BindOverlay create(String name, Consumer<Object> callback, java.util.Set<?> keys) {
        BindOverlay overlay = new BindOverlay(new Animation(Easing.LINEAR, 150L), new TimedFade(1050L, 500L, 1000L), callback, name, new Timer());
        overlay.pressedText = formatKeys(keys);
        return overlay;
    }

    @Override
    public boolean canStack() {
        return false;
    }

    @Override
    public boolean onScroll(float direction) {
        Object key = new ScrollKey(direction < 0.0F ? 1069 : 1070);
        addKey(key);
        return true;
    }

    /** Gives a captured key to the callback (at most once per 180 ms) and removes this overlay from the screen; the null key (ESC) only removes the overlay. */
    public void addKey(Object key) {
        if (key != null) {
            if (debounce.lastMs + 180L > System.currentTimeMillis()) {
                return;                                                   // still inside the 180 ms grace period
            }
            callback.accept(key);                                         // includes the bind-capturing Consumer
            com.adjustclient.Adjust.INSTANCE.fileSystem.markDirty();
        }
        ClickGuiScreen.instance().removeOverlay(this);
    }

    /** Joins the names of a set of bind keys with ", " ("Scroll Down", "Scroll Up", "Key <code>", Keyboard names ...). */
    public static String formatKeys(java.util.Set<?> keys) {
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        java.util.Iterator<?> it = keys.iterator();
        while (it.hasNext()) {
            Object k = it.next();
            String name;
            if (k instanceof ScrollKey) {          // scroll key
                name = ((ScrollKey) k).keyCode() == 1069 ? "Scroll Up" : "Scroll Down";
            } else if (k instanceof KeyboardKey) {
                name = ((KeyboardKey) k).getName(); // KeyboardKey.getName owns the key-name lookup and exception fallback.
            } else {
                name = ((com.adjustclient.core.bind.BindKey) k).getName();
            }
            sb.append(name);
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean keyTyped(int typedChar, int keyCode) {
        if (keyCode == -1) {
            return false;
        }
        Object key;
        if (keyCode == org.lwjgl.input.Keyboard.KEY_ESCAPE) {
            key = null;
        } else {
            key = new KeyboardKey(keyCode); // keyboard bind key
        }
        addKey(key);
        return true;
    }

    @Override
    public boolean mouseClicked(Vec2f mouse, int button, boolean pressed) {
        if (!pressed) {
            return false;
        }
        addKey(new MouseKey(button)); // mouse bind key
        return true;
    }

    @Override
    public void render(ScaledResolution window, Render2DEvent queue, Object unused, Vec2f mouse, boolean firstPass) {
        fade.update(1.0F);
        float w = (float) window.getScaledWidth();
        float h = (float) window.getScaledHeight();
        // dark full screen layer: colour (21, 21, 21), alpha = (int) (220 * fadeValue)
        int bg = ((int) (220.0F * fade.value) << 24) | 0x151515;
        NvgDraw.fillRect(bg, 0.0F, 0.0F, w, h);
        String text = "Press any key to bind " + ChatColor.WHITE + name;
        float textFade = pulse.b();
        int textColor = (((int) (textFade * 60.0F) + 170) << 24) | 0x969696; // (150, 150, 150, 170..230)
        // centred at x = w / 2, y = h / 2.17
        Fonts.TAHOMA_20.drawString(text, w / 2.0F - Fonts.TAHOMA_20.getStringWidth(text) * 0.5F, h / 2.17F, textColor);
        if (pressedText != null) {
            Fonts.TAHOMA_20.drawCenteredString(pressedText, w / 2.0F, h / 2.17F + 12.0F, 0xC8E9E9E9);
        }
        // the pulse restarts once its fall has finished (timer and both animations are dropped)
        if (pulse.fadeOut != null && pulse.fadeOut.finished) {
            pulse.fadeOut = null;
            pulse.fadeIn = null;
            pulse.timer = null;
        }
    }
}
