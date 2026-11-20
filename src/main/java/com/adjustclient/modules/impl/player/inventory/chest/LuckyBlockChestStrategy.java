package com.adjustclient.modules.impl.player.inventory.chest;
import com.adjustclient.modules.impl.player.inventory.InventoryModule;
import com.adjustclient.modules.impl.player.inventory.action.*;
import com.adjustclient.modules.impl.player.inventory.plan.*;
import com.adjustclient.utilities.MinecraftInstance;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import com.adjustclient.managers.config.user.UserConfiguration;
import com.adjustclient.modules.impl.player.inventory.item.ItemFilterMode;

public final class LuckyBlockChestStrategy extends ChestLootStrategy implements MinecraftInstance {
    private final InventoryModule inventory;
    public LuckyBlockChestStrategy(InventoryModule inventory) { this.inventory = inventory; }
    public InventoryAction chooseAction(InventorySlot slot) {
        var planner = slot.inventory;
        if (inventory.transferDirection == ItemFilterMode.UP) {
            if (!(planner instanceof ArmorPlanner)) return null;
        } else if (!(planner instanceof ItemPlanner)) return null;
        UserConfiguration.INSTANCE.timestamp |= 1L;
        ItemStack stack = slot.itemStack;
        Item item = stack.getItem();
        if (item == Items.emerald || item == Items.iron_ingot || item == Items.diamond || item == Items.gold_ingot)
            return new ShiftClickAction(slot);
        if (item == Items.skull && stack.hasDisplayName() && stack.getDisplayName().contains("Lucky Block"))
            return new ShiftClickAction(slot);
        if (!(item instanceof ItemSword)) return null;
        InventoryAction action = planner instanceof ItemPlanner
                ? ((ItemPlanner) planner).chooseAction(slot) : ((ArmorPlanner) planner).chooseAction(slot);
        return action == null || action instanceof MoveAction || action instanceof HotbarSwapAction
                ? action : new ShiftClickAction(slot);
    }
    public boolean canClose() {
        return inventory.transferDirection != null && mc.currentScreen instanceof GuiChest
                && inventory.inventorySettings.bedWarsAutoChest.isEnabled()
                && !inventory.inventorySettings.bedWarsAutoChest.isHidden();
    }
}
