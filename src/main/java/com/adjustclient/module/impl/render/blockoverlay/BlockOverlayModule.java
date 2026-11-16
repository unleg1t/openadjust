package com.adjustclient.module.impl.render.blockoverlay;

import com.adjustclient.Adjust;
import com.adjustclient.event.BlockRenderStateEvent;
import com.adjustclient.event.CancellablePlayerUpdateEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.Render3DEvent;
import com.adjustclient.event.WorldResetEvent;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.hypixel.HypixelGameState;
import com.adjustclient.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.hypixel.game.bedwars.BedWarsMapInfo;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.render.hud.playerlist.stats.BedWarsTeam;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import java.awt.Color;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import static com.adjustclient.util.MinecraftInstance.mc;

public final class BlockOverlayModule extends Module {
    private static final int[] DEFAULT_SECTOR_TEAMS = {0, 1, 2, 3, 4, 5, 6, 7};
    public static BlockOverlayModule INSTANCE;
    public final BlockOverlaySettings lookingAt;
    public final ChestOverlaySettings chest;
    public final RangedOverlaySettings trappedChest;
    public final RangedOverlaySettings enderChest;
    public final RangedOverlaySettings hopper;
    public final RangedOverlaySettings dispenser;
    public final RangedOverlaySettings dropper;
    public final SpawnerOverlaySettings mobSpawner;
    public final BedOverlaySettings bed;
    public final ObsidianOverlaySettings obsidian;
    private final Map<BlockOverlaySettings, Block> cachedTargets;
    private final Map<BlockPos, TileEntityTarget> visibleTargets;
    private final Map<BlockPos, BlockTarget> blockCache;
    private boolean rendering;
    private boolean needsRefresh;

    public BlockOverlayModule() {
        super("Block Overlay", Category.RENDER);
        INSTANCE = this;
        lookingAt = new BlockOverlaySettings("Looking at", this, new Color(67, 90, 209, 55), true, true, this::markRefresh);
        chest = new ChestOverlaySettings(this, new Color(231, 153, 76, 55), this::markRefresh);
        trappedChest = new RangedOverlaySettings("Trapped Chest", this, new Color(232, 78, 78, 55), false, this::markRefresh);
        enderChest = new RangedOverlaySettings("Ender Chest", this, new Color(162, 62, 229, 55), false, this::markRefresh);
        hopper = new RangedOverlaySettings("Hopper", this, new Color(103, 103, 102, 55), false, this::markRefresh);
        dispenser = new RangedOverlaySettings("Dispenser", this, new Color(57, 232, 255, 55), false, this::markRefresh);
        dropper = new RangedOverlaySettings("Dropper", this, new Color(81, 201, 87, 55), false, this::markRefresh);
        mobSpawner = new SpawnerOverlaySettings(this, new Color(237, 99, 36, 55), this::markRefresh);
        bed = new BedOverlaySettings(this, new Color(98, 158, 201, 85));
        obsidian = new ObsidianOverlaySettings(this, new Color(109, 19, 168, 55));
        cachedTargets = new HashMap<>();
        visibleTargets = new HashMap<>();
        blockCache = new HashMap<>();
        setHidden(false);
        clearCaches();
    }

    public void markRefresh() { needsRefresh = true; }

