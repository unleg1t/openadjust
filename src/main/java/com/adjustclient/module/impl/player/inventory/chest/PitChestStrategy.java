package com.adjustclient.module.impl.player.inventory.chest;

import com.adjustclient.module.impl.player.inventory.InventoryModule;
import com.adjustclient.module.impl.player.inventory.action.DropAction;
import com.adjustclient.module.impl.player.inventory.action.InventoryAction;
import com.adjustclient.module.impl.player.inventory.action.MoveAction;
import com.adjustclient.module.impl.player.inventory.action.ShiftClickAction;
import com.adjustclient.module.impl.player.inventory.plan.ArmorPlanner;
import com.adjustclient.module.impl.player.inventory.plan.InventorySlot;
import com.adjustclient.module.impl.player.inventory.plan.InventorySnapshot;
import com.adjustclient.util.MinecraftInstance;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatColor;

public final class PitChestStrategy extends ChestLootStrategy implements MinecraftInstance {
    public PitChestStrategy() {
    }

    public PitChestStrategy(InventoryModule inventory) {
    }

    @Override
    public InventoryAction chooseAction(InventorySlot slot) {
        if (!(slot.inventory instanceof ArmorPlanner) || Integer.compareUnsigned(slot.slotId, 9) < 0) {
            return null;
        }
        InventoryAction equipAction = chooseHelmetAction(slot);
        if (equipAction != null) {
            return equipAction;
        }
        if (shouldDrop(slot)) {
            return new DropAction(slot, Math.random() >= 0.36);
        }
        return null;
    }

    @Override
    public boolean canClose() {
        return true;
    }

    private boolean hasLeatherArmorInSameSlot(InventorySnapshot snapshot, Item item) {
        if (!(item instanceof ItemArmor armor)) {
            return false;
        }
        for (InventorySlot candidate : snapshot.slots) {
            if (candidate.itemStack.getItem() instanceof ItemArmor other
                    && other.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER
                    && other.armorType == armor.armorType) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDisplayNameContaining(ItemStack stack, String substring) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("display", 10)) {
            return false;
        }
        if (!stack.getTagCompound().getCompoundTag("display").hasKey("Name", 8)) {
            return false;
        }
        String stripped = ChatColor.getTextWithoutFormattingCodes(stack.getDisplayName());
        return stripped != null && stripped.contains(substring);
    }

    private boolean isPrimarySlotForItem(InventorySnapshot snapshot, InventorySlot target, Item item) {
        InventorySlot first = null;
        for (InventorySlot candidate : snapshot.slots) {
            if (candidate.inventory instanceof ArmorPlanner
                    && candidate.slotId >= 9
                    && candidate.itemStack.getItem() == item) {
                if (candidate.slotId >= 36 && candidate.slotId < 45) {
                    return candidate == target;
                }
                if (first == null) {
                    first = candidate;
                }
            }
        }
        return first == target;
    }

    private InventoryAction chooseHelmetAction(InventorySlot slot) {
        ItemStack stack = slot.itemStack;
        if (!(stack.getItem() instanceof ItemArmor armor)) {
            return null;
        }
        if (armor != Items.iron_helmet && !hasDisplayNameContaining(stack, "RNGesus Helmet")) {
            return null;
        }
        InventorySnapshot snapshot = slot.inventory.snapshot;
        if (!snapshot.autoArmor) {
            return null;
        }
        ItemStack equipped = mc.thePlayer.inventory.armorInventory[3 - armor.armorType];
        if (equipped == null) {
            return new ShiftClickAction(slot);
        }
        if (equipped.getItem() instanceof ItemArmor equippedArmor
                && equippedArmor.damageReduceAmount < armor.damageReduceAmount) {
            InventorySlot destination = snapshot.findSlot(equipped);
            return destination == null ? null : new MoveAction(slot, destination);
        }
        return null;
    }

    private boolean hasGoldenSwordForIronSword(InventorySnapshot snapshot, Item item) {
        if (item != Items.iron_sword) {
            return false;
        }
        for (InventorySlot candidate : snapshot.slots) {
            if (candidate.itemStack.getItem() == Items.golden_sword) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldDrop(InventorySlot slot) {
        ItemStack stack = slot.itemStack;
        Item item = stack.getItem();
        InventorySnapshot snapshot = slot.inventory.snapshot;
        if (hasDisplayNameContaining(stack, "Lucky Diamond")
                || hasDisplayNameContaining(stack, "RNGesus Helmet")) {
            return false;
        }
        if (item == Items.diamond_sword) {
            return !isPrimarySlotForItem(snapshot, slot, item);
        }
        boolean sameItemEquipped = false;
        if (item instanceof ItemArmor armor) {
            if (armor.getArmorMaterial() == ItemArmor.ArmorMaterial.CHAIN) {
                return true;
            }
            ItemStack equipped = mc.thePlayer.inventory.armorInventory[3 - armor.armorType];
            sameItemEquipped = equipped != null && equipped.getItem() == item;
            if (item == Items.iron_helmet) {
                return equipped != null
                        && equipped.getItem() instanceof ItemArmor equippedArmor
                        && armor.damageReduceAmount <= equippedArmor.damageReduceAmount;
            }
        }
        if (item == Items.diamond_leggings) {
            return sameItemEquipped || !isPrimarySlotForItem(snapshot, slot, item);
        }
        if (item == Items.diamond_chestplate || item == Items.diamond_boots) {
            return sameItemEquipped;
        }
        if (item != Items.iron_chestplate
                && item != Items.iron_leggings
                && item != Items.iron_boots
                && item != Items.iron_sword) {
            return false;
        }
        if (!snapshot.keepChecker.isUseful(stack)) {
            return true;
        }
        return hasLeatherArmorInSameSlot(snapshot, item) || hasGoldenSwordForIronSword(snapshot, item);
    }
}
