package com.adjustclient.script.api;

import com.adjustclient.script.api.BlocksSetting;
import com.adjustclient.script.api.BooleanSetting;
import com.adjustclient.script.api.ButtonSetting;
import com.adjustclient.script.api.ColorSetting;
import com.adjustclient.script.api.ExpandSetting;
import com.adjustclient.script.api.HotbarSetting;
import com.adjustclient.script.api.ItemsSetting;
import com.adjustclient.script.api.KeybindSetting;
import com.adjustclient.script.api.ModeSetting;
import com.adjustclient.script.api.NumberSetting;
import com.adjustclient.script.api.RotationMode;
import com.adjustclient.script.api.RotationSetting;
import com.adjustclient.script.api.StringSetting;

public class Settings {
    public native ExpandSetting expand(String var1);

    public native BooleanSetting bool(String var1, boolean var2);

    public native NumberSetting number(String var1, double var2, double var4, double var6, double var8);

    public native ModeSetting mode(String var1, String var2);

    public native RotationSetting rotation(String var1, RotationMode var2);

    public native StringSetting string(String var1, String var2);

    public native ColorSetting color(String var1, int var2, boolean var3);

    public native KeybindSetting keybind(String var1);

    public native ItemsSetting items(String var1);

    public native BlocksSetting blocks(String var1);

    public native HotbarSetting hotbar(String var1);

    public native ButtonSetting button(String var1, String var2);
}

