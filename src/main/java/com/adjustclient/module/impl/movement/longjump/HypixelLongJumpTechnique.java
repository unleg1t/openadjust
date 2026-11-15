package com.adjustclient.module.impl.movement.longjump;

public abstract class HypixelLongJumpTechnique extends LongJumpMode {
    protected final HypixelLongJumpMode hypixel;
    protected HypixelLongJumpTechnique(HypixelLongJumpMode hypixel) {
        super(hypixel.module);
        this.hypixel = hypixel;
    }

    @Override public boolean isActive() {
        return hypixel.module.isEnabled() && hypixel.module.mode.getValue() == hypixel
                && hypixel.technique == this;
    }
}
