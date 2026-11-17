package com.adjustclient.game.rewound;

import com.adjustclient.event.ScoreboardRenderEvent;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

public final class PitStreakScoreboard implements ScoreboardRenderEvent.Overlay {
    public final String title;
    public final List<String> lines;

    public PitStreakScoreboard(String title, List<String> lines) {
        this.title = title;
        this.lines = lines;
    }

    @Override
    public float getWidth() {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        float widest = font.getStringWidth(title);
        for (String line : lines) {
            widest = Math.max(widest, font.getStringWidth(line));
        }
        return widest;
    }

    @Override
    public void render(ScaledResolution scaledResolution, float width, boolean transparent, float bottomY) {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        int boxWidth = (int) width;
        int left = scaledResolution.getScaledWidth() - boxWidth - 3;
        int right = scaledResolution.getScaledWidth() - 1;
        int top = (int) bottomY + 4;
        int titleHeight = font.FONT_HEIGHT;
        if (!transparent) {
            Gui.drawRect(left - 2, top, right, top + titleHeight, 0x60000000);
            Gui.drawRect(left - 2, top + titleHeight, right, top + titleHeight + 1, 0x50000000);
        }
        font.drawString(title, left + boxWidth / 2 - (int) font.getStringWidth(title) / 2, top + 1, 0x20FFFFFF);
        int y = top + titleHeight + 1;
        for (String line : lines) {
            if (!transparent) {
                Gui.drawRect(left - 2, y, right, y + titleHeight, 0x50000000);
            }
            font.drawString(line, left, y, 0x20FFFFFF);
            y += titleHeight;
        }
    }
}