    public void shouldRenderOverlay() {
        boolean visible = bed.full.isEnabled() && bed.fullColor.getColor().getAlpha() != 0;
        if (!visible) visible = bed.outline.isEnabled() && bed.fullColor.getColor().getAlpha() != 0;
        if (!visible || !bed.ignoreWhitelisted.isEnabled()) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.renderGlobal == null) return;
        if (minecraft.isCallingFromMinecraftThread()) {
            minecraft.renderGlobal.loadRenderers();
        } else {
            minecraft.addScheduledTask(RenderUtil.outlineRefresh(minecraft));
        }
    }

    @EventHandler
    public void collectTargets(CancellablePlayerUpdateEvent event) {
        if (event == null || event.isCancelled() || !isEnabled()) return;
        collectTargets();
    }

    public void collectTargets() {
        World world = mc.theWorld;
        if (world == null) return;
        if (ServerTracker.INSTANCE != null
                && ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL) {
            HypixelGameState state = Adjust.gameStateTracker == null ? null : Adjust.gameStateTracker.state;
            rendering = state != null && Boolean.FALSE.equals(state.lobby);
            if (!rendering) return;
        } else {
            rendering = true;
        }

        if (needsRefresh) {
            visibleTargets.clear();
            try {
                for (TileEntity tile : world.loadedTileEntityList) {
                    RangedOverlaySettings settings = settingsFor(tile);
                    if (settings == null || !settings.isVisible()) continue;
                    BlockPos pos = new BlockPos(tile.getPos());
                    Block block = world.getBlockState(pos).getBlock();
                    if (block != tile.getBlockType()) continue;
                    if (acceptsTileEntity(tile, settings)) continue;

                    block.setBlockBoundsBasedOnState(world, pos);
                    AxisAlignedBB bounds = block.getSelectedBoundingBox(world, pos);

                    if (tile instanceof TileEntityChest) {
                        TileEntityChest chestEntity = (TileEntityChest) tile;
                        chestEntity.checkForAdjacentChests();
                        TileEntityChest adjacent = chestEntity.adjacentChestZNeg;
                        if (adjacent == null) adjacent = chestEntity.adjacentChestXNeg;
                        if (adjacent == null) adjacent = chestEntity.adjacentChestXPos;
                        if (adjacent == null) adjacent = chestEntity.adjacentChestZPos;
                        if (adjacent != null) {
                            BlockPos adjPos = new BlockPos(adjacent.getPos());
                            if (visibleTargets.containsKey(adjPos)) {
                                continue;
                            }
                            Block adjBlock = world.getBlockState(adjPos).getBlock();
                            adjBlock.setBlockBoundsBasedOnState(world, adjPos);
                            bounds = bounds.union(adjBlock.getSelectedBoundingBox(world, adjPos));
                        }
                    }

                    visibleTargets.put(pos, new TileEntityTarget(settings, bounds, tile));
                }
                needsRefresh = false;
            } catch (ConcurrentModificationException | NoSuchElementException ignored) {
            }
        } else {
            visibleTargets.values().removeIf(
                    target -> acceptsTileEntity(target.tileEntity, target.settings));
        }

        synchronized (blockCache) {
            Iterator<Map.Entry<BlockPos, BlockTarget>> it = blockCache.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, BlockTarget> entry = it.next();
                BlockTarget target = entry.getValue();
                Block expected = cachedTargets.get(target.settings);
                BlockPos pos = entry.getKey();
                if (world.getBlockState(pos).getBlock() != expected) {
                    it.remove();
                    continue;
                }
                if (target.settings instanceof ObsidianOverlaySettings
                        && ((ObsidianOverlaySettings) target.settings).onlySurroundingBeds.isEnabled()
                        && isBedPartVisible(pos)) {
                    it.remove();
                    continue;
                }
                if (target.settings instanceof BedOverlaySettings) {
                    BlockPos[] parts = BlockUtil.getBedParts(pos);
                    if (shouldRenderBedOverlay(pos, (BedOverlaySettings) target.settings)
                            || parts == null || parts.length < 2 || parts[1] == null
                            || world.getBlockState(parts[1]).getBlock() != expected) {
                        it.remove();
                    }
                }
            }
        }
    }

    @EventHandler
    public void renderOverlays(Render3DEvent event) {
        if (!isEnabled()) return;
        renderOverlays();
    }

    public void renderOverlays() {
        if (visibleTargets.isEmpty() && blockCache.isEmpty()) return;
        if (!rendering) return;
        RenderUtil.begin3d();
        try {
            RenderManager manager = mc.getRenderManager();
            double viewX = manager.viewerPosX;
            double viewY = manager.viewerPosY;
            double viewZ = manager.viewerPosZ;
            for (TileEntityTarget target : visibleTargets.values()) {
                if (!isInRange(target.bounds, target.settings)) {
                    drawTarget(target.settings,
                            target.settings.fullColor.getColor().getRGB(),
                            target.settings.outlineColor.getColor().getRGB(),
                            target.bounds.offset(-viewX, -viewY, -viewZ));
                }
            }
            synchronized (blockCache) {
                for (Map.Entry<BlockPos, BlockTarget> entry : blockCache.entrySet()) {
                    BlockTarget target = entry.getValue();
                    if (isInRange(target.bounds, target.settings)) continue;
                    drawTarget(target.settings, target.color, target.outlineColor,
                            target.bounds.offset(-viewX, -viewY, -viewZ));
                    if (target.settings == bed && bed.isVisible() && bed.showMissingBlocks.isEnabled()
                            && ServerTracker.INSTANCE != null) {
                        RenderUtil.glColor(((target.color >>> 25) << 24) | (Color.RED.getRGB() & 0xFFFFFF));
                        BlockPos keyPos = entry.getKey();
                        for (BlockPos missing : ServerTracker.INSTANCE.onBlockStateUpdate(keyPos)) {
                            RenderUtil.drawFilledBox(new AxisAlignedBB(
                                    missing.getX() - viewX, missing.getY() - viewY, missing.getZ() - viewZ,
                                    missing.getX() + 1 - viewX, missing.getY() + 1 - viewY, missing.getZ() + 1 - viewZ));
                        }
                    }
                }
            }
        } finally {
            RenderUtil.end3d();
        }
    }

    private static BedWarsTeam resolveBedTeam(BedWarsMapInfo mapInfo, BlockPos pos) {
        if (mapInfo == null || mc.theWorld == null) return null;
        try {
            synchronized (mapInfo) {
                if (mapInfo.sectorTeams == null) {
                    mapInfo.sectorTeams = DEFAULT_SECTOR_TEAMS.clone();
                }
                return mapInfo.getTeam(pos);
            }
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    @EventHandler
    public void updateTargetCache(BlockRenderStateEvent event) {
        World world = mc.theWorld;
        if (!isEnabled() || world == null || event == null || event.state == null || event.position == null) return;
        synchronized (blockCache) {
            Block block = event.state.getBlock();
            for (Map.Entry<BlockOverlaySettings, Block> entry : cachedTargets.entrySet()) {
                if (entry.getValue() != block) continue;
                BlockOverlaySettings settings = entry.getKey();
                BlockPos pos = new BlockPos(event.position);
                if (blockCache.containsKey(pos)) continue;

                AxisAlignedBB bounds = block.getSelectedBoundingBox(world, pos);
                int fullColor = settings.fullColor.getColor().getRGB();
                int outlineColor = settings.outlineColor.getColor().getRGB();

                if (settings instanceof BedOverlaySettings) {
                    BedOverlaySettings bedSettings = (BedOverlaySettings) settings;
                    if (shouldRenderBedOverlay(pos, bedSettings)) continue;
                    EnumFacing facing = event.state.getProperties().containsKey(BlockBed.FACING)
                            ? event.state.getValue(BlockBed.FACING) : null;
                    BlockBed.EnumPartType part = event.state.getProperties().containsKey(BlockBed.PART)
                            ? event.state.getValue(BlockBed.PART) : null;
                    if (facing == null || part == null) continue;
                    BlockPos otherPos = part == BlockBed.EnumPartType.HEAD
                            ? pos.offset(facing.getOpposite())
                            : pos.offset(facing);
                    if (world.getBlockState(otherPos).getBlock() != block) continue;
                    if (blockCache.containsKey(otherPos)) {
                        continue;
                    }
                    bounds = bounds.union(block.getSelectedBoundingBox(world, otherPos));
                    if (bedSettings.teamColor.isEnabled()
                            && Adjust.gameStateTracker != null
                            && Adjust.gameStateTracker.state instanceof BedWarsGameState) {
                        BedWarsMapInfo mapInfo = ((BedWarsGameState) Adjust.gameStateTracker.state).mapInfo;
                        BedWarsTeam team = resolveBedTeam(mapInfo, pos);
                        if (team != null) {
                            int rgb = team.dyeColor.getMapColor().colorValue & 0xFFFFFF;
                            fullColor = (fullColor & 0xFF000000) | rgb;
                            outlineColor = (outlineColor & 0xFF000000) | rgb;
                        }
                    }
                } else if (settings instanceof ObsidianOverlaySettings) {
                    if (((ObsidianOverlaySettings) settings).onlySurroundingBeds.isEnabled()
                            && isBedPartVisible(pos)) {
                        continue;
                    }
                }

                blockCache.put(pos, new BlockTarget(settings, bounds, fullColor, outlineColor));
            }
        }
    }

    public boolean isBedPartVisible(BlockPos pos) {
        World world = mc.theWorld;
        if (world == null) return false;
        for (EnumFacing facing : EnumFacing.values()) {
            if (facing == EnumFacing.UP) continue;
            if (world.getBlockState(pos.offset(facing)).getBlock() == Blocks.bed) return false;
        }
        return true;
    }

    private RangedOverlaySettings settingsFor(TileEntity tile) {
        if (tile instanceof TileEntityChest) return ((TileEntityChest) tile).getChestType() == 1 ? trappedChest : chest;
        if (tile instanceof TileEntityEnderChest) return enderChest;
        if (tile instanceof TileEntityDropper) return dropper;
        if (tile instanceof TileEntityDispenser) return dispenser;
        if (tile instanceof TileEntityHopper) return hopper;
        if (tile instanceof TileEntityMobSpawner) return mobSpawner;
        return null;
    }

    public boolean acceptsTileEntity(TileEntity tileEntity, RangedOverlaySettings settings) {
        if (settings instanceof ChestOverlaySettings && tileEntity instanceof TileEntityChest) {
            return Adjust.gameStateTracker != null
                    && Adjust.gameStateTracker.state != null
                    && Adjust.gameStateTracker.state.game == HypixelGame.SKYWARS
                    && ((ChestOverlaySettings) settings).hideOpened.isEnabled()
                    && ((TileEntityChest) tileEntity).numPlayersUsing >= 1;
        }
        if (settings instanceof SpawnerOverlaySettings && tileEntity instanceof TileEntityMobSpawner) {
            return ((SpawnerOverlaySettings) settings).activatedOnly.isEnabled()
                    && ((TileEntityMobSpawner) tileEntity).getSpawnerBaseLogic().spawnDelay == 20;
        }
        return false;
    }

    public boolean isInRange(AxisAlignedBB bounds, BlockOverlaySettings settings) {
        if (mc.thePlayer == null || !(settings instanceof RangedOverlaySettings)) return false;
        RangedOverlaySettings ranged = (RangedOverlaySettings) settings;
        int max = ranged.maxRange.getInt();
        if (max == ranged.maxRange.getMax().intValue()) return false;
        double x = Math.max(bounds.minX, Math.min(mc.thePlayer.posX, bounds.maxX));
        double y = Math.max(bounds.minY, Math.min(mc.thePlayer.posY, bounds.maxY));
        double z = Math.max(bounds.minZ, Math.min(mc.thePlayer.posZ, bounds.maxZ));
        double dx = mc.thePlayer.posX - x;
        double dy = mc.thePlayer.posY - y;
        double dz = mc.thePlayer.posZ - z;
        return dx * dx + dy * dy + dz * dz > (double) (max * max);
    }

    public boolean shouldRenderBedOverlay(BlockPos pos, BedOverlaySettings settings) {
        if (Adjust.gameStateTracker == null || !(Adjust.gameStateTracker.state instanceof BedWarsGameState)) return false;
        BedWarsGameState state = (BedWarsGameState) Adjust.gameStateTracker.state;
        if (state.startTimer == null) return false;
        if (settings.ignoreLocal.isEnabled() && state.isNearSpawn(pos)) return true;
        if (settings.ignoreWhitelisted.isEnabled()) {
            BedWarsTeam team = resolveBedTeam(state.mapInfo, pos);
            if (team != null && Adjust.INSTANCE != null && Adjust.INSTANCE.tagManager != null) {
                try {
                    for (String name : Adjust.INSTANCE.tagManager.getSavedNames()) {
                        if (state.findPlayerTeam(name) == team) return true;
                    }
                } catch (RuntimeException ignored) {
                }
            }
        }
        return false;
    }

    public void drawTarget(BlockOverlaySettings settings, int fullColor, int outlineColor, AxisAlignedBB bounds) {
        if (settings.full.isEnabled() && settings.fullColor.getColor().getAlpha() != 0) {
            RenderUtil.glColor(fullColor);
            RenderUtil.drawFilledBox(bounds);
        }
        if (settings.outline.isEnabled() && settings.fullColor.getColor().getAlpha() != 0) {
            RenderUtil.glColor(outlineColor);
            float prevWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
            GL11.glLineWidth(prevWidth * settings.getThickness());
            RenderUtil.drawOutlinedBox(bounds);
            GL11.glLineWidth(prevWidth);
        }
    }

    @EventHandler
    public void onWorldReset(WorldResetEvent event) {
        if (!isEnabled()) return;
        needsRefresh = true;
        rendering = false;
    }

    @Override public void onEnable() {
        if (mc.renderGlobal != null && cachedTargets.keySet().stream().anyMatch(BlockOverlaySettings::isVisible)) {
            if (mc.isCallingFromMinecraftThread()) mc.renderGlobal.loadRenderers();
            else mc.addScheduledTask(RenderUtil.outlineRefresh(mc));
        }
        needsRefresh = true;
    }

    @Override public void onDisable() {
        visibleTargets.clear();
        synchronized (blockCache) {
            blockCache.clear();
        }
    }

    public void clearCaches() {
        cachedTargets.put(bed, Blocks.bed);
        cachedTargets.put(obsidian, Blocks.obsidian);
    }

    private static void lambda$constructor$0(BlockOverlayModule module) { module.needsRefresh = true; }
    private static void lambda$constructor$1(BlockOverlayModule module) { module.needsRefresh = true; }
    private static void lambda$constructor$2(BlockOverlayModule module) { module.needsRefresh = true; }
    private static void lambda$constructor$3(BlockOverlayModule module) { module.needsRefresh = true; }
    private static void lambda$constructor$4(BlockOverlayModule module) { module.needsRefresh = true; }
    private static void lambda$constructor$5(BlockOverlayModule module) { module.needsRefresh = true; }
    private static void lambda$constructor$6(BlockOverlayModule module) { module.needsRefresh = true; }
    private static void lambda$constructor$7(BlockOverlayModule module) { module.needsRefresh = true; }
}
