package com.adjustclient.module.impl.combat;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
public final class NoClickDelayModule extends Module {
    public static NoClickDelayModule INSTANCE;
    public NoClickDelayModule() { super("No Click Delay", Category.COMBAT); INSTANCE = this; setHidden(false); }
}
