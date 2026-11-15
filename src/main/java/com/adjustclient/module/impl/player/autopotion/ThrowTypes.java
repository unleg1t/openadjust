package com.adjustclient.module.impl.player.autopotion;

import com.adjustclient.setting.BooleanSetting;

public final class ThrowTypes {
    public final BooleanSetting enabled;
    public final int[] potionIds;

    public ThrowTypes(BooleanSetting enabled, int... potionIds) {
        this.enabled = enabled;
        this.potionIds = potionIds;
    }
}
