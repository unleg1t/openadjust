package com.adjustclient.module.impl.combat.builder.feature.beddefense;

import java.util.Objects;

public final class BedDefensePlacement {
    public final BedDefenseBlockType type;
    public final int x;
    public final int y;
    public final int z;
    public final int blockId;

    public BedDefensePlacement(int x, int y, int z, BedDefenseBlockType type, int blockId) {
        if (Math.abs(x) > 16 || Math.abs(y) > 16 || Math.abs(z) > 16)
            throw new IllegalArgumentException("Placement is too far from the bed");
        if (net.minecraft.block.Block.getBlockById(blockId) == null)
            throw new IllegalArgumentException("Unknown block id: " + blockId);
        Objects.requireNonNull(type);
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.blockId = blockId;
    }

    @Override public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof BedDefensePlacement other)) return false;
        return x == other.x
                && y == other.y
                && z == other.z
                && blockId == other.blockId
                && type == other.type;
    }

    @Override public int hashCode() {
        return Objects.hash(x, y,
                z, type, blockId);
    }
}
