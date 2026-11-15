package com.adjustclient.module.impl.movement;

import com.adjustclient.module.Module;
import com.adjustclient.module.Category;

public final class WallClimbModule extends Module {
    public static WallClimbModule INSTANCE;
    public WallClimbModule() { super("WallClimb", Category.MOVEMENT); INSTANCE = this; }
}
