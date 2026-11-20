package com.adjustclient.modules.impl.player.inventory;

import com.adjustclient.modules.impl.player.inventory.action.HotbarSwapAction;
import com.adjustclient.modules.impl.player.inventory.action.InventoryAction;
import com.adjustclient.modules.impl.player.inventory.item.*;
import com.adjustclient.modules.impl.player.inventory.item.type.*;
import com.adjustclient.modules.impl.player.inventory.plan.*;
import com.adjustclient.core.settings.HotbarSetting;
import com.adjustclient.core.gui.clickgui.setting.hotbar.HotbarSettingComponent;
import java.util.Comparator;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;

public final class HotbarLayout {
    private final InventoryModule inventory;
    public final ItemType[] slotTypes;

    public HotbarLayout(InventoryModule inventory) {
        this.inventory = inventory;
        slotTypes = new ItemType[9];
        HotbarSetting setting = inventory.inventorySettings.layout;
        setting.addListener(() -> {
            ItemStack[] entries = inventory.inventorySettings.layout.getValue();
            for (int slot = 0; slot < entries.length; slot++) {
                ItemStack icon = entries[slot];
                if (icon == null) { slotTypes[slot] = null; continue; }
                Integer potionDamage = HotbarSettingComponent.POTIONS.get(icon.getItem());
                ItemStack representative = potionDamage == null ? new ItemStack(icon.getItem())
                        : new ItemStack(Items.potionitem, 1, Math.max(0, potionDamage));
                ItemType match = null;
                for (ItemType type : ItemTypes.ALL) {
                    if (type.matches(representative)) { match = type; break; }
                }
                if (match == null)
                    throw new IllegalArgumentException("Bad hotbar layout type " + 1 + ": " + icon.getItem().getItemStackDisplayName(icon));
                slotTypes[slot] = match;
            }
        });
        initializeDefaultLayout(setting);
    }

    public Integer findAssignedSlot(InventorySlot slot) {
        ItemStack stack = slot.itemStack;
        for (int index = 0; index < slotTypes.length; index++) {
            ItemType type = slotTypes[index];
            if (type == null || !type.matches(stack)) continue;
            if (type.sortKeyFunction == null) return index;
            InventorySnapshot snapshot = slot.inventory.snapshot;
            return stack == bestStack(snapshot, type) ? index : null;
        }
        return null;
    }

    private ItemStack bestStack(InventorySnapshot snapshot, ItemType type) {
        List<ItemStack> candidates = snapshot.getSlots(type, new Class<?>[0]);
        if (candidates.isEmpty()) return null;
        candidates.sort(Comparator.comparingDouble(stack -> {
            int score = type.sortKeyFunction.applyAsInt(stack);
            InventorySlot found = snapshot.findSlot(stack);
            if (found != null) {
                for (int index = 0; index < slotTypes.length; index++) {
                    if (slotTypes[index] == null || !slotTypes[index].equals(type)) continue;
                    var planner = found.inventory;
                    ItemStack existing = planner instanceof ArmorPlanner
                            ? net.minecraft.client.Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(index)
                            : planner.getStack(index);
                    if (existing != null && existing == stack) return (float) score - 0.1F;
                }
            }
            return score;
        }));
        if (type instanceof BucketItemType) System.out.println(candidates.get(0));
        return candidates.get(0);
    }

    public InventoryAction chooseSwap(InventorySlot slot, InventoryPlanner planner) {
        ItemStack stack = slot.itemStack;
        for (int index = 0; index < slotTypes.length; index++) {
            ItemType type = slotTypes[index];
            if (type == null || !type.matches(stack)) continue;
            InventorySnapshot snapshot = planner.snapshot;
            if (type.sortKeyFunction != null
                    && stack != bestStack(slot.inventory.snapshot, type)) return null;
            ItemStack existing = planner.getStack(index);
            if (existing != null && type.matches(existing)
                    && canReplace(snapshot, type, stack, existing)
                    && snapshot.keepChecker.isUseful(existing)) return null;
            return new HotbarSwapAction(slot, index);
        }
        return null;
    }

    public boolean canReplace(InventorySnapshot snapshot, ItemType type, ItemStack stack, ItemStack existing) {
        if (type.sortKeyFunction == null) return true;
        ItemStack best = bestStack(snapshot, type);
        if (best == null || existing == null) return true;
        return existing.getItem() != best.getItem() || existing.getItemDamage() != best.getItemDamage()
                || stack.getItem() == best.getItem() && stack.getItemDamage() == best.getItemDamage();
    }

    public boolean isAssigned(InventorySlot slot) {
        ItemStack stack = slot.itemStack;
        for (int index = 0; index < slotTypes.length; index++) {
            ItemType type = slotTypes[index];
            if (type == null || !type.matches(stack)) continue;
            var planner = slot.inventory;
            return type.sortKeyFunction != null
                    ? stack == bestStack(planner.snapshot, type)
                    : stack == planner.getStack(index);
        }
        return false;
    }

    private void initializeDefaultLayout(HotbarSetting setting) {
        ItemStack[] layout = setting.getValue();
        layout[0] = new ItemStack((Item) Items.diamond_sword);
        layout[1] = new ItemStack((Item) Items.diamond_pickaxe);
        layout[2] = new ItemStack((Item) Items.diamond_axe);
        layout[3] = new ItemStack((Item) Items.diamond_shovel);
        layout[4] = new ItemStack(Item.getItemFromBlock(Blocks.planks));
        layout[5] = HotbarSetting.getStack(11);
        layout[6] = new ItemStack((Item) Items.ender_pearl);
        layout[8] = new ItemStack((Item) Items.cooked_beef);
        setting.defaultHotbar = layout.clone();
    }
}
