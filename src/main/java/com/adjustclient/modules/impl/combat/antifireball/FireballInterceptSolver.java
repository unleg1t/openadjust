package com.adjustclient.modules.impl.combat.antifireball;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.utilities.DistanceUtil;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.RotationUtil;
import com.adjustclient.utilities.Vec2f;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.entity.TrackedEntity;
import com.adjustclient.utilities.anticheat.entity.TrackedFireball;
import com.adjustclient.utilities.rotation.AimPoint;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

final class FireballInterceptSolver implements MinecraftInstance {
    final AntiFireballModule module;
    FireballAim target;

    FireballInterceptSolver(AntiFireballModule module) {
        this.module = module;
    }

    void update() {
        target = null;
        if (mc.thePlayer == null) return;
        Vec3 eyes = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        target = findIntercept(eyes, module.settings.getRange());
    }

    FireballAim findIntercept(Vec3 eyes, double range) {
        List<TrackedFireball> candidates = findCandidates(eyes, range);
        if (candidates.isEmpty()) return null;
        candidates.sort(Comparator.comparingDouble(fireball -> {
            EntityFireball entity = fireball.entity;
            return entity.getDistanceSqToEntity(mc.thePlayer);
        }));
        for (TrackedFireball fireball : candidates) {
            FireballAim aim = findEntityIntercept(fireball, eyes, range);
            if (aim != null) return aim;
        }
        return null;
    }

    FireballAim findEntityIntercept(TrackedFireball tracked, Vec3 eyes, double range) {
        AimPoint aim = RotationUtil.getAimPoint(tracked.entity, eyes,
                new Vec2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch), true, true, range);
        return aim == null ? null : new FireballAim(tracked, aim);
    }

    List<TrackedFireball> findCandidates(Vec3 eyes, double range) {
        List<TrackedFireball> result = new ArrayList<>();
        if (ServerTracker.INSTANCE.entityTracker == null) return result;
        List<TrackedEntity<?>> entities = ServerTracker.INSTANCE.entityTracker.getEntities(256, null);
        for (Iterator<TrackedEntity<?>> iterator = entities.iterator(); iterator.hasNext();) {
            TrackedEntity<?> tracked = iterator.next();
            var entity = tracked.getEntity();
            if (tracked.isRemoved()) {
                iterator.remove();
                continue;
            }
            if (!(tracked instanceof TrackedFireball)) continue;
            TrackedFireball fireball = (TrackedFireball) tracked;
            var shooter = fireball.shooter;
            if (shooter != null) {
                if (shooter.isUser()) continue;
                var game = Adjust.gameStateTracker.state;
                if (game instanceof BedWarsGameState) {
                    var team = ((BedWarsGameState) game).findPlayerTeam(fireball.getName());
                    if (team != null && team == ((BedWarsGameState) game)
                            .findPlayerTeam(mc.getSession().getUsername())) continue;
                }
            }
            float border = entity.getCollisionBorderSize();
            AxisAlignedBB expanded = entity.getEntityBoundingBox().expand(border, border, border);
            Vec3 nearest = DistanceUtil.closestPoint(eyes, expanded);
            if (eyes.distanceTo(nearest) > range + border) continue;
            result.add(fireball);
        }
        return result;
    }
}
