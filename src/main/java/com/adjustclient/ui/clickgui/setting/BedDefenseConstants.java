package com.adjustclient.ui.clickgui.setting;

import com.adjustclient.module.impl.combat.builder.feature.beddefense.BedDefenseBlockType;
final class BedDefenseConstants {
    static final int[] $SwitchMap$Block = new int[BedDefenseBlockType.values().length];

    static {
        $SwitchMap$Block[BedDefenseBlockType.WOOL.ordinal()] = 1;
        $SwitchMap$Block[BedDefenseBlockType.GLASS.ordinal()] = 2;
        $SwitchMap$Block[BedDefenseBlockType.WOOD.ordinal()] = 3;
        $SwitchMap$Block[BedDefenseBlockType.END_STONE.ordinal()] = 4;
        $SwitchMap$Block[BedDefenseBlockType.CLAY.ordinal()] = 5;
        $SwitchMap$Block[BedDefenseBlockType.OBSIDIAN.ordinal()] = 6;
    }
}
