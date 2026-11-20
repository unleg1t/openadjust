package com.adjustclient.modules.impl.player.autopotion;

public final class PotionCandidate {
    public final int potionId;
    public final int amplifier;
    public final int slot;

    public PotionCandidate(int potionId, int amplifier, int slot) {
        this.potionId = potionId;
        this.amplifier = amplifier;
        this.slot = slot;
    }
}
