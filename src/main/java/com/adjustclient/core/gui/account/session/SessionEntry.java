package com.adjustclient.core.gui.account.session;

import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.managers.session.SessionStats;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.TimeUtil;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.util.ChatColor;

public final class SessionEntry implements MinecraftInstance, GuiListExtended.IGuiListEntry {
    public final SessionStats stats;

    public SessionEntry(SessionStats stats) {
        this.stats = stats;
    }

    @Override
    public void setSelected(int slotIndex, int x, int y) {
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
        boolean isCurrentSession = Minecraft.getMinecraft().getSession().getUsername().equals(this.stats.name);
        boolean isScreenSelected = mc.currentScreen instanceof SessionListScreen screen && screen.selectedEntry == this;

        String prefix = (isScreenSelected || isCurrentSession) ? ChatColor.BOLD.toString() : "";
        String nameText = prefix + this.stats.name;

        int nameColor;
        if (isScreenSelected) {
            Color animated = HUDModule.animatedColor;
            nameColor = animated != null ? animated.getRGB() : HUDModule.colorSetting.getColor().getRGB();
        } else if (isCurrentSession) {
            nameColor = new Color(220, 220, 220, 210).getRGB();
        } else {
            nameColor = new Color(190, 190, 195, 200).getRGB();
        }

        float centerX = (float) x + (float) listWidth / 2.0F;
        float nameWidth = mc.fontRendererObj.getStringWidth(nameText, 1.0F);
        mc.fontRendererObj.drawString(nameText, nameColor, true, centerX - nameWidth / 2.0F, (float) (y + 2), 1.0F);

        int detailColor = new Color(180, 185, 183, 255).getRGB();
        String lastUsedText = "last used " + ChatColor.WHITE + TimeUtil.timeAgo(this.stats.lastSeen, " ago");
        float lastUsedWidth = mc.fontRendererObj.getStringWidth(lastUsedText, 0.5F);
        mc.fontRendererObj.drawString(lastUsedText, detailColor, true, centerX - lastUsedWidth * 0.5F, (float) (y + 13), 0.5F);

        float kdr = this.stats.deaths == 0 ? (float) this.stats.kills : (float) this.stats.kills / (float) this.stats.deaths;
        String kdrFormatted = NumberSetting.FORMAT.format(kdr);
        String statsText = ChatColor.WHITE + kdrFormatted + ChatColor.RESET + " KDR, "
                + ChatColor.WHITE + this.stats.games + ChatColor.RESET + " game" + (this.stats.games == 1 ? "" : "s");
        float statsWidth = mc.fontRendererObj.getStringWidth(statsText, 0.5F);
        mc.fontRendererObj.drawString(statsText, detailColor, true, centerX - statsWidth * 0.5F, (float) y + 19.5F, 0.5F);
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        if (mc.currentScreen instanceof SessionListScreen screen) {
            screen.selectedEntry = this;
            SessionListScreen.selectedName = this.stats.name;
        }
        return false;
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
    }
}
