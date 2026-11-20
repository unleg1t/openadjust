package com.adjustclient.modules.impl.player.autohypixel.feature.bedwars.tracker.item;

import net.minecraft.item.Item;

public enum ToolTier {
    WOOD, STONE, IRON, GOLD, DIAMOND;

    public static ToolTier fromMaterial(Item.ToolMaterial material) {
        return switch (material) {
            case WOOD -> WOOD;
            case STONE -> STONE;
            case IRON -> IRON;
            case EMERALD -> DIAMOND;
            case GOLD -> GOLD;
        };
    }
}
