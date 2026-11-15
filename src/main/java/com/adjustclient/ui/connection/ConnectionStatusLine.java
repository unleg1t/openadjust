package com.adjustclient.ui.connection;

import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.TextComponent;
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
