package com.adjustclient.modules.impl.render.blockoverlay;

import com.adjustclient.modules.Module;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.settings.*;
import java.awt.Color;
import static com.adjustclient.utilities.MinecraftInstance.mc;

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
