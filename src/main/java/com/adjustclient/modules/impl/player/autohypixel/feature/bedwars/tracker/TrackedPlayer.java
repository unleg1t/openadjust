package com.adjustclient.modules.impl.player.autohypixel.feature.bedwars.tracker;

import com.adjustclient.modules.impl.player.autohypixel.feature.bedwars.tracker.item.*;
import com.adjustclient.modules.impl.render.hud.playerlist.stats.BedWarsTeam;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public final class TrackedPlayer {
    public final com.adjustclient.utilities.anticheat.player.TabPlayer player;
    public final BedWarsTeam team;
    public TrackedEquipment<ToolTier> sword;
    public TrackedEquipment<ArmorTier> armor;
    public TrackedEquipment<ToolTier> axe;
    public TrackedEquipment<ToolTier> pickaxe;
    public ItemStack bow;
    public ItemStack shears;
    public TrackedItem fireball;
    public final TrackedItem[] recentItems = new TrackedItem[1];
    public ItemStack[] renderItems;

    public TrackedPlayer(com.adjustclient.utilities.anticheat.player.TabPlayer player, BedWarsTeam team) {
        this.player = player;
        this.team = team;
        this.sword = new TrackedEquipment<>(ToolTier.WOOD, new ItemStack(Items.wooden_sword));
        this.armor = new TrackedEquipment<>(ArmorTier.LEATHER, new ItemStack(Items.leather_boots));
    }

    public void rememberFireball(ItemStack stack) {
        fireball = new TrackedItem(stack);
        recentItems[0] = fireball;
    }
}
