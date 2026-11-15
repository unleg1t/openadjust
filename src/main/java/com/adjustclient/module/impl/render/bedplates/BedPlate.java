package com.adjustclient.module.impl.render.bedplates;

import com.adjustclient.module.impl.render.hud.playerlist.stats.BedWarsTeam;
import com.adjustclient.util.Timer;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.util.Vec3;

public final class BedPlate {
    Vec3 center;
    final List<BedLayer> layers = new ArrayList<>();
    final Object2IntLinkedOpenHashMap<Block> blocks = new Object2IntLinkedOpenHashMap<>();
    final Timer timer = new Timer();
    BedWarsTeam team;
    boolean incomplete;
    boolean dirty;

    void rebuildBlocks() {
        blocks.clear();
        for (BedLayer layer : layers) {
            for (Block block : layer.blocks.keySet()) {
                blocks.put(block, layer.getMetadata(block));
            }
        }
    }

    void markDirty() {
        if (timer.hasElapsed(500L)) dirty = true;
    }
}
