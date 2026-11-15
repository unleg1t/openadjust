package com.adjustclient.module.impl.player.inventory.quickbuy;

public enum ShopCategory {
    QUICK_BUY("Quick Buy", 9),
    BLOCKS("Blocks", 10),
    MELEE("Melee", 11),
    ARMOR("Armor", 12),
    TOOLS("Tools", 13),
    RANGED("Ranged", 14),
    POTIONS("Potions", 15),
    UTILITY("Utility", 16),
    UPGRADES("Upgrades & Traps", -1);

    public final String title;
    public final int slot;
    ShopCategory(String title, int slot) { this.title = title; this.slot = slot; }
}
