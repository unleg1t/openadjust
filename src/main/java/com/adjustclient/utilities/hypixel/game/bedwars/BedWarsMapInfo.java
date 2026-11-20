package com.adjustclient.utilities.hypixel.game.bedwars;

import com.adjustclient.modules.impl.render.hud.playerlist.stats.BedWarsTeam;
import com.adjustclient.utilities.MinecraftInstance;
import com.google.gson.*;
import java.util.*;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

public final class BedWarsMapInfo implements MinecraftInstance {
    public final BedWarsGameState state;
    public final List<Pair<BlockPos, BedWarsTeam>> scannedTeams = new ArrayList<>();
    public int[] sectorTeams = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
    public boolean initialized;
    public Object field20;
    public static final Map<String, int[]> cachedMaps = new HashMap<>();
    public static final Map<String, Integer> teamIndices = new HashMap<>();
    static {
        cachedMaps.put("Orchid", new int[] {6, 6, 7, 7, 0, 0, 5, 5});
        cachedMaps.put("Silted", new int[] {6, 6, 7, 7, 0, 0, 5, 5});
        cachedMaps.put("Turtle Cove", new int[] {2, 3, 4, 5, 6, 7, 0, 1});
        cachedMaps.put("Keep", new int[] {5, 6, 7, 0, 1, 2, 3, 4});
        cachedMaps.put("Slumber", new int[] {3, 2, 1, 0, 7, 6, 5, 4});
        cachedMaps.put("Shark Attack", new int[] {5, 5, 6, 6, 7, 7, 0, 0});
        cachedMaps.put("Aquarium", new int[] {5, 6, 6, 7, 7, 0, 0, 5});
        cachedMaps.put("Nostalgia", new int[] {0, 0, 5, 5, 6, 6, 7, 7});
        cachedMaps.put("Retreat", new int[] {5, 4, 3, 2, 1, 0, 7, 6});
        cachedMaps.put("Infinite", new int[] {6, 6, 5, 5, 0, 0, 7, 7});
        cachedMaps.put("Mosdalr", new int[] {5, 7, 7, 6, 6, 0, 0, 5});
        cachedMaps.put("Coastal", new int[] {5, 6, 6, 7, 7, 0, 0, 5});
        cachedMaps.put("Paladin", new int[] {0, 5, 5, 6, 6, 7, 7, 0});
        cachedMaps.put("Raze", new int[] {0, 5, 5, 6, 6, 7, 7, 0});
        cachedMaps.put("Cliffside", new int[] {7, 6, 5, 4, 3, 2, 1, 0});
        cachedMaps.put("Paradox", new int[] {6, 6, 7, 7, 0, 0, 5, 5});
        cachedMaps.put("Deposit", new int[] {0, 0, 5, 5, 6, 6, 7, 7});
        cachedMaps.put("Tigris", new int[] {6, 6, 7, 7, 0, 0, 5, 5});
        cachedMaps.put("Dragon Light", new int[] {2, 3, 4, 5, 6, 7, 0, 1});
        cachedMaps.put("Yue", new int[] {2, 3, 4, 5, 6, 7, 0, 1});
        cachedMaps.put("Lost Temple", new int[] {7, 7, 0, 0, 5, 5, 6, 6});
        cachedMaps.put("Ambush", new int[] {6, 7, 0, 1, 2, 3, 4, 5});
        cachedMaps.put("Vigilante", new int[] {2, 3, 4, 5, 6, 7, 0, 1});
        cachedMaps.put("Arid", new int[] {2, 3, 4, 5, 6, 7, 0, 1});
        cachedMaps.put("Stilted", new int[] {6, 6, 7, 7, 0, 0, 5, 5});
        cachedMaps.put("Bio-Hazard", new int[] {4, 5, 6, 7, 0, 1, 2, 3});
        cachedMaps.put("Boletum", new int[] {7, 0, 0, 5, 5, 6, 6, 7});
        cachedMaps.put("Tengshe", new int[] {5, 6, 6, 7, 7, 0, 0, 5});
        cachedMaps.put("Zen Plaza", new int[] {5, 6, 6, 7, 7, 0, 0, 5});
        cachedMaps.put("Hanging Gardens", new int[] {6, 7, 0, 1, 2, 3, 4, 5});
        cachedMaps.put("Boardwalk", new int[] {5, 5, 6, 6, 7, 7, 0, 0});
        cachedMaps.put("Montipora", new int[] {3, 4, 5, 6, 7, 0, 1, 2});
        cachedMaps.put("Alaric", new int[] {5, 6, 6, 7, 7, 0, 0, 5});
        cachedMaps.put("Duye", new int[] {1, 2, 3, 4, 5, 6, 7, 0});
        cachedMaps.put("Silver Birch", new int[] {4, 5, 6, 7, 0, 1, 2, 3});
        cachedMaps.put("Usagi", new int[] {7, 0, 0, 5, 5, 6, 6, 7});
        cachedMaps.put("Mortuus", new int[] {6, 6, 7, 7, 0, 0, 5, 5});
        cachedMaps.put("Impere", new int[] {5, 6, 7, 0, 1, 2, 3, 4});
        cachedMaps.put("Babylon", new int[] {6, 7, 0, 1, 2, 3, 4, 5});
        cachedMaps.put("Terminal", new int[] {0, 5, 5, 6, 6, 7, 7, 0});
        cachedMaps.put("Highland Peaks", new int[] {5, 4, 3, 2, 1, 0, 7, 6});
        cachedMaps.put("Arcade", new int[] {2, 3, 4, 5, 6, 7, 0, 1});
        cachedMaps.put("Entangle", new int[] {6, 7, 7, 0, 0, 5, 5, 6});
        cachedMaps.put("Urban Plaza", new int[] {7, 6, 5, 4, 3, 2, 1, 0});
        cachedMaps.put("Kubo", new int[] {5, 6, 6, 7, 7, 0, 0, 5});
        cachedMaps.put("Crogorm", new int[] {6, 0, 3, 4, 1, 2, 5, 7});
        cachedMaps.put("Grave", new int[] {7, 0, 0, 5, 5, 6, 6, 7});
        cachedMaps.put("Sky Festival", new int[] {6, 7, 7, 0, 0, 5, 5, 6});
        cachedMaps.put("Beeeee", new int[] {0, 0, 5, 5, 6, 6, 7, 7});
        cachedMaps.put("Carapace", new int[] {7, 6, 6, 0, 0, 5, 5, 7});
        teamIndices.put("red", 5);
        teamIndices.put("aqua", 1);
        teamIndices.put("pink", 3);
        teamIndices.put("gray", 4);
        teamIndices.put("green", 7);
        teamIndices.put("white", 2);
        teamIndices.put("blue", 6);
        teamIndices.put("yellow", 0);
        teamIndices.put("cyan", 1);
    }

