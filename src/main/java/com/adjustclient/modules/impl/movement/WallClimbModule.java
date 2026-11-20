package com.adjustclient.modules.impl.movement;

import com.adjustclient.modules.Module;
import com.adjustclient.modules.Category;

public final class WallClimbModule extends Module {
    public static WallClimbModule INSTANCE;
    public WallClimbModule() { super("WallClimb", Category.MOVEMENT); INSTANCE = this; }
}
