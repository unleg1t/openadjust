package com.adjustclient.modules.impl.render.bedplates;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.BlockRenderStateEvent;
import com.adjustclient.utilities.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.utilities.hypixel.game.bedwars.BedWarsMapInfo;
import com.adjustclient.modules.impl.render.hud.playerlist.stats.BedWarsTeam;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.anticheat.ServerTracker;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.World;

public final class BedTracker implements MinecraftInstance {
    private static final int[] DEFAULT_SECTOR_TEAMS = {0, 1, 2, 3, 4, 5, 6, 7};
    final BedPlatesModule module;
    final Map<BlockPos, BedPlate> beds = new HashMap<>();
    BedTracker(BedPlatesModule module) { this.module = module; }

    public void clear() { synchronized (beds) { beds.clear(); } }

    public void onBlockChange(BlockRenderStateEvent event) {
        if (event == null || event.position == null || event.state == null) return;
        BlockPos position = event.position;
        IBlockState state = event.state;
        if (state.getBlock() == Blocks.bed) { update(position, state); return; }
        synchronized (beds) {
            for (Map.Entry<BlockPos, BedPlate> entry : beds.entrySet()) {
                if (entry.getKey().getY() <= position.getY() && position.distanceSq(entry.getKey()) <= 144.0) {
                    entry.getValue().markDirty();
                    break;
                }
            }
        }
    }

    public void remove(BlockPos bed) { synchronized (beds) { beds.remove(bed); } }

    private static <T extends Comparable<T>> T getValueOrNull(IBlockState state, IProperty<T> property) {
        return state.getProperties().containsKey(property) ? state.getValue(property) : null;
    }

