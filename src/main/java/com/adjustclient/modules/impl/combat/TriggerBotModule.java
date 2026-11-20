package com.adjustclient.modules.impl.combat;

import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;

public final class TriggerBotModule extends Module {
    public static TriggerBotModule INSTANCE;
    public TriggerBotModule() { super("Trigger Bot", Category.COMBAT); INSTANCE = this; }
}
