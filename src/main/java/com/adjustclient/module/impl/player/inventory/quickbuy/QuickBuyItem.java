package com.adjustclient.module.impl.player.inventory.quickbuy;

import java.util.function.Predicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;

public enum QuickBuyItem {
    WOOL("wool", "Wool", ShopCategory.BLOCKS, Item.getItemFromBlock(Blocks.wool)),
    LADDER("ladder", "Ladder", ShopCategory.BLOCKS, Item.getItemFromBlock(Blocks.ladder)),
    STONE_SWORD("stone_sword", "Stone Sword", ShopCategory.MELEE, (Item) Items.stone_sword),
    IRON_SWORD("iron_sword", "Iron Sword", ShopCategory.MELEE, (Item) Items.iron_sword),
    DIAMOND_SWORD("diamond_sword", "Diamond Sword", ShopCategory.MELEE, (Item) Items.diamond_sword),
    KNOCKBACK_STICK("knockback_stick", "Knockback Stick", ShopCategory.MELEE, (Item) Items.stick),
    CHAINMAIL_ARMOR("chainmail_armor", "Chainmail Armor", ShopCategory.ARMOR, (Item) Items.chainmail_boots),
    IRON_ARMOR("iron_armor", "Iron Armor", ShopCategory.ARMOR, (Item) Items.iron_boots),
    DIAMOND_ARMOR("diamond_armor", "Diamond Armor", ShopCategory.ARMOR, (Item) Items.diamond_boots),
    PICKAXE("pickaxe", "Pickaxe", ShopCategory.TOOLS, QuickBuyItem::isPickaxe),
    AXE("axe", "Axe", ShopCategory.TOOLS, QuickBuyItem::isAxe),
    SHEARS("shears", "Shears", ShopCategory.TOOLS, (Item) Items.shears),
    ARROWS("arrows", "Arrows", ShopCategory.RANGED, Items.arrow),
    INVISIBILITY_POTION("invisibility_potion", "Invisibility Potion", ShopCategory.POTIONS, "Invisibility"),
    JUMP_BOOST_POTION("jump_boost_potion", "Jump Boost Potion", ShopCategory.POTIONS, "Jump"),
    SPEED_POTION("speed_potion", "Speed Potion", ShopCategory.POTIONS, "Speed"),
    GOLDEN_APPLE("golden_apple", "Golden Apple", ShopCategory.UTILITY, (Item) Items.golden_apple),
    FIREBALL("fireball", "Fireball", ShopCategory.UTILITY, (Item) Items.fire_charge),
    TNT("tnt", "TNT", ShopCategory.UTILITY, Item.getItemFromBlock(Blocks.tnt)),
    ENDER_PEARL("ender_pearl", "Ender Pearl", ShopCategory.UTILITY, (Item) Items.ender_pearl),
    BRIDGE_EGG("bridge_egg", "Bridge Egg", ShopCategory.UTILITY, (Item) Items.egg),
    MILK_BUCKET("milk_bucket", "Milk Bucket", ShopCategory.UTILITY, (Item) Items.milk_bucket),
    WATER_BUCKET("water_bucket", "Water Bucket", ShopCategory.UTILITY, (Item) Items.water_bucket),
    SHARPNESS("upgrade_sharpness", "Sharpness", ShopCategory.UPGRADES, (Item) Items.iron_sword),
    PROTECTION("upgrade_protection", "Protection", ShopCategory.UPGRADES, (Item) Items.iron_chestplate),
    MINER_FATIGUE("upgrade_miner_fatigue", "Miner Fatigue", ShopCategory.UPGRADES, (Item) Items.iron_pickaxe),
    MANIAC_MINER("upgrade_maniac_miner", "Maniac Miner", ShopCategory.UPGRADES, (Item) Items.golden_pickaxe),
    IRON_FORGE("upgrade_iron_forge", "Iron Forge", ShopCategory.UPGRADES, Item.getItemFromBlock(Blocks.furnace)),
    FEATHER_FALLING("upgrade_feather_falling", "Feather Falling", ShopCategory.UPGRADES, (Item) Items.diamond_boots);

    public final String id;
    public final String displayName;
    public final ShopCategory category;
    public final Predicate<ItemStack> matcher;

    QuickBuyItem(String id, String displayName, ShopCategory category, Predicate<ItemStack> matcher) {
        this.id = id; this.displayName = displayName; this.category = category; this.matcher = matcher;
    }
    QuickBuyItem(String id, String displayName, ShopCategory category, Item item) {
        this(id, displayName, category, stack -> stack != null && stack.getItem() == item);
    }
    QuickBuyItem(String id, String displayName, ShopCategory category, String name) {
        this(id, displayName, category, stack -> {
            return stack != null && stack.hasTagCompound()
                    && stack.getTagCompound().hasKey("display", 10)
                    && stack.getTagCompound().getCompoundTag("display").hasKey("Name", 8)
                    && stack.getDisplayName().contains(name);
        });
    }

    private static boolean isAxe(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.diamond_axe || item == Items.iron_axe
                || item == Items.stone_axe || item == Items.wooden_axe;
    }

    private static boolean isPickaxe(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.diamond_pickaxe || item == Items.golden_pickaxe
                || item == Items.iron_pickaxe || item == Items.wooden_pickaxe;
    }
}
