package com.adjustclient.modules.impl.render.tweaks;

import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.MouseWheelEvent;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.utilities.ScreenUtil;
import net.minecraft.client.settings.GameSettings;

public final class ZoomTweak extends Tweak {
    public static ZoomTweak INSTANCE;
    public final BooleanSetting noCinematic;
    public final BooleanSetting scrollZoom;
    public float zoom;

    public ZoomTweak(TweaksModule module) {
        super(module);
        INSTANCE = this;
        zoom = 4.0F;
        ExpandSetting settings = new ExpandSetting(module, "Zoom");
        settings.enabledPredicate = this::hideCondition;
        noCinematic = new BooleanSetting(settings, "No cinematic", true);
        scrollZoom = new BooleanSetting(settings, "Scroll zoom", true);
    }
    private boolean hideCondition() { return noCinematic.isEnabled() || scrollZoom.isEnabled(); }

    @EventHandler public void updateZoom(MouseWheelEvent event) {
        if (!module.isEnabled() || !isZoomActive() || !scrollZoom.isEnabled()
                || ScreenUtil.isScreenOpen() || !GameSettings.isKeyDown(mc.gameSettings.ofKeyBindZoom)) return;
        if (event.wheelDelta > 0) zoom *= 1.25F;
        else if (event.wheelDelta < 0) {
            zoom *= 0.75F;
            mc.renderGlobal.displayListEntitiesDirty = true;
        }
        zoom = Math.max(1.0F, Math.min(600.0F, zoom));
    }
    public void resetZoom() { zoom = 4.0F; }
    @Override public void onDisable() { resetZoom(); }

    public boolean isZoomActive() { return noCinematic.isEnabled() || scrollZoom.isEnabled(); }
    public float getZoom() { return module.isEnabled() && isZoomActive() && scrollZoom.isEnabled() ? zoom : 4.0F; }
    public boolean isZoomKeyDown() {
        return module.isEnabled() && isZoomActive() && scrollZoom.isEnabled()
                && !ScreenUtil.isScreenOpen() && GameSettings.isKeyDown(mc.gameSettings.ofKeyBindZoom);
    }
    @Override protected boolean enabledSetting() { return module.isEnabled() && isZoomActive(); }
}
