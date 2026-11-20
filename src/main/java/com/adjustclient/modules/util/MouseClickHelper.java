package com.adjustclient.modules.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public final class MouseClickHelper {
    public final Minecraft minecraft;
    public final SwingMouseClick left;
    public final SwingMouseClick right;
    public final MouseClick middle;
    public final Map<KeyBinding, MouseClick> buttons;
    private Object field20;
    public MouseClick field4ClickOverride;

    public MouseClickHelper(Minecraft minecraft) {
        this.minecraft = minecraft;
        left = new SwingMouseClick(minecraft.gameSettings.keyBindAttack);
        right = new SwingMouseClick(minecraft.gameSettings.keyBindUseItem);
        middle = new MouseClick(minecraft.gameSettings.keyBindPickBlock);
        this.field4ClickOverride = this.middle;
        HashMap<KeyBinding, MouseClick> map = new HashMap<>();
        if (minecraft.gameSettings.keyBindAttack != null) map.put(minecraft.gameSettings.keyBindAttack, left);
        if (minecraft.gameSettings.keyBindUseItem != null) map.put(minecraft.gameSettings.keyBindUseItem, right);
        if (minecraft.gameSettings.keyBindPickBlock != null) map.put(minecraft.gameSettings.keyBindPickBlock, middle);
        buttons = map;
    }
}
