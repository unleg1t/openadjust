package com.adjustclient.module.impl.player.ghosthand;

import com.adjustclient.event.EventHandler;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.entity.TrackedEntity;
import com.adjustclient.util.anticheat.entity.TrackedLivingEntity;
import net.minecraft.entity.Entity;
import java.util.*;
import java.util.function.Predicate;

public final class GhostHandModule extends Module {
    public static GhostHandModule INSTANCE;
    public final GhostHandSettings settings;
    public final Map<Entity, EntityFadeState> fades = new HashMap<>();

    public GhostHandModule() {
        super("Ghost Hand", Category.PLAYER);
        INSTANCE = this;
        settings = new GhostHandSettings(this);
    }

    @EventHandler public void updateFades(com.adjustclient.event.CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        int mask = settings.getTargetMask();
        List<TrackedLivingEntity> entities = ServerTracker.INSTANCE.entityTracker.getEntities(mask, TrackedLivingEntity.class);
        entities.removeIf(
                tracked -> mc.thePlayer.getDistanceToEntity(tracked.getEntity()) > 10.0F);
        Set<Entity> present = new HashSet<>();
        for (TrackedLivingEntity tracked : entities) {
            Entity entity = tracked.getEntity();
            present.add(entity);
            EntityFadeState fade = fades.get(entity);
            if (fade == null) fades.put(entity, new EntityFadeState());
            else fade.retiring = false;
        }
        for (Iterator<Map.Entry<Entity, EntityFadeState>> iterator = fades.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<Entity, EntityFadeState> entry = iterator.next();
            if (present.contains(entry.getKey())) continue;
            EntityFadeState fade = entry.getValue();
            fade.retiring = true;
            fade.target.update(1.0F);
            fade.armor.update(1.0F);
            if (fade.target.finished && fade.target.value >= 1.0F) iterator.remove();
        }
    }

