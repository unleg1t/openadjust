package com.adjustclient.module.impl.render;

import com.adjustclient.module.Module;
import com.adjustclient.module.Category;

public final class BetterScoreboardModule extends Module {
    public static BetterScoreboardModule INSTANCE;
    private BetterScoreboardModule(String name, Category category) { super(name, category); INSTANCE = this; }
}