    public BedWarsMapInfo(BedWarsGameState state) { this.state = state; }

    public synchronized void initialize() {
        if (initialized || BedWarsGameState.bedLocations == null
                || state.mode == null || state.mapName == null) return;
        initialized = true;
        JsonObject data = BedWarsGameState.bedLocations;
        JsonObject map = data.getAsJsonObject("map");
        if (loadCached(state.mapName)) return;
        if (map != null && loadEntry(data, map.get(state.mapName))) return;
        JsonObject modes = data.getAsJsonObject("modes");
        if (modes != null) {
            for (Map.Entry<String, JsonElement> entry : modes.entrySet()) {
                if (state.mode.name().contains(entry.getKey()) && loadEntry(data, entry.getValue())) return;
            }
        }
    }

    public int[] parseTeamOrder(JsonArray array) {
        int[] result = new int[8];
        for (int i = 0; i < array.size(); i++) {
            JsonPrimitive value = array.get(i).getAsJsonPrimitive();
            result[i] = value.isString() ? teamIndices.getOrDefault(value.getAsString(), 5) : value.getAsInt();
        }
        return result;
    }

    public boolean loadCached(String map) {
        if (!cachedMaps.containsKey(map)) return false;
        sectorTeams = cachedMaps.get(map);
        return true;
    }

    public synchronized BedWarsTeam getTeam(BlockPos pos) {
        initialize();
        if (initialized) {
            return BedWarsTeam.VALUES[sectorTeams[sector(pos.getX(), pos.getZ())]];
        }
        for (Pair<BlockPos, BedWarsTeam> entry : scannedTeams) {
            if (entry.getLeft().distanceSq(pos) < 12.25) return entry.getRight();
        }
        BlockPos immutablePos = new BlockPos(pos);
        BedWarsTeam team = scanTeam(immutablePos);
        scannedTeams.add(Pair.of(immutablePos, team));
        return team;
    }

    public int sector(int x, int z) {
        if (x >= 0 && z >= 0) return x <= z ? 1 : 0;
        int ax = Math.abs(x), az = Math.abs(z);
        if (x >= 0) return az <= ax ? 7 : 6;
        if (z < 0) return ax <= az ? 5 : 4;
        return az <= ax ? 3 : 2;
    }

    public boolean loadEntry(JsonObject data, JsonElement value) {
        if (value == null) return false;
        if (!value.isJsonArray()) value = data.get(value.getAsString());
        if (!(value instanceof JsonArray)) return false;
        sectorTeams = parseTeamOrder(value.getAsJsonArray());
        return true;
    }

    public BedWarsTeam scanTeam(BlockPos origin) {
        net.minecraft.world.World world = mc.theWorld;
        if (world == null) return null;
        Map<EnumDyeColor, Integer> counts = new HashMap<>();
        for (int x = -16; x < 16; x++) {
            for (int y = -2; y < 10; y++) {
                for (int z = -16; z < 16; z++) {
                    IBlockState state = world.getBlockState(origin.add(x, y, z));
                    if (state.getBlock() != Blocks.wool) continue;
                    for (Map.Entry<IProperty, Comparable> property : state.getProperties().entrySet()) {
                        if (!"color".equals(property.getKey().getName())) continue;
                        EnumDyeColor color = (EnumDyeColor) property.getValue();
                        counts.put(color, counts.getOrDefault(color, 0) + 1);
                    }
                }
            }
        }
        EnumDyeColor best = null;
        int count = Integer.MIN_VALUE;
        for (Map.Entry<EnumDyeColor, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > count) { best = entry.getKey(); count = entry.getValue(); }
        }
        return best == null ? null : BedWarsTeam.byDyeColor(best);
    }
}
