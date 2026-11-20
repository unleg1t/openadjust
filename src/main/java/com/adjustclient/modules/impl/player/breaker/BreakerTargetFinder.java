package com.adjustclient.modules.impl.player.breaker;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.utilities.hypixel.game.bedwars.BedWarsMapInfo;
import com.adjustclient.modules.impl.render.hud.playerlist.stats.BedWarsTeam;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.*;
import com.adjustclient.utilities.anticheat.ServerTracker;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.WorldSettings;

public class BreakerTargetFinder implements MinecraftInstance {
    public final BreakerModule module;
    public BreakerTargetFinder(BreakerModule module) { this.module = module; }

    public boolean canSearch() {
        return mc.thePlayer.sendQueue.doneLoadingTerrain && mc.thePlayer.getHealth() > 0.0F && isSupportedGame();
    }

    public List<BreakTarget> collectSurrounding(List<BlockPos> parts) {
        List<BreakTarget> result = new ArrayList<>();
        for (BlockPos part : parts) {
            for (EnumFacing facing : EnumFacing.values()) {
                if (facing == EnumFacing.DOWN) continue;
                BlockPos pos = part.offset(facing);
                if (parts.contains(pos)) continue;
                IBlockState state = mc.theWorld.getBlockState(pos);
                if (state.getBlock() == Blocks.bed) continue;
                result.add(new BreakTarget(pos, getExposedFaces(pos), state));
            }
        }
        return result;
    }

    public static boolean isFaceExposed(BlockPos pos, EnumFacing face) {
        AxisAlignedBB own = BlockUtil.getCollisionBox(pos);
        BlockPos neighbor = pos.offset(face);
        AxisAlignedBB adjacent = BlockUtil.getCollisionBox(neighbor);
        return adjacent == null || (own == null ? adjacent.maxY - adjacent.minY < 1.0
                : own.maxY - own.minY > adjacent.maxY - adjacent.minY)
                || adjacent.maxX - adjacent.minX < 1.0 || adjacent.maxZ - adjacent.minZ < 1.0;
    }

    public BlockReachCheck findTarget() {
        Block wanted = module.block.getValue().block;
        BlockPos target = BlockUtil.findBed(pos -> {
            if (mc.theWorld.getBlockState(pos).getBlock() != Blocks.bed) return false;
            var server = ServerTracker.INSTANCE.serverState;
            if (server != null && server.antiCheatModel != null) {
                if (server.antiCheatModel.serverType == ServerType.HYPIXEL) {
                    var game = Adjust.gameStateTracker.state;
                    if (!(game instanceof BedWarsGameState bedWars) || bedWars.startTimer == null) return true;
                    if (bedWars.isNearSpawn(pos)) return false;
                    BedWarsMapInfo map = bedWars.mapInfo;
                    if (map == null) return true;
                    BedWarsTeam team = map.getTeam(pos);
                    if (team == null) return true;
                    for (String name : Adjust.INSTANCE.tagManager.getSavedNames())
                        if (bedWars.findPlayerTeam(name) == team) return false;
                    return true;
                }
                if (server.antiCheatModel.serverType == ServerType.REWOUND) {
                    var game = Adjust.rewoundGameTracker.state;
                    if (game instanceof com.adjustclient.utilities.game.rewound.RewoundBedWarsGameState rewoundBedWars
                            && rewoundBedWars.startTimer != null
                            && rewoundBedWars.isNearSpawn(pos)) {
                        return false;
                    }
                }
            }
            return true;
        });
        if (target == null) return null;
        BlockPos[] parts = wanted == Blocks.bed ? BlockUtil.getBedParts(target) : new BlockPos[] {target};
        for (BlockPos part : parts) {
            List<EnumFacing> exposed = getExposedFaces(part);
            long breakTime = BlockUtil.getBreakTimeMs(wanted, part, mc.thePlayer.inventory.getCurrentItem());
            if (!exposed.isEmpty() || !module.breakSurrounding.isEnabled())
                return new BlockReachCheck(part, part, null, breakTime);
        }
        List<BreakTarget> surrounding = collectSurrounding(List.of(parts));
        surrounding.removeIf(candidate -> candidate.exposedFaces.isEmpty());
        surrounding.forEach(candidate -> {
            candidate.tool = module.toolSelector.select(candidate.state.getBlock());
            candidate.breakTime = BlockUtil.getBreakTimeMs(candidate.state.getBlock(), candidate.pos, candidate.tool.stack);
        });
        surrounding.removeIf(candidate -> (float) candidate.breakTime >= module.maxBreakTime.getValue().floatValue() * 1000.0F);
        surrounding.sort(Comparator.comparingDouble(candidate -> {
            AxisAlignedBB box = BlockUtil.getCollisionBox(candidate.pos);
            Vec3 center = box == null ? new Vec3(candidate.pos) : box.getCenter();
            return (double) candidate.breakTime + mc.thePlayer.getPositionVector().squareDistanceTo(center) * 0.001;
        }));
        if (surrounding.isEmpty()) return null;
        BreakTarget selected = surrounding.get(0);
        return new BlockReachCheck(selected.pos, target, selected.tool, selected.breakTime,
                () -> mc.theWorld.getBlockState(target).getBlock() instanceof BlockBed);
    }

    public static boolean isSupportedGame() {
        var state = ServerTracker.INSTANCE.serverState;
        if (state == null || state.antiCheatModel == null
                || state.antiCheatModel.serverType != ServerType.HYPIXEL)
            return mc.playerController.currentGameType == WorldSettings.GameType.SURVIVAL;
        var game = Adjust.gameStateTracker.state;
        return game.game == com.adjustclient.utilities.hypixel.HypixelGame.BED_WARS
                && game.startTimer != null && !game.ended && !game.isSpectator();
    }

    public List<EnumFacing> getExposedFaces(BlockPos pos) {
        AxisAlignedBB own = BlockUtil.getCollisionBox(pos);
        if (own == null) return Collections.emptyList();
        TrackedBreak history = module.tracker.get(pos);
        double height = own.maxY - own.minY;
        List<EnumFacing> result = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(facing);
            AxisAlignedBB box = BlockUtil.getCollisionBox(neighbor);
            if (box == null || height > box.maxY - box.minY || box.maxX - box.minX < 1.0 || box.maxZ - box.minZ < 1.0) {
                if (history == null || !history.attempted.contains(neighbor) || history.attempted.size() >= 8) result.add(facing);
            }
        }
        return result;
    }

    public List<BlockPos> getSurrounding(BlockPos pos) {
        List<BlockPos> result = new ArrayList<>(8);
        for (BlockPos part : BlockUtil.getBedParts(pos))
            getExposedFaces(part).forEach(face -> { if (isFaceExposed(part, face)) result.add(part.offset(face)); });
        return result;
    }
}