    @EventHandler public void onBlockRaytrace(com.adjustclient.event.BlockRaytraceEvent event) {
        if (!isEnabled()) return;
        Entity camera = event.viewEntity;
        float partialTicks = event.partialTicks;
        double reach = event.reach;
        net.minecraft.util.Vec3 start = camera.getPositionEyes(partialTicks);
        net.minecraft.util.Vec3 look = camera.getLook(partialTicks);
        net.minecraft.util.Vec3 end = start.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
        java.util.function.Function<net.minecraft.block.Block, Boolean> selected =
                block -> settings.blocks.contains(block);
        int endX = net.minecraft.util.MathHelper.floor_double(end.xCoord);
        int endY = net.minecraft.util.MathHelper.floor_double(end.yCoord);
        int endZ = net.minecraft.util.MathHelper.floor_double(end.zCoord);
        int x = net.minecraft.util.MathHelper.floor_double(start.xCoord);
        int y = net.minecraft.util.MathHelper.floor_double(start.yCoord);
        int z = net.minecraft.util.MathHelper.floor_double(start.zCoord);
        net.minecraft.util.BlockPos position = new net.minecraft.util.BlockPos(x, y, z);
        net.minecraft.block.state.IBlockState state = camera.worldObj.getBlockState(position);
        net.minecraft.block.Block block = state.getBlock();
        net.minecraft.util.MovingObjectPosition hit = null;
        if (selected.apply(block) && block.canCollideCheck(state, false))
            hit = block.collisionRayTrace(camera.worldObj, position, start, end);
        net.minecraft.util.MovingObjectPosition lastMiss = null;
        int remaining = 200;
        while (hit == null && remaining-- >= 0) {
            if (x == endX && y == endY && z == endZ) break;
            double nextX = 999.0, nextY = 999.0, nextZ = 999.0;
            double tx = 999.0, ty = 999.0, tz = 999.0;
            if (endX > x) nextX = x + 1.0; else if (endX < x) nextX = x;
            if (endY > y) nextY = y + 1.0; else if (endY < y) nextY = y;
            if (endZ > z) nextZ = z + 1.0; else if (endZ < z) nextZ = z;
            double dx = end.xCoord - start.xCoord;
            double dy = end.yCoord - start.yCoord;
            double dz = end.zCoord - start.zCoord;
            if (endX != x) tx = (nextX - start.xCoord) / dx;
            if (endY != y) ty = (nextY - start.yCoord) / dy;
            if (endZ != z) tz = (nextZ - start.zCoord) / dz;
            if (tx == -0.0) tx = -0.0001;
            if (ty == -0.0) ty = -0.0001;
            if (tz == -0.0) tz = -0.0001;
            net.minecraft.util.EnumFacing face;
            if (tx < ty && tx < tz) {
                face = endX > x ? net.minecraft.util.EnumFacing.WEST : net.minecraft.util.EnumFacing.EAST;
                start = new net.minecraft.util.Vec3(nextX, start.yCoord + dy * tx, start.zCoord + dz * tx);
            } else if (ty < tz) {
                face = endY > y ? net.minecraft.util.EnumFacing.DOWN : net.minecraft.util.EnumFacing.UP;
                start = new net.minecraft.util.Vec3(start.xCoord + dx * ty, nextY, start.zCoord + dz * ty);
            } else {
                face = endZ > z ? net.minecraft.util.EnumFacing.NORTH : net.minecraft.util.EnumFacing.SOUTH;
                start = new net.minecraft.util.Vec3(start.xCoord + dx * tz, start.yCoord + dy * tz, nextZ);
            }
            x = net.minecraft.util.MathHelper.floor_double(start.xCoord) - (face == net.minecraft.util.EnumFacing.EAST ? 1 : 0);
            y = net.minecraft.util.MathHelper.floor_double(start.yCoord) - (face == net.minecraft.util.EnumFacing.UP ? 1 : 0);
            z = net.minecraft.util.MathHelper.floor_double(start.zCoord) - (face == net.minecraft.util.EnumFacing.SOUTH ? 1 : 0);
            position = new net.minecraft.util.BlockPos(x, y, z);
            state = camera.worldObj.getBlockState(position);
            block = state.getBlock();
            if (!selected.apply(block)) continue;
            if (block.canCollideCheck(state, false)) hit = block.collisionRayTrace(camera.worldObj, position, start, end);
            else lastMiss = new net.minecraft.util.MovingObjectPosition(
                    net.minecraft.util.MovingObjectPosition.MovingObjectType.MISS, start, face, position);
        }
        mc.objectMouseOver = hit == null ? lastMiss : hit;
        if (mc.objectMouseOver != null || !settings.fallbackToDefault.isEnabled()) event.cancelled = true;
    }

    @EventHandler public void filterEntities(com.adjustclient.event.MouseOverEntitiesEvent event) {
        if (!isEnabled()) return;
        List<Entity> entities = event.entities;
        if (ServerTracker.INSTANCE.entityTracker == null) return;
        int mask = settings.getTargetMask();
        for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext();) {
            Entity entity = iterator.next();
            TrackedEntity<?> tracked = ServerTracker.INSTANCE.entityTracker.get(entity);
            if (!(tracked instanceof TrackedLivingEntity)) continue;
            if (settings.includeTeammates() && tracked.matchesType(32) || tracked.matchesType(mask)) iterator.remove();
        }
    }

    @EventHandler public void onEntityOpacity(com.adjustclient.event.EntityOpacityEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;
        EntityFadeState fade = fades.get(event.entity);
        if (fade == null) return;
        event.bodyAlpha = fade.target.update(fade.retiring ? 1.0F : settings.getTargetOpacity() / 100.0F);
        event.armorAlpha = fade.armor.update(fade.retiring ? 1.0F : settings.getArmorOpacity() / 100.0F);
    }

    @Override public void onDisable() { fades.clear(); }

    @Override public String getDescription() { return "Allows you to interact through entities and blocks."; }
}
