package com.adjustclient.module.impl.movement.longjump;

public final class HypixelLongJumpMode extends LongJumpMode {
    public final FireballLongJumpTechnique fireball;
    public HypixelLongJumpTechnique technique;
    public HypixelLongJumpMode(LongJumpModule module) {
        super(module);
        fireball = new FireballLongJumpTechnique(this);
    }

    @Override public void onDisable() {
        technique.onDisable();
        technique = null;
    }

    @Override public void onEnable() {
        technique = fireball;
        technique.onEnable();
    }

    @Override public String getName() { return "Hypixel"; }
}
