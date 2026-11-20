package com.adjustclient.core.events;

import com.adjustclient.modules.impl.player.AntiDebuffModule;

public final class RenderEffectEvent extends CancellableEvent {
    public OverlayType phase;

    public RenderEffectEvent(OverlayType phase) {
        this.phase = phase;
    }

    public static boolean shouldRender(OverlayType phase) {
        AntiDebuffModule module = AntiDebuffModule.INSTANCE;
        if (module == null || !module.isEnabled()) return true;
        if (module.pumpkinOverlay.isEnabled() && phase == OverlayType.PUMPKIN) return false;
        if (module.nauseaEffect.isEnabled() && phase == OverlayType.NAUSEA) return false;
        if (module.portalOverlay.isEnabled() && phase == OverlayType.PORTAL) return false;
        return !module.blindnessEffect.isEnabled() || phase != OverlayType.BLINDNESS;
    }
}
