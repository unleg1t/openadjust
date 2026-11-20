package com.adjustclient.modules.impl.render.bedplates;

import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.ModeSetting;
import com.adjustclient.core.settings.NumberSetting;

public final class BedPlatesSettings {
    public final ModeSetting<BedPlatesMode> mode;
    public final NumberSetting alpha;
    public final BooleanSetting ignoreLocal;
    public final BooleanSetting ignoreWhitelisted;
    public BedPlatesSettings(BedPlatesModule module) {
        mode = new ModeSetting<>(module, "Mode", BedPlatesMode.BLOCKS, BedPlatesMode.values());
        mode.addListener((oldValue, newValue) -> {
            BedTracker tracker = module.tracker;
            if (tracker == null) return;
            synchronized (tracker.beds) {
                if (newValue == BedPlatesMode.BLOCKS) {
                    tracker.beds.values().forEach(plate -> plate.rebuildBlocks());
                } else {
                    tracker.beds.values().forEach(plate -> plate.blocks.clear());
                }
            }
        });
        alpha = new NumberSetting(module, "Alpha", 85, 70, 100, 1).formatter(NumberSetting.INTEGER_SUFFIX_B);
        ExpandSetting hypixel = new ExpandSetting(module, "Hypixel");
        ignoreLocal = new BooleanSetting(hypixel, "Ignore local", true, "On", "Off");
        Runnable refreshOutlines = () -> {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            if (mc.theWorld != null && mc.renderGlobal != null && module.isEnabled()) {
                if (mc.isCallingFromMinecraftThread()) mc.renderGlobal.loadRenderers();
                else mc.addScheduledTask(com.adjustclient.core.rendering.RenderUtil.outlineRefresh(mc));
            }
        };
        ignoreLocal.addListener(refreshOutlines);
        ignoreWhitelisted = new BooleanSetting(hypixel, "Ignore whitelisted", false);
        ignoreWhitelisted.addListener(refreshOutlines);
    }
    public int getAlpha() { return (int) (alpha.getValue().floatValue() / 100.0F * 255.0F); }
}
