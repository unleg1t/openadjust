package com.adjustclient.modules.impl.combat.builder.feature.beddefense;

import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public enum BedDefenseBlockType {
    WOOL("Wool", Set.of(Blocks.wool)),
    GLASS("Glass", Set.of(Blocks.glass, Blocks.stained_glass)),
    WOOD("Wood", Set.of(Blocks.planks, Blocks.log, Blocks.log2)),
    END_STONE("End Stone", Set.of(Blocks.end_stone)),
    CLAY("Clay", Set.of(Blocks.hardened_clay, Blocks.stained_hardened_clay)),
    OBSIDIAN("Obsidian", Set.of(Blocks.obsidian)),
    OTHER("Block", Set.of());

    public final String displayName;
    public final Set<Block> blocks;

    BedDefenseBlockType(String displayName, Set<Block> blocks) {
        this.displayName = displayName;
        this.blocks = blocks;
    }
}
