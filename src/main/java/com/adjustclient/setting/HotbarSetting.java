package com.adjustclient.setting;

import com.adjustclient.util.ByteUtil;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;

public class HotbarSetting extends Setting<ItemStack[]> {
    public static final LinkedHashMap<Integer, ItemStack> PALETTE;
    public ItemStack[] defaultHotbar;
    private Object field52;

    static {
        PALETTE = new LinkedHashMap<>();
        PALETTE.put(0, new ItemStack((Item) Items.diamond_sword));
        PALETTE.put(1, new ItemStack((Item) Items.diamond_pickaxe));
        PALETTE.put(2, new ItemStack((Item) Items.diamond_axe));
        PALETTE.put(3, new ItemStack((Item) Items.diamond_shovel));
        PALETTE.put(4, new ItemStack((Item) Items.fishing_rod));
        PALETTE.put(5, new ItemStack(Items.bow));
        PALETTE.put(6, new ItemStack((Item) Items.ender_pearl));
        PALETTE.put(7, new ItemStack((Block) Blocks.stone));
        PALETTE.put(8, new ItemStack((Block) Blocks.planks));
        PALETTE.put(9, new ItemStack((Item) Items.diamond));
        PALETTE.put(10, new ItemStack((Item) Items.saddle));
        PALETTE.put(11, new ItemStack((Item) Items.dye));
        PALETTE.put(12, new ItemStack((Item) Items.map));
        PALETTE.put(13, new ItemStack((Item) Items.string));
        PALETTE.put(14, new ItemStack((Item) Items.cooked_beef));
        PALETTE.put(15, new ItemStack((Item) Items.water_bucket));
    }

    public HotbarSetting(SettingContainer parent, String name, ItemStack[] value, BooleanSupplier hideIf) {
        super(parent, name, value, hideIf);
        this.defaultHotbar = value.clone();
    }
    public HotbarSetting(SettingContainer parent, String name, ItemStack[] value) { this(parent, name, value, null); }

    public static ItemStack getStack(int id) { return PALETTE.get(id); }

    public void read(DataInputStream input) throws IOException {
        java.util.List<Item> seen = new java.util.ArrayList<>();
        ItemStack[] hotbar = getValue();
        for (int i = 0; i < hotbar.length; i++) {
            ItemStack stack = readStack(input);
            if (stack != null) {
                if (seen.contains(stack.getItem())) continue;
                seen.add(stack.getItem());
            }
            hotbar[i] = stack;
        }
        fireChanged();
    }

    public void write(DataOutputStream output) throws IOException {
        for (ItemStack stack : getValue()) ByteUtil.writeVarInt(output, stack == null ? -1 : getId(stack).intValue());
    }

    public static Integer getId(ItemStack stack) {
        return PALETTE.entrySet().stream()
                .filter(entry -> entry.getValue().getItem().equals(stack.getItem()))
                .findFirst()
                .map(entry -> entry.getKey())
                .orElse(null);
    }

    public static ItemStack readStack(DataInputStream input) throws IOException {
        int id = ByteUtil.readVarInt(input);
        return id == -1 ? null : getStack(id);
    }

    @Override public void reset() {
        if (defaultHotbar == null) return;
        System.arraycopy(defaultHotbar, 0, getValue(), 0, getValue().length);
        notifyListeners(getValue(), getValue());
    }

    public boolean isChanged() { return true; }
}

