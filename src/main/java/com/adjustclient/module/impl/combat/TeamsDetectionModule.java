package com.adjustclient.module.impl.combat;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
public final class TeamsDetectionModule extends Module {
    public static TeamsDetectionModule INSTANCE;
    public TeamsDetectionModule() { super("Teams Detection", Category.COMBAT); INSTANCE = this; setHidden(false); toggle(true); }
    @Override public String getDescription() { return "When enabled, teammates will be filtered as friendly."; }
    @Override public String getAdditionalDescription() { return "This module may be unreliable on unsupported servers."; }
}
