package com.adjustclient.modules.impl.player.autopotion;

import com.adjustclient.core.settings.BooleanSetting;

public final class ThrowTypes {
    public final BooleanSetting enabled;
    public final int[] potionIds;

    public ThrowTypes(BooleanSetting enabled, int... potionIds) {
        this.enabled = enabled;
        this.potionIds = potionIds;
    }
}