    private static BedWarsTeam resolveTeam(BedWarsMapInfo mapInfo, BlockPos bedPos) {
        if (mapInfo == null || mc.theWorld == null) return null;
        try {
            synchronized (mapInfo) {
                if (mapInfo.sectorTeams == null) {
                    mapInfo.sectorTeams = DEFAULT_SECTOR_TEAMS.clone();
                }
                return mapInfo.getTeam(bedPos);
            }
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private Vec3 getCenter(World world, BedPlate plate, BlockPos bed) {
        IBlockState state = world.getBlockState(bed);
        EnumFacing facing = getValueOrNull(state, BlockBed.FACING);
        BlockBed.EnumPartType part = getValueOrNull(state, BlockBed.PART);
        if (facing == null || part == null) return new Vec3(bed);
        if (part == BlockBed.EnumPartType.HEAD) facing = facing.getOpposite();
        Vec3 center = new Vec3(
                bed.getX() + 0.5 + (facing.getAxis() == EnumFacing.Axis.X ? facing.getAxisDirection().getOffset() * 0.5F : 0.0F),
                bed.getY() + 0.5 + (facing.getAxis() == EnumFacing.Axis.Y ? facing.getAxisDirection().getOffset() * 0.5F : 0.0F),
                bed.getZ() + 0.5 + (facing.getAxis() == EnumFacing.Axis.Z ? facing.getAxisDirection().getOffset() * 0.5F : 0.0F)
        );
        for (int i = 0; i <= plate.layers.size(); i++) {
            BlockPos pos = bed.up(i);
            if (world.getBlockState(pos).getBlock() != Blocks.air
                    || world.getBlockState(pos.offset(facing)).getBlock() != Blocks.air) {
                center = center.addVector(0.0, 1.0, 0.0);
            }
        }
        return center;
    }

    public void update(BlockPos bed, IBlockState state) {
        World world = mc.theWorld;
        if (world == null || bed == null || state == null) return;
        EnumFacing facing = getValueOrNull(state, BlockBed.FACING);
        BlockBed.EnumPartType part = getValueOrNull(state, BlockBed.PART);
        if (facing == null || part == null || part == BlockBed.EnumPartType.FOOT) return;
        BlockPos bedPos = new BlockPos(bed.getX(), bed.getY(), bed.getZ());
        BlockPos other = bedPos.offset(facing.getOpposite());
        if (world.getBlockState(other).getBlock() != Blocks.bed) return;
        BedPlate plate = new BedPlate();
        collect(world, bedPos, plate, facing);
        collect(world, other, plate, facing.getOpposite());
        plate.layers.removeIf(layer -> {
            int count = 0;
            for (Set<BlockPos> positions : layer.blocks.values()) count += positions.size();
            return count == 0;
        });
        if (plate.layers.isEmpty()) {
            BedLayer layer = new BedLayer(1);
            layer.blocks.put(Blocks.bed, Set.of(BlockPos.ORIGIN));
            plate.layers.add(layer);
        }
        plate.center = getCenter(world, plate, bedPos);
        if (Adjust.gameStateTracker != null && Adjust.gameStateTracker.state instanceof BedWarsGameState gameState) {
            if (module.settings.ignoreLocal.isEnabled() && gameState.isNearSpawn(bedPos)) {
                remove(bedPos);
                return;
            }
            BedWarsTeam team = resolveTeam(gameState.mapInfo, bedPos);
            plate.team = team;
            if (team != null && module.settings.ignoreWhitelisted.isEnabled()
                    && Adjust.INSTANCE != null && Adjust.INSTANCE.tagManager != null) {
                try {
                    for (String name : Adjust.INSTANCE.tagManager.getSavedNames()) {
                        if (team == gameState.findPlayerTeam(name)) {
                            remove(bedPos);
                            return;
                        }
                    }
                } catch (RuntimeException ignored) {
                }
            }
        }
        if (module.settings.mode.getValue() == BedPlatesMode.BLOCKS) plate.rebuildBlocks();
        plate.incomplete = ServerTracker.INSTANCE != null && !ServerTracker.INSTANCE.onBlockStateUpdate(bedPos).isEmpty();
        synchronized (beds) { beds.put(bedPos, plate); }
    }

    private void collect(World world, BlockPos bed, BedPlate plate, EnumFacing facing) {
        BedScanResult x = null, z = null;
        switch (facing) {
            case NORTH: z = new BedScanResult(null, bed.getZ()); break;
            case SOUTH: z = new BedScanResult(bed.getZ(), null); break;
            case WEST: x = new BedScanResult(bed.getX(), null); break;
            case EAST: x = new BedScanResult(null, bed.getX()); break;
            default: return;
        }
        for (EnumFacing direction : EnumFacing.values()) {
            if (direction == EnumFacing.DOWN) continue;
            BlockPos pos = bed.offset(direction);
            if (!inRange(pos, x, z)) continue;
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (!(block.isFullBlock() || block == Blocks.ladder || block == Blocks.stained_glass)) continue;
            if (plate.layers.isEmpty()) plate.layers.add(new BedLayer(1));
            BedLayer layer = plate.layers.get(0);
            layer.blocks.computeIfAbsent(block, ignored -> new HashSet<>()).add(pos);
            layer.metadata.put(block, block.damageDropped(state));
            scanNeighbors(world, pos, 2, new HashSet<>(Collections.singleton(direction.getOpposite())), plate, x, z);
        }
    }

    private boolean inRange(BlockPos pos, BedScanResult x, BedScanResult z) {
        if (x != null && ((x.minimum != null && pos.getX() < x.minimum) || (x.maximum != null && pos.getX() > x.maximum))) return false;
        return z == null || ((z.minimum == null || pos.getZ() >= z.minimum) && (z.maximum == null || pos.getZ() <= z.maximum));
    }

    private void scanNeighbors(World world, BlockPos pos, int depth, Set<EnumFacing> excluded, BedPlate plate, BedScanResult x, BedScanResult z) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (facing == EnumFacing.DOWN || (excluded != null && excluded.contains(facing))) continue;
            BlockPos next = pos.offset(facing);
            if (!inRange(next, x, z)) continue;
            IBlockState state = world.getBlockState(next);
            Block block = state.getBlock();
            if (!(block.isFullBlock() || block == Blocks.ladder || block == Blocks.stained_glass)) continue;
            if (depth > plate.layers.size()) plate.layers.add(new BedLayer(depth));
            BedLayer layer = plate.layers.get(depth - 1);
            layer.blocks.computeIfAbsent(block, ignored -> new HashSet<>()).add(next);
            layer.metadata.put(block, block.damageDropped(state));
            Set<EnumFacing> directions = excluded == null ? new HashSet<>() : excluded;
            directions.add(facing.getOpposite());
            if (depth < 7) scanNeighbors(world, next, depth + 1, directions, plate, x, z);
        }
    }

    public void tick() {
        World world = mc.theWorld;
        if (world == null) return;
        synchronized (beds) {
            Map<BlockPos, IBlockState> pending = new HashMap<>();
            Iterator<Map.Entry<BlockPos, BedPlate>> iterator = beds.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<BlockPos, BedPlate> entry = iterator.next();
                IBlockState state = world.getBlockState(entry.getKey());
                if (state.getBlock() != Blocks.bed) { iterator.remove(); continue; }
                BedPlate plate = entry.getValue();
                plate.incomplete = ServerTracker.INSTANCE != null && !ServerTracker.INSTANCE.onBlockStateUpdate(entry.getKey()).isEmpty();
                if (plate.dirty) pending.put(entry.getKey(), state);
            }
            for (Map.Entry<BlockPos, IBlockState> entry : pending.entrySet()) {
                update(entry.getKey(), entry.getValue());
            }
        }
    }
}
