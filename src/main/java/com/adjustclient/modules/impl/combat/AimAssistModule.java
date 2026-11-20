package com.adjustclient.modules.impl.combat;

import com.adjustclient.modules.Module;
import com.adjustclient.modules.Category;

public class AimAssistModule extends Module {
    public static AimAssistModule INSTANCE;
    public AimAssistModule() { super("Aim Assist", Category.COMBAT); INSTANCE = this; }
}
