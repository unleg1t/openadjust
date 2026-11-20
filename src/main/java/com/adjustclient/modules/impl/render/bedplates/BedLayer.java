package com.adjustclient.modules.impl.render.bedplates;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;

final class BedLayer {
    final Map<Block, Set<BlockPos>> blocks = new HashMap<>();
    final Map<Block, Integer> metadata = new HashMap<>();
    final int index;
    BedLayer(int index) { this.index = index; }
    int getMetadata(Block block) { return metadata.getOrDefault(block, 0); }
}
