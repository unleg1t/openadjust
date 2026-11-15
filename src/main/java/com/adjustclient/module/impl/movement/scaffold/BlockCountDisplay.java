package com.adjustclient.module.impl.movement.scaffold;

import net.minecraft.util.ChatColor;
final class BlockCountDisplay {

    /** The number as drawn text. */
    String countText;
    /** Width of {@link #countText}; the " block(s)" text starts this far right of the left edge. */
    float countWidth;
    /** Width of the whole centered text (used to center it horizontally). */
    float totalWidth;
    /** Number of blocks; {@code Integer.MAX_VALUE} in creative mode. */
    int count;
    /** Set when the HUD should fade out. */
    boolean hiding;

    BlockCountDisplay(int count) {
        this.count = count;
        this.countText = (ChatColor.BOLD == null ? "" : ChatColor.BOLD.toString())
                + (count == Integer.MAX_VALUE ? "inf" : Integer.toString(count));
        this.countWidth = com.adjustclient.render.font.Fonts.TAHOMA_10.getStringWidth(countText);
        this.totalWidth = countWidth + 17.0F;
    }
}
