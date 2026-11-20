package com.adjustclient.modules.impl.render;

import com.adjustclient.modules.Module;
import com.adjustclient.modules.Category;

public final class BetterScoreboardModule extends Module {
    public static BetterScoreboardModule INSTANCE;
    private BetterScoreboardModule(String name, Category category) { super(name, category); INSTANCE = this; }
}
