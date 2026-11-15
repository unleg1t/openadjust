package com.adjustclient.module.impl.render.chams;

import com.adjustclient.event.EntityRenderEligibilityEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.entity.TrackedEntity;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.Entity;

public final class ChamsModule extends Module {
    public static ChamsModule INSTANCE;
    public final ChamsTargetSettings local;
    public final ChamsTargetSettings players;
    public final ChamsTargetSettings friendly;
    public final ChamsTargetSettings enemy;
    public final ChamsTargetSettings hostile;
    public final ChamsTargetSettings passive;
    public final Map<Entity, ChamsTargetSettings> overrides = new HashMap<>();

    public ChamsModule() {
        super("Chams", Category.RENDER);
        INSTANCE = this;
        local = new ChamsTargetSettings(this, "Local", true, new Color(0x90b053));
        players = new ChamsTargetSettings(this, "Players", true, new Color(0x435ad1));
        friendly = new ChamsTargetSettings(this, "Friendly", true, new Color(0x5f80ff));
        enemy = new ChamsTargetSettings(this, "Enemy", true, new Color(0x2e3e92));
        hostile = new ChamsTargetSettings(this, "Hostile", false, new Color(0xffff5757, true));
        passive = new ChamsTargetSettings(this, "Passive", false, new Color(0xffffc766, true));
    }

    @com.adjustclient.event.EventHandler
    public void onUpdate(com.adjustclient.event.CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        updateOverrides();
    }

    public void updateOverrides() {
        overrides.clear();
        int mask = 0;
        if (players.enabled.isEnabled()) mask |= 2;
        if (hostile.enabled.isEnabled()) mask |= 4;
        if (passive.enabled.isEnabled()) mask |= 8;
        if (friendly.enabled.isEnabled()) mask |= 0x20;
        if (enemy.enabled.isEnabled()) mask |= 0x800;
        for (TrackedEntity<?> tracked : ServerTracker.INSTANCE.entityTracker.getEntities(mask | 0x80, null)) {
            ChamsTargetSettings target;
            if (tracked.getEntity().isUser()) {
                target = local;
            } else if (tracked.matchesType(0x800)) {
                target = enemy;
            } else if (tracked.matchesType(2)) {
                target = players;
            } else if (tracked.matchesType(0x20)) {
                target = friendly;
            } else if (tracked.matchesType(4)) {
                target = hostile;
            } else if (tracked.matchesType(8)) {
                target = passive;
            } else {
                continue;
            }
            overrides.put(tracked.getEntity(), target);
        }
    }
    @com.adjustclient.event.EventHandler
    public void getOverride(EntityRenderEligibilityEvent event) {
        if (!isEnabled()) return;
        ChamsTargetSettings settings = overrides.get(event.entity);
        if (settings != null && settings.throughWallsEnabled.isEnabled()) {
            event.forceRender = true;
        }
    }
    @Override public void onEnable() { overrides.clear(); }
    @Override public void onDisable() { overrides.clear(); }
}
