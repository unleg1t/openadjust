package com.adjustclient.module.impl.combat;

import com.adjustclient.module.Module;
import com.adjustclient.module.Category;

public class AimAssistModule extends Module {
    public static AimAssistModule INSTANCE;
    public AimAssistModule() { super("Aim Assist", Category.COMBAT); INSTANCE = this; }
}
