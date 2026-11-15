package com.adjustclient.setting;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.function.BooleanSupplier;
import net.minecraft.block.Block;

public final class BlocksSetting extends ItemsSetting {
    public BlocksSetting(SettingContainer parent, String name, IntList value) { super(parent, name, value); }
    public BlocksSetting(SettingContainer parent, String name, IntList value, BooleanSupplier hideIf) {
        super(parent, name, value, hideIf);
    }

    public boolean contains(Block block) {
        int id = Block.getIdFromBlock(block);
        switch (id) {
            case 8: id = 9; break;
            case 10: id = 11; break;
            case 29:
            case 34:
            case 36: id = 33; break;
            case 43: id = 44; break;
            case 62: id = 61; break;
            case 68: id = 63; break;
            case 74: id = 73; break;
            case 75: id = 76; break;
            case 94: id = 93; break;
            case 97: id = 98; break;
            case 119: id = 120; break;
            case 125: id = 126; break;
            case 150: id = 149; break;
            case 162: id = 17; break;
            case 177: id = 176; break;
            case 178: id = 151; break;
            case 181: id = 182; break;
        }
        return getValue().contains(id);
    }
}
