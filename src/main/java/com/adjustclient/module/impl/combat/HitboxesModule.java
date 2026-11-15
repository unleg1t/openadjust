package com.adjustclient.module.impl.combat;

import com.adjustclient.anticheat.feature.Feature;
import com.adjustclient.event.EntityCollisionBorderEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.*;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.entity.TrackedLivingEntity;
import net.minecraft.entity.Entity;
import com.adjustclient.module.impl.player.AntiCheatModule;

public class HitboxesModule extends Module {
    public static HitboxesModule INSTANCE;
    public final NumberSetting size;
    public final ExpandSetting targets;
    public final BooleanSetting players;
    public final BooleanSetting teammates;
    public final BooleanSetting invisibles;
    public HitboxesModule() {
        super("Hitboxes", Category.COMBAT);
        INSTANCE = this;
        size = new NumberSetting(this, "Size", 0.3, 0.1, 1.0, 0.05);
        targets = new ExpandSetting(this, "Targets");
        players = new BooleanSetting(targets, "Players", true);
        teammates = new BooleanSetting(targets, "Teammates", false);
        invisibles = new BooleanSetting(targets, "Invisibles", true);
    }
    @EventHandler public void onEntityRemove(EntityCollisionBorderEvent event) {
        if (!isEnabled()) return;
        if (AntiCheatModule.restrictionsEnabled && ServerTracker.INSTANCE.serverState != null) {
            var blocker = ServerTracker.INSTANCE.serverState.featureBlocker;
            for (var antiCheat : blocker.antiCheats.values()) {
                if (!antiCheat.isAllowed(com.adjustclient.anticheat.feature.Features.HITBOXES)) {
                    blocker.reportBlocked(antiCheat, com.adjustclient.anticheat.feature.Features.HITBOXES);
                    return;
                }
            }
        }
        if (isTarget(event.entity)) event.border = size.getFloat();
    }
    public boolean isTarget(Entity entity) {
        int mask = 0x400 | (players.isEnabled() ? 2 : 0) | (teammates.isEnabled() ? 0x20 : 0) | (invisibles.isEnabled() ? 0x40 : 0);
        for (TrackedLivingEntity tracked : ServerTracker.INSTANCE.entityTracker.getEntities(mask, TrackedLivingEntity.class)) {
            if (tracked.getEntity() == entity) return true;
        }
        return false;
    }
}
