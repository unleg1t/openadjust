package net.minecraft.client.settings;

import com.adjustclient.Adjust;
import com.adjustclient.event.EventBus;
import com.adjustclient.event.KeyBindingStateEvent;
import com.adjustclient.module.util.MouseClick;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IntHashMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyBinding implements Comparable<KeyBinding>
{
    private static final List<KeyBinding> keybindArray = Lists.<KeyBinding>newArrayList();
    private static final IntHashMap<KeyBinding> hash = new IntHashMap();
    private static final Set<String> keybindSet = Sets.<String>newHashSet();
    private final String keyDescription;
    private final int keyCodeDefault;
    private final String keyCategory;
    private int keyCode;
    public boolean pressed;
    public int pressTime;

    public static void onTick(int keyCode)
    {
        if (keyCode != 0)
        {
            KeyBinding keybinding = (KeyBinding)hash.lookup(keyCode);

            if (keybinding != null)
            {
                Minecraft mc = Minecraft.getMinecraft();

                if (mc != null && mc.timer != null && mc.timer.timerSpeed < 1.0F && keybinding.pressTime >= 1)
                {
                    return;
                }

                KeyBindingStateEvent event = EventBus.post(new KeyBindingStateEvent(keybinding, keybinding.pressed, true));

                if (!event.isCancelled())
                {
                    ++keybinding.pressTime;
                }
            }
        }
    }

    public static void setKeyBindState(int keyCode, boolean pressed)
    {
        if (keyCode != 0)
        {
            KeyBinding keybinding = (KeyBinding)hash.lookup(keyCode);

            if (keybinding != null)
            {
                KeyBindingStateEvent event = EventBus.post(new KeyBindingStateEvent(keybinding, pressed, false));

                if (!event.isCancelled())
                {
                    keybinding.pressed = event.pressed;
                }
            }
        }
    }

    public static void unPressAllKeys()
    {
        if (Adjust.mouseClickHelper != null)
        {
            resetMouseClick(Adjust.mouseClickHelper.left);
            resetMouseClick(Adjust.mouseClickHelper.right);
            resetMouseClick(Adjust.mouseClickHelper.middle);
            resetMouseClick(Adjust.mouseClickHelper.field4ClickOverride);
        }

        for (KeyBinding keybinding : keybindArray)
        {
            keybinding.unpressKey();
        }
    }

    private static void resetMouseClick(MouseClick wrapper)
    {
        if (wrapper != null)
        {
            wrapper.clicks = null;
            wrapper.down = Boolean.FALSE;
            wrapper.holdTicks = 0;

            if (wrapper.key != null)
            {
                wrapper.key.unpressKey();
            }
        }
    }

    public static void resetKeyBindingArrayAndHash()
    {
        hash.clearMap();

        for (KeyBinding keybinding : keybindArray)
        {
            hash.addKey(keybinding.keyCode, keybinding);
        }
    }

    public static Set<String> getKeybinds()
    {
        return keybindSet;
    }

    public KeyBinding(String description, int keyCode, String category)
    {
        this.keyDescription = description;
        this.keyCode = keyCode;
        this.keyCodeDefault = keyCode;
        this.keyCategory = category;
        keybindArray.add(this);
        hash.addKey(keyCode, this);
        keybindSet.add(category);
    }

    public boolean isKeyDown()
    {
        return this.pressed;
    }

    public String getKeyCategory()
    {
        return this.keyCategory;
    }

    public boolean isPressed()
    {
        if (this.pressTime == 0)
        {
            return false;
        }
        else
        {
            --this.pressTime;

            if (this.pressTime == 0 && Adjust.inventoryStateTracker != null)
            {
                Adjust.inventoryStateTracker.suppressedKeys.remove(this.keyCode);
            }

            return true;
        }
    }

    private void unpressKey()
    {
        this.pressTime = 0;
        this.pressed = false;
    }

    public String getKeyDescription()
    {
        return this.keyDescription;
    }

    public int getKeyCodeDefault()
    {
        return this.keyCodeDefault;
    }

    public int getKeyCode()
    {
        return this.keyCode;
    }

    public void setKeyCode(int keyCode)
    {
        this.keyCode = keyCode;
    }

    public int compareTo(KeyBinding p_compareTo_1_)
    {
        int i = I18n.format(this.keyCategory, new Object[0]).compareTo(I18n.format(p_compareTo_1_.keyCategory, new Object[0]));

        if (i == 0)
        {
            i = I18n.format(this.keyDescription, new Object[0]).compareTo(I18n.format(p_compareTo_1_.keyDescription, new Object[0]));
        }

        return i;
    }
}
