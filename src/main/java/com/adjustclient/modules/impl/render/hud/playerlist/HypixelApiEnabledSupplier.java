package com.adjustclient.modules.impl.render.hud.playerlist;

import java.util.function.BooleanSupplier;

final class HypixelApiEnabledSupplier implements BooleanSupplier {

    private final PlayerList owner;

    HypixelApiEnabledSupplier(PlayerList owner) {
        this.owner = owner;
    }

    @Override
    public boolean getAsBoolean() {
        return this.owner.hypixelApiEnabled.isEnabled();
    }
}
