package com.adjustclient.utilities.hypixel.game.build;

import java.util.Objects;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;

public record BuildBlock(IBlockState state, AxisAlignedBB bounds) {
    @Override public boolean equals(Object other) {
        return other == this || other instanceof BuildBlock block
                && Objects.equals(bounds, block.bounds) && Objects.equals(state, block.state);
    }
    @Override public int hashCode() { return 31 * Objects.hashCode(state) + Objects.hashCode(bounds); }
    @Override public String toString() { return "c[state=" + state + ", bounds=" + bounds + "]"; }
}
