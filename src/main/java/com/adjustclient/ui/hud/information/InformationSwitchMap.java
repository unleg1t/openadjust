package com.adjustclient.ui.hud.information;

import net.minecraft.util.EnumFacing;

final class InformationSwitchMap {
    static final int[] $SwitchMap$net$minecraft$util$EnumFacing;

    static {
        $SwitchMap$net$minecraft$util$EnumFacing = new int[EnumFacing.values().length];
        $SwitchMap$net$minecraft$util$EnumFacing[EnumFacing.NORTH.ordinal()] = 1;
        $SwitchMap$net$minecraft$util$EnumFacing[EnumFacing.SOUTH.ordinal()] = 2;
        $SwitchMap$net$minecraft$util$EnumFacing[EnumFacing.WEST.ordinal()] = 3;
        $SwitchMap$net$minecraft$util$EnumFacing[EnumFacing.EAST.ordinal()] = 4;
    }
}
