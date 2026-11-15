package com.adjustclient.module.util;

import com.adjustclient.Adjust;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.setting.SettingContainer;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.Timer;
import net.minecraft.client.Minecraft;

public final class CpsSettings {
    public final SettingContainer parent;
    public final NumberSetting min;
    public final NumberSetting max;
    public final Timer timer = new Timer();
    public int clickDelay = -1;
    public int rerollJitter;
    private boolean deferred;
    private Object field32;

    public CpsSettings(SettingContainer parent, int minimum, int maximum) {
        this.parent = parent;
        min = new NumberSetting(parent, "CPS (min)", minimum, 1, 20, 1);
        max = new NumberSetting(parent, "CPS (max)", maximum, 1, 20, 1);
        Runnable[] retry = new Runnable[1];
        retry[0] = () -> {
            if (!Adjust.INSTANCE.fileSystem.loading) {
                if (min.getInt() > max.getInt()) min.setValue(max.getValue());
                if (max.getInt() < min.getInt()) max.setValue(min.getValue());
                clickDelay = -1;
            } else {
                if (deferred) { deferred = false; return; }
                deferred = true;
                Minecraft.getMinecraft().addScheduledTask(retry[0]);
            }
            deferred = false;
        };
        min.addListener(() -> {
            if (!Adjust.INSTANCE.fileSystem.loading) {
                if (min.getInt() > max.getInt()) min.setValue(max.getValue());
                if (max.getInt() < min.getInt()) max.setValue(min.getValue());
                clickDelay = -1;
            } else {
                if (deferred) return;
                deferred = true;
                Minecraft.getMinecraft().addScheduledTask(retry[0]);
            }
        });
        max.addListener(() -> {
            if (!Adjust.INSTANCE.fileSystem.loading) {
                if (min.getInt() > max.getInt()) min.setValue(max.getValue());
                if (max.getInt() < min.getInt()) max.setValue(min.getValue());
                clickDelay = -1;
            } else {
                if (deferred) return;
                deferred = true;
                Minecraft.getMinecraft().addScheduledTask(retry[0]);
            }
        });
    }

    public int getClickDelay(boolean allowRoll) {
        if (clickDelay == -1) { rollClickDelay(); return clickDelay; }
        int current = clickDelay;
        if (allowRoll && timer.hasElapsed(rerollJitter + 43)) {
            clickDelay = (int) RandomUtil.nextGaussian(min.getDouble(), max.getDouble());
            rerollJitter = RandomUtil.nextInt(23);
        }
        return current;
    }

    public void rollClickDelay() {
        clickDelay = (int) RandomUtil.nextGaussian(min.getDouble(), max.getDouble());
        rerollJitter = RandomUtil.nextInt(23);
    }
}
