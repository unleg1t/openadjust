package com.adjustclient.modules.impl.render.esp;

import net.minecraft.init.Items;
public final class ItemValidator {
    public com.adjustclient.modules.impl.player.inventory.plan.InventorySnapshot snapshot;

    public boolean validate(net.minecraft.item.ItemStack stack) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.isSingleplayer()) return true;
        var state = com.adjustclient.Adjust.gameStateTracker.state;
        if (state.game == com.adjustclient.utilities.hypixel.HypixelGame.BED_WARS) return true;
        var game = state.game;
        if ((game.flags & 2) != 0) return true;
        if (game == com.adjustclient.utilities.hypixel.HypixelGame.MURDER_MYSTERY
                && state.startTimer != null) {
            return stack.getItem() == Items.gold_ingot;
        }
        if (snapshot == null) {
            var inventory = com.adjustclient.modules.impl.player.inventory.InventoryModule.INSTANCE;
            var created = new com.adjustclient.modules.impl.player.inventory.plan.InventorySnapshot(
                    inventory.hotbarLayout, inventory.inventorySettings);
            created.clicker = new com.adjustclient.modules.impl.player.inventory.click.InventoryClicker(created);
            created.keepChecker = new com.adjustclient.modules.impl.player.inventory.plan.ItemKeepChecker(created);
            snapshot = created;
            inventory.updateSnapshot(created, true);
        }
        for (var type : com.adjustclient.modules.impl.player.inventory.item.ItemTypes.ALL) {
            if (!type.matches(stack)) continue;
            java.util.Objects.requireNonNull(type.keepFilter);
            if (!(type.keepFilter instanceof com.adjustclient.modules.impl.player.inventory.item.filter.SimpleItemFilter)
                    && !(type.keepFilter instanceof com.adjustclient.modules.impl.player.inventory.item.filter.BestItemFilter)) {
                return false;
            }
            return type.keepFilter.isItemGood(type, stack, snapshot);
        }
        return false;
    }
}
