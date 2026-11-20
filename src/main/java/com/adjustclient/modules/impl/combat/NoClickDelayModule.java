package com.adjustclient.modules.impl.combat;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
public final class NoClickDelayModule extends Module {
    public static NoClickDelayModule INSTANCE;
    public NoClickDelayModule() { super("No Click Delay", Category.COMBAT); INSTANCE = this; setHidden(false); }
}
