package com.adjustclient.modules.impl.render.hud.playerlist.stats;

import java.awt.Color;
import com.adjustclient.modules.impl.render.hud.playerlist.StatValue;

public final class ColoredIntStat extends StatValue<Integer> {
    public ColoredIntStat(Integer value, String text) { super(value, text); this.value = value; }
    public ColoredIntStat(String text, Color color) { super(text, color); }
}
