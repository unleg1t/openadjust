package com.adjustclient.module.impl.render.blockoverlay;

import com.adjustclient.setting.*;
import com.adjustclient.render.RenderUtil;
import java.awt.Color;
import static com.adjustclient.util.MinecraftInstance.mc;

public final class BedOverlaySettings extends RangedOverlaySettings {
    public final BooleanSetting ignoreLocal;
    public final BooleanSetting ignoreWhitelisted;
    public final BooleanSetting teamColor;
    public final BooleanSetting showMissingBlocks;

    public BedOverlaySettings(BlockOverlayModule module, Color color) {
        super("Bed", module, color, false, null);
        showMissingBlocks = new BooleanSetting(group, "Show missing blocks", true);
        ExpandSetting hypixel = new ExpandSetting(group, "Hypixel");
        Runnable changed = () -> {
            if (mc.theWorld != null && module.isEnabled()) {
                if (mc.isCallingFromMinecraftThread()) mc.renderGlobal.loadRenderers();
                else mc.addScheduledTask(RenderUtil.outlineRefresh(mc));
            }
        };
        ignoreLocal = new BooleanSetting(hypixel, "Ignore local", true);
        ignoreLocal.addListener(changed);
        ignoreWhitelisted = new BooleanSetting(hypixel, "Ignore whitelisted", false);
        ignoreWhitelisted.addListener(changed);
        teamColor = new BooleanSetting(hypixel, "Team color", true);
        teamColor.addListener(changed);
    }
}
