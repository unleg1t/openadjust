package com.adjustclient.module.impl.movement;

import com.adjustclient.module.Category;
import com.adjustclient.module.UtilityModule;

public final class MovementRecorderModule extends UtilityModule {
    public static MovementRecorderModule INSTANCE;
    private MovementRecorderModule() {
        super("Movement Recorder", Category.MOVEMENT);
        INSTANCE = this;
    }
}
