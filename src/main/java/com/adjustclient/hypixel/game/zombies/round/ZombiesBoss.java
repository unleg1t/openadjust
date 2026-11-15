package com.adjustclient.hypixel.game.zombies.round;

import java.util.function.Supplier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.ChatColor;

public enum ZombiesBoss {
    NONE("None", null),
    BLAZE("Blaze", ZombiesBoss::blazeIcon),
    SLIME("Slime", ZombiesBoss::slimeIcon),
    BOMBIE(ChatColor.RED.toString() + ChatColor.BOLD + "Bombie", ZombiesBoss::bombieIcon),
    INFERNO(ChatColor.RED.toString() + ChatColor.BOLD + "Inferno", ZombiesBoss::infernoIcon),
    BROODMOTHER(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Broodmother", ZombiesBoss::broodmotherIcon),
    KING_SLIME(ChatColor.GREEN.toString() + ChatColor.BOLD + "King Slime", ZombiesBoss::kingSlimeIcon),
    WITHER(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Wither", ZombiesBoss::witherIcon),
    HEROBRINE(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Herobrine", ZombiesBoss::herobrineIcon),
    MEGA_BLOB(ChatColor.GREEN.toString() + ChatColor.BOLD + "Mega Blob", ZombiesBoss::megaBlobIcon),
    MEGA_MAGMA(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Mega Magma", () -> new ItemStack(Items.magma_cream, 1, 0)),
    WORLD_ENDER(ChatColor.BLACK.toString() + ChatColor.BOLD + "World Ender", ZombiesBoss::worldEnderIcon),
    ANGRY_PRISONER(ChatColor.RED.toString() + ChatColor.BOLD + "Angry Prisoner", () -> new ItemStack(Items.redstone, 1, 0)),
    CORRUPTED_PIGMAN(ChatColor.RED.toString() + ChatColor.BOLD + "Corrupted Pigman", () -> new ItemStack(Items.cooked_porkchop, 1, 0)),
    WARDEN(ChatColor.GOLD.toString() + ChatColor.BOLD + "Warden", () -> new ItemStack(Items.ender_eye, 1, 0));
    public final String displayName;
    public final Supplier<ItemStack> iconSupplier;
    public Object field24;
    ZombiesBoss(String displayName, Supplier<ItemStack> iconSupplier) { this.displayName = displayName; this.iconSupplier = iconSupplier; }
    private static ItemStack herobrineIcon() { return new ItemStack(Items.ender_eye, 1, 0); }
    private static ItemStack witherIcon() { return new ItemStack(Items.skull, 1, 1); }
    private static ItemStack blazeIcon() { return new ItemStack(Items.blaze_rod, 1, 0); }
    private static ItemStack bombieIcon() { return new ItemStack(Item.getItemFromBlock(Blocks.tnt)); }
    private static ItemStack slimeIcon() { return new ItemStack(Items.slime_ball, 1, 0); }
    private static ItemStack megaBlobIcon() { return new ItemStack(Items.slime_ball, 1, 0); }
    private static ItemStack infernoIcon() { return new ItemStack(Items.blaze_powder, 1, 0); }
    private static ItemStack broodmotherIcon() { return new ItemStack(Items.ender_eye, 1, 0); }
    private static ItemStack worldEnderIcon() { return new ItemStack(Items.nether_star, 1, 0); }
    private static ItemStack kingSlimeIcon() { return new ItemStack(Items.slime_ball, 1, 0); }
}
