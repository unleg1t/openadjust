package com.adjustclient.modules.impl.movement;

import com.adjustclient.modules.Category;
import com.adjustclient.modules.UtilityModule;

public final class MovementRecorderModule extends UtilityModule {
    public static MovementRecorderModule INSTANCE;
    private MovementRecorderModule() {
        super("Movement Recorder", Category.MOVEMENT);
        INSTANCE = this;
    }
}
