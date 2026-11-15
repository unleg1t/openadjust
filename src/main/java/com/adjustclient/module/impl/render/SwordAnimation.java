package com.adjustclient.module.impl.render;
import com.adjustclient.setting.ModeOption;
public enum SwordAnimation implements ModeOption {
    NONE("None"), VANILLA("Vanilla"), BOUNCE("Bounce"), STASIS("Stasis"), HORIZONTAL("Horizontal"), DIG("Dig"), SHOVE("Shove"),
    TAP("Tap"), SLIDE("Slide"), ATTRACT("Attract"), DETRACT("Detract"), SWANK("Swank"), PUNCH("Punch");
    private final String name;
    SwordAnimation(String name) { this.name = name; }
    @Override public String getName() { return name; }
}
