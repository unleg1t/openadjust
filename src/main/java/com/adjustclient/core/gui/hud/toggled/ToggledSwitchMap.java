package com.adjustclient.core.gui.hud.toggled;

final class ToggledSwitchMap {
    static final int[] $SwitchMap$Border = new int[3];
    static final int[] $SwitchMap$Color = new int[4];

    static {
        $SwitchMap$Border[ToggledBorder.FULL.ordinal()] = 1;
        $SwitchMap$Border[ToggledBorder.RIGHT.ordinal()] = 2;
        $SwitchMap$Color[ToggledColor.DEFAULT.ordinal()] = 1;
        $SwitchMap$Color[ToggledColor.RAINBOW.ordinal()] = 2;
        $SwitchMap$Color[ToggledColor.ASTOLFO.ordinal()] = 3;
        $SwitchMap$Color[ToggledColor.RANDOM.ordinal()] = 4;
    }
}
