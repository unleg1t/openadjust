package com.adjustclient.core.gui.creativetab;

import com.adjustclient.utilities.WorldUtil;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

public final class HousingCreativeTab extends CreativeTabs {
    public HousingCreativeTab(int index, String label) { super(index, label); }

    @Override public int getTabColumn() { return 6; }

    @Override public void displayAllReleventItems(List<ItemStack> items) {
        try {
            ItemStack menu = new ItemStack((Item) Items.ghast_tear, 1, 0);
            menu.setTagCompound(JsonToNBT.getTagFromJson("{HideFlags:254,display:{Name:\"\u00a7aHousing Menu\u00a77 (Right Click)\"},ExtraAttributes:{HOUSING_MENU:\"TRUSTED\"}}"));
            items.add(menu);
            ItemStack teleport = new ItemStack((net.minecraft.block.Block) Blocks.end_portal_frame, 1, 0);
            teleport.setTagCompound(JsonToNBT.getTagFromJson("{overrideMeta:1b,display:{Lore:[0:\"\u00a77Place this block in your house\",1:\"\u00a77to place a Teleport Pad!\"],Name:\"\u00a7aTeleport Pad\"},AttributeModifiers:[]}"));
            items.add(teleport);
            ItemStack launch = new ItemStack((net.minecraft.block.Block) Blocks.slime_block, 1, 0);
            launch.setTagCompound(JsonToNBT.getTagFromJson("{overrideMeta:1b,display:{Lore:[0:\"\u00a77Place this block in your house\",1:\"\u00a77to place a Launch Pad!\"],Name:\"\u00a7aLaunch Pad\"},AttributeModifiers:[]}"));
            items.add(launch);
            ItemStack parkour = new ItemStack((net.minecraft.block.Block) Blocks.light_weighted_pressure_plate, 1, 0);
            parkour.setTagCompound(JsonToNBT.getTagFromJson("{overrideMeta:1b,display:{Lore:[0:\"\u00a77Place this block in your house\",1:\"\u00a77to add a checkpoint to your\",2:\"\u00a77parkour!\"],Name:\"\u00a7aParkour Block\"},AttributeModifiers:[]}"));
            items.add(parkour);
            ItemStack actionPad = new ItemStack((net.minecraft.block.Block) Blocks.heavy_weighted_pressure_plate, 1, 0);
            WorldUtil.applyNbtJson(actionPad, "{display:{Lore:[0:\"\u00a77Place this block in your house\",1:\"\u00a77to place an Action Pad!\"],Name:\"\u00a7aAction Pad\"}}");
            items.add(actionPad);
            ItemStack hologram = new ItemStack((Item) Items.name_tag, 1, 0);
            hologram.setTagCompound(JsonToNBT.getTagFromJson("{overrideMeta:1b,display:{Lore:[0:\"\u00a77Place this in your house to\",1:\"\u00a77place a Hologram!\"],Name:\"\u00a7aHologram\"},AttributeModifiers:[]}"));
            items.add(hologram);
            ItemStack npc = new ItemStack((Item) Items.skull, 1, 0);
            npc.setTagCompound(JsonToNBT.getTagFromJson("{display:{Lore:[0:\"\u00a77Place this in your house to\",1:\"\u00a77place an NPC!\"],Name:\"\u00a7aNPC\"}}"));
            items.add(npc);
            ItemStack actionButton = new ItemStack((net.minecraft.block.Block) Blocks.stone_button, 1, 0);
            WorldUtil.applyNbtJson(actionButton, "{overrideMeta:1b,display:{Lore:[0:\"\u00a77Place this block in your house\",1:\"\u00a77to place an Action Button!\"],Name:\"\u00a7aAction Button\"},AttributeModifiers:[]}");
            items.add(actionButton);
            ItemStack statLeaderboard = new ItemStack((Item) Items.book, 1, 0);
            statLeaderboard.setTagCompound(JsonToNBT.getTagFromJson("{display:{Lore:[0:\"\u00a77Place this in your house to\",1:\"\u00a77place a Stat Leaderboard!\"],Name:\"\u00a7aStat Leaderboard\"}}"));
            items.add(statLeaderboard);
            ItemStack parkourLeaderboard = new ItemStack((Item) Items.book, 1, 0);
            WorldUtil.applyNbtJson(parkourLeaderboard, "{display:{Lore:[0:\"\u00a77Place this in your house to\",1:\"\u00a77place a parkour leaderboard!\"],Name:\"\u00a7aParkour Leaderboard\"}}");
            items.add(parkourLeaderboard);
            ItemStack biome = new ItemStack((Item) Items.stick, 1, 0);
            biome.setTagCompound(JsonToNBT.getTagFromJson("{ench:[],display:{Lore:[0:\"\u00a77Allows you to change the Biome\",1:\"\u00a77at the \u00a7atargeted block\u00a77!\",2:\"\",3:\"\u00a77Biome: \u00a7eDesert\",4:\"\u00a77Radius: \u00a7e3\",5:\"\",6:\"\u00a7eLeft-Click to change settings!\",7:\"\u00a7eRight-Click to use!\"],Name:\"\u00a7aBiome Stick\u00a77 - \u00a7eDesert\u00a77 (3)\"},ExtraAttributes:{data:{biomeType:\"DESERT\",radius:3},ITEM_ID:\"BIOME_STICK\"}}"));
            items.add(biome);
            ItemStack potion = new ItemStack((Item) Items.potionitem, 1, 16385);
            potion.setTagCompound(JsonToNBT.getTagFromJson("{CustomPotionEffects:[{Id:15,Amplifier:2,Duration:1000000},{Id:2,Amplifier:2,Duration:1000000},{Id:9,Amplifier:2,Duration:1000000},{Id:19,Amplifier:2,Duration:1000000},{Id:20,Amplifier:2,Duration:1000000},{Id:18,Amplifier:2,Duration:1000000},{Id:17,Amplifier:2,Duration:1000000},{Id:4,Amplifier:2,Duration:1000000}]}"));
            items.add(potion);
        } catch (net.minecraft.nbt.NBTException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public String getTranslatedTabLabel() { return "Housing"; }

    @Override public Item getTabIconItem() { return (Item) Items.oak_door; }
}
