package com.adjustclient.core.gui.connection;

import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.TextComponent;
import java.awt.Color;
import net.minecraft.util.ChatColor;

class ConnectionStatusLine extends ConnectionStatusSegment {
    ConnectionStatusLine(String text) {
        this.components = new TextComponent[] {
            new TextComponent(ChatColor.BOLD + text, new Color(182, 180, 180, 255).getRGB())
        };
        this.font = Fonts.TAHOMA_12;
    }
}
