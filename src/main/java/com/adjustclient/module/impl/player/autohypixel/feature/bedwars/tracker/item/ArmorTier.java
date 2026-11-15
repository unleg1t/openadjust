package com.adjustclient.module.impl.player.autohypixel.feature.bedwars.tracker.item;

import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import net.minecraft.item.ItemArmor;

public enum ArmorTier {
    LEATHER, CHAIN, IRON, DIAMOND;
    private static boolean warnedAboutGold;

    public static ArmorTier fromMaterial(ItemArmor.ArmorMaterial material) {
        return switch (material) {
            case LEATHER -> LEATHER;
            case CHAIN -> CHAIN;
            case IRON -> IRON;
            case DIAMOND -> DIAMOND;
            case GOLD -> {
                if (!warnedAboutGold) {
                    warnedAboutGold = true;
                    Logger.log(LogLevel.WARN, true, "Somebody has golden armor and this shouldn't be possible");
                }
                yield IRON;
            }
        };
    }
}
