package com.adjustclient.module.impl.player.breaker;

import com.adjustclient.setting.ModeOption;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public enum BreakerBlock implements ModeOption {
    BED("Bed", Blocks.bed), DRAGON_EGG("Dragon Egg", Blocks.dragon_egg);
    private final String name;
    public final Block block;
    BreakerBlock(String name, Block block) { this.name = name; this.block = block; }
    public String getName() { return name; }
}
