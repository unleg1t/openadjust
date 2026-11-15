package com.adjustclient.ui.clickgui.setting;

import java.util.List;
import com.adjustclient.module.impl.combat.builder.feature.beddefense.BedDefensePlacement;
import com.adjustclient.module.impl.combat.builder.feature.beddefense.BedDefensePreset;

record BedDefenseBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {

    static BedDefenseBounds a(BedDefensePreset preset) {
        List<BedDefensePlacement> blocks = preset.placements;
        float minX = 0.0F;
        float minY = 0.0F;
        float minZ = 0.0F;
        float maxX = 1.0F;
        float maxY = 1.0F;
        float maxZ = 2.0F;
        for (int i = 0; i < blocks.size(); i++) {
            BedDefensePlacement block = blocks.get(i);
            minX = Math.min(minX, (float) block.x);
            minY = Math.min(minY, (float) block.y);
            minZ = Math.min(minZ, (float) block.z);
            maxX = Math.max(maxX, (float) block.x + 1.0F);
            maxY = Math.max(maxY, (float) block.y + 1.0F);
            maxZ = Math.max(maxZ, (float) block.z + 1.0F);
        }
        return new BedDefenseBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    float fitScale(float maxWidth, float maxHeight) {
        float dz = maxZ - minZ;
        float dx = maxX - minX;
        float diagonal = (float) Math.sqrt((double) (dx * dx + dz * dz));
        float scaleW = diagonal < 1.0F ? maxWidth : maxWidth / diagonal;
        float projected = (maxY - minY) + diagonal * 0.55000001F;
        float scaleH = projected < 1.0F ? maxHeight : maxHeight / projected;
        return Math.min(Math.min(scaleW, scaleH), 18.0F);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BedDefenseBounds)) {
            return false;
        }
        BedDefenseBounds b = (BedDefenseBounds) o;
        return Float.compare(b.maxZ, maxZ) == 0 && Float.compare(b.maxY, maxY) == 0 && Float.compare(b.maxX, maxX) == 0
                && Float.compare(b.minZ, minZ) == 0 && Float.compare(b.minY, minY) == 0 && Float.compare(b.minX, minX) == 0;
    }

    @Override
    public int hashCode() {
        int h = Float.hashCode(minX);
        h = 31 * h + Float.hashCode(minY);
        h = 31 * h + Float.hashCode(minZ);
        h = 31 * h + Float.hashCode(maxX);
        h = 31 * h + Float.hashCode(maxY);
        return 31 * h + Float.hashCode(maxZ);
    }

    @Override
    public String toString() {
        return "d[minX=" + minX + ", minY=" + minY + ", minZ=" + minZ + ", maxX=" + maxX + ", maxY=" + maxY + ", maxZ=" + maxZ + "]";
    }
}
