package com.adjustclient.modules.impl.render.hud.playerlist.menu;

import com.adjustclient.modules.impl.render.hud.playerlist.stats.PlayerEntry;
import java.util.function.BiPredicate;

public final class MenuGroup {
    public String title;
    public MenuItem[] items;
    public BiPredicate<PlayerEntry, Object> field12;
    public float x;
    public float y;
    public boolean visible;
    public boolean expanded;
    public MenuGroup(String title, MenuItem... items) { this(title, null, items); }
    public MenuGroup(String title, BiPredicate<PlayerEntry, Object> hideIf, MenuItem... items) {
        this.title = title; this.field12 = hideIf; this.items = items;
    }
}
