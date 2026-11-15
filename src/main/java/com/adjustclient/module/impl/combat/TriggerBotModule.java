package com.adjustclient.module.impl.combat;

import com.adjustclient.module.Category;
import com.adjustclient.module.Module;

public final class TriggerBotModule extends Module {
    public static TriggerBotModule INSTANCE;
    public TriggerBotModule() { super("Trigger Bot", Category.COMBAT); INSTANCE = this; }
}
