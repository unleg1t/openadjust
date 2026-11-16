package com.adjustclient.module.impl.render.blockoverlay;

import com.adjustclient.module.Module;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.setting.*;
import java.awt.Color;
import static com.adjustclient.util.MinecraftInstance.mc;

public final class ObsidianOverlaySettings extends RangedOverlaySettings {
    public final BooleanSetting onlySurroundingBeds;
    public ObsidianOverlaySettings(SettingContainer parent, Color color) {
        super("Obsidian", parent, color, false, () -> {
            if (mc.theWorld != null && mc.renderGlobal != null && parent instanceof Module module && module.isEnabled()) {
                if (mc.isCallingFromMinecraftThread()) mc.renderGlobal.loadRenderers();
                else mc.addScheduledTask(RenderUtil.outlineRefresh(mc));
            }
        });
        onlySurroundingBeds = new BooleanSetting(group, "Only surrounding beds", true);
        onlySurroundingBeds.addListener(() -> {
            if (mc.theWorld != null && mc.renderGlobal != null && parent instanceof Module module && module.isEnabled()) {
                if (mc.isCallingFromMinecraftThread()) mc.renderGlobal.loadRenderers();
                else mc.addScheduledTask(RenderUtil.outlineRefresh(mc));
            }
        });
    }
}
