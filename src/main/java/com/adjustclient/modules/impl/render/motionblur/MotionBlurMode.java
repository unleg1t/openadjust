package com.adjustclient.modules.impl.render.motionblur;

import com.adjustclient.core.settings.ModeOption;

public enum MotionBlurMode implements ModeOption {
    PHOSPHOR("Phosphor Blur"), MONKEY("Monkey Blur"), MOULBERRY("Moulberry Blur");
    private final String displayName;
    MotionBlurMode(String displayName) { this.displayName = displayName; }
    public String getName() { return displayName; }
}
