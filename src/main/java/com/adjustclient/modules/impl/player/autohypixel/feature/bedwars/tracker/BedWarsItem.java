package com.adjustclient.modules.impl.player.autohypixel.feature.bedwars.tracker;

import java.util.function.Predicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemPotion;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatColor;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.Potion;

enum BedWarsItem {
    MACHINE_GUN_BOW("Machine Gun Bow", stack -> stack.getItem() == Items.bow && hasName(stack, "machine gun bow"), ChatColor.DARK_RED, Category.ROTATION, null),
    DEVASTATOR_BOW("Devastator Bow", stack -> stack.getItem() == Items.bow && hasName(stack, "devastator bow"), ChatColor.DARK_GREEN, Category.ROTATION, null),
    MIRACLE_OF_THE_STARS("Miracle of the Stars", stack -> stack.getItem() == Items.ender_pearl && hasName(stack, "miracle of the stars"), ChatColor.YELLOW, Category.ROTATION, null),
    UNSTABLE_TELEPORTATION_DEVICE("Unstable Teleportation Device", stack -> hasName(stack, "unstable teleportation device"), ChatColor.YELLOW, Category.ROTATION, null),
    MYSTIC_MIRROR("Mystic Mirror", stack -> hasName(stack, "mystic mirror"), ChatColor.LIGHT_PURPLE, Category.ROTATION, null),
    SLEEPING_DUST("Sleeping Dust", stack -> hasName(stack, "sleeping dust"), ChatColor.RED, Category.ROTATION, null),
    CHARLIE_THE_UNICORN("Charlie the Unicorn", stack -> stack.getItem() == Items.golden_horse_armor || hasName(stack, "charlie the unicorn"), ChatColor.LIGHT_PURPLE, Category.ROTATION, null),
    ICE_BRIDGE("Ice Bridge", stack -> (stack.getItem() == Item.getItemFromBlock(Blocks.ice) && stack.hasEffect()) || hasName(stack, "ice bridge"), ChatColor.AQUA, Category.ROTATION, null),
    BLOCK_ZAPPER("Block Zapper", stack -> stack.getItem() == Items.prismarine_shard || hasName(stack, "block zapper"), ChatColor.DARK_AQUA, Category.ROTATION, null),
    TIME_WARP_PEARL("Time Warp Pearl", stack -> stack.getItem() == Items.ender_pearl && stack.hasEffect(), ChatColor.DARK_AQUA, Category.ROTATION, null),
    MEGA_TNT("Mega TNT", stack -> stack.getItem() == Item.getItemFromBlock(Blocks.tnt) && stack.hasEffect(), ChatColor.DARK_RED, Category.ROTATION, null),
    WIND_CHARGE("Wind Charge", stack -> stack.getItem() == Items.snowball && stack.hasEffect(), ChatColor.WHITE, Category.ROTATION, null),
    ENCHANTED_SHEARS("Enchanted Shears", stack -> stack.getItem() == Items.shears && stack.hasEffect(), ChatColor.GREEN, Category.ROTATION, null),
    SHURIKEN("Shuriken", stack -> stack.getItem() == Items.prismarine_shard, ChatColor.YELLOW, Category.ROTATION, null),
    BRIDGE_ZAPPER("Bridge Zapper", stack -> stack.getItem() == Items.redstone, ChatColor.DARK_BLUE, Category.ROTATION, null),
    SUGAR_COOKIE("Sugar Cookie", stack -> stack.getItem() == Items.cookie, ChatColor.AQUA, Category.ROTATION, null),
    COBWEB("Cobweb", stack -> stack.getItem() == Item.getItemFromBlock(Blocks.web), ChatColor.WHITE, Category.ROTATION, null),
    TELEPORTATION_DEVICE("Teleportation Device", stack -> stack.getItem() == Item.getItemFromBlock(Blocks.end_portal_frame), ChatColor.DARK_GREEN, Category.ROTATION, null),
    MACE("Mace", stack -> stack.getItem() == Items.iron_axe, ChatColor.GOLD, Category.ROTATION, null),
    FINAL_REVIVE_BEACON("Final Revive Beacon", stack -> stack.getItem() == Item.getItemFromBlock(Blocks.beacon) && !stack.hasEffect(), ChatColor.DARK_RED, Category.ROTATION, null),
    DIAMOND_SWORD("Diamond Sword", stack -> stack.getItem() == Items.diamond_sword, ChatColor.AQUA, Category.GEAR, null),
    GOLDEN_PICKAXE("Golden Pickaxe", stack -> stack.getItem() == Items.golden_pickaxe, ChatColor.YELLOW, Category.GEAR, null),
    DIAMOND_PICKAXE("Diamond Pickaxe", stack -> stack.getItem() == Items.diamond_pickaxe, ChatColor.AQUA, Category.GEAR, null),
    GOLDEN_AXE("Golden Axe", stack -> stack.getItem() == Items.golden_axe, ChatColor.YELLOW, Category.GEAR, null),
    DIAMOND_AXE("Diamond Axe", stack -> stack.getItem() == Items.diamond_axe, ChatColor.AQUA, Category.GEAR, null),
    PUNCH_BOW("Punch Bow", stack -> stack.getItem() == Items.bow && stack.getEnchantmentTagList() != null && stack.getEnchantmentTagList().tagCount() == 2, ChatColor.DARK_GREEN, Category.GEAR, null),
    POWER_BOW("Power Bow", stack -> stack.getItem() == Items.bow && stack.getEnchantmentTagList() != null && stack.getEnchantmentTagList().tagCount() == 1, ChatColor.DARK_GREEN, Category.GEAR, null),
    BOW("Bow", stack -> stack.getItem() == Items.bow && (stack.getEnchantmentTagList() == null || stack.getEnchantmentTagList().tagCount() == 0), ChatColor.GREEN, Category.GEAR, null),
    CHAINMAIL_ARMOR("Chainmail Armor", stack -> stack.getItem() == Items.chainmail_boots, ChatColor.WHITE, Category.GEAR, null),
    IRON_ARMOR("Iron Armor", stack -> stack.getItem() == Items.iron_boots, ChatColor.WHITE, Category.GEAR, null),
    DIAMOND_ARMOR("Diamond Armor", stack -> stack.getItem() == Items.diamond_boots, ChatColor.AQUA, Category.GEAR, null),
    FIREBALL("Fireball", stack -> stack.getItem() == Items.fire_charge, ChatColor.GOLD, Category.UTILITY, null),
    ENDER_PEARL("Ender Pearl", stack -> stack.getItem() == Items.ender_pearl && !stack.hasEffect(), ChatColor.DARK_AQUA, Category.UTILITY, null),
    OBSIDIAN("Obsidian", stack -> stack.getItem() == Item.getItemFromBlock(Blocks.obsidian), ChatColor.DARK_PURPLE, Category.UTILITY, null),
    TNT("TNT", stack -> stack.getItem() == Item.getItemFromBlock(Blocks.tnt) && !stack.hasEffect(), ChatColor.RED, Category.UTILITY, null),
    BRIDGE_EGG("Bridge Egg", stack -> stack.getItem() == Items.egg, ChatColor.YELLOW, Category.UTILITY, null),
    MILK_BUCKET("Milk Bucket", stack -> stack.getItem() == Items.milk_bucket, ChatColor.WHITE, Category.UTILITY, null),
    WATER_BUCKET("Water Bucket", stack -> stack.getItem() == Items.water_bucket, ChatColor.BLUE, Category.UTILITY, null),
    KNOCKBACK_STICK("Knockback Stick", stack -> stack.getItem() == Items.stick, ChatColor.YELLOW, Category.UTILITY, null),
    BEDBUG("Bedbug", stack -> stack.getItem() == Items.snowball && !stack.hasEffect(), ChatColor.WHITE, Category.UTILITY, null),
    DREAM_DEFENDER("Dream Defender", stack -> (stack.getItem() == Items.spawn_egg && stack.getMetadata() != 93) || hasName(stack, "dream defender"), ChatColor.WHITE, Category.UTILITY, null),
    INVISIBILITY_POTION("Invisibility Potion", stack -> {
        if (!(stack.getItem() instanceof ItemPotion)) return false;
        var effects = ((ItemPotion) stack.getItem()).getEffects(stack);
        if (effects == null) return false;
        for (PotionEffect effect : effects) if (effect.getPotionID() == Potion.invisibility.getId()) return true;
        return false;
    }, ChatColor.WHITE, Category.UTILITY, null),
    JUMP_BOOST_POTION("Jump Boost Potion", stack -> {
        if (!(stack.getItem() instanceof ItemPotion)) return false;
        var effects = ((ItemPotion) stack.getItem()).getEffects(stack);
        if (effects == null) return false;
        for (PotionEffect effect : effects) if (effect.getPotionID() == Potion.jump.getId()) return true;
        return false;
    }, ChatColor.GREEN, Category.UTILITY, null),
    SPEED_POTION("Speed Potion", stack -> {
        if (!(stack.getItem() instanceof ItemPotion)) return false;
        var effects = ((ItemPotion) stack.getItem()).getEffects(stack);
        if (effects == null) return false;
        for (PotionEffect effect : effects) if (effect.getPotionID() == Potion.moveSpeed.getId()) return true;
        return false;
    }, ChatColor.AQUA, Category.UTILITY, null),
    SHARPENED_SWORDS("Sharpened Swords", null, ChatColor.AQUA, Category.UPGRADE, null),
    REINFORCED_ARMOR("Reinforced Armor", null, ChatColor.AQUA, Category.UPGRADE, null);

    final String displayName;
    final Predicate<ItemStack> matcher;
    final ChatColor color;
    final Category category;
    ItemStack icon;

    BedWarsItem(String displayName, Predicate<ItemStack> matcher, ChatColor color, Category category, ItemStack icon) {
        this.displayName = displayName;
        this.matcher = matcher;
        this.color = color;
        this.category = category;
        this.icon = icon;
    }

    static boolean hasName(ItemStack stack, String text) {
        return stack.hasDisplayName()
                && ChatColor.getTextWithoutFormattingCodes(stack.getDisplayName()).toLowerCase().contains(text);
    }

    static BedWarsItem fromStack(ItemStack stack) {
        for (BedWarsItem item : values()) {
            if (item.matcher != null && item.matcher.test(stack)) return item;
        }
        return null;
    }

    enum Category {
        GEAR, UTILITY, ROTATION, UPGRADE
    }
}
