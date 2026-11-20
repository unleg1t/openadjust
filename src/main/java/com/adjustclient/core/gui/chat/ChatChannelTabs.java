package com.adjustclient.core.gui.chat;

import com.adjustclient.Adjust;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.rendering.ColorUtil;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.core.settings.ColorSetting;
import com.adjustclient.core.gui.screen.ClickableRegion;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.ChatColor;

public final class ChatChannelTabs {
    public List<ClickableRegion<ChatChannel>> tabs = new ArrayList<>();
    public float x;
    public float y;
    public Object field16;

    public void rebuild() {
        tabs.clear();
        if (Adjust.chatManager == null) return;
        Adjust.chatManager.syncMinecraftHistory();
        for (ChatChannel channel : Adjust.chatManager.channels) tabs.add(new ClickableRegion<>(channel));
    }
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0) return false;
        for (ClickableRegion<ChatChannel> tab : tabs) {
            if (tab.isHovered(mouseX, mouseY)) {
                Adjust.chatManager.selectChannel((ChatChannel) tab.owner);
                return true;
            }
        }
        return false;
    }
    public void draw() {
        layout();
        for (ClickableRegion<ChatChannel> tab : tabs) {
            ChatChannel channel = (ChatChannel) tab.owner;
            NvgDraw.fillRect(0x80000000, tab.x, tab.y, tab.x + tab.width, tab.y + tab.height);
            float textX = tab.x + 2.5F;
            float textY = tab.y + 3.5F;
            String text = channel.name;
            if (channel == Adjust.chatManager.selected) {
                Color color = HUDModule.getHudColor();
                if (HUDModule.enableSecondary.isEnabled() && !HUDModule.enableSecondary.isHidden()
                        && HUDModule.colorSetting.getMode() == ColorSetting.Mode.PLAIN) {
                    Color secondary = ColorUtil.secondary(color);
                    for (int i = 0; i < text.length(); i++) {
                        String character = String.valueOf(text.charAt(i));
                        double ratio = Math.max(0.0, Math.min(1.0, ((float) i / text.length()) * 0.8F));
                        int argb = new Color(
                                (int) (secondary.getRed() * ratio + color.getRed() * (1.0 - ratio)),
                                (int) (secondary.getGreen() * ratio + color.getGreen() * (1.0 - ratio)),
                                (int) (secondary.getBlue() * ratio + color.getBlue() * (1.0 - ratio)),
                                (int) (secondary.getAlpha() * ratio + color.getAlpha() * (1.0 - ratio))).getRGB();
                        Fonts.TAHOMA_12.drawString(character, textX, textY, argb);
                        textX += Fonts.TAHOMA_12.getStringWidth(character);
                    }
                } else if (HUDModule.colorSetting.getMode() == ColorSetting.Mode.RAINBOW) {
                    for (int i = 0; i < text.length(); i++) {
                        String character = String.valueOf(text.charAt(i));
                        float hue = ((float) (System.currentTimeMillis() % 5000L)
                                + (5000.0F - (4 * i) * 30.0F)) / 5000.0F;
                        int argb = (Color.HSBtoRGB(hue, 0.45F, 0.8F) & 0xffffff) | (color.getAlpha() << 24);
                        Fonts.TAHOMA_12.drawString(character, textX, textY, argb);
                        textX += Fonts.TAHOMA_12.getStringWidth(character);
                    }
                } else {
                    Fonts.TAHOMA_12.drawString(text, textX, textY, color.getRGB());
                    textX += Fonts.TAHOMA_12.getStringWidth(text);
                }
            } else {
                Fonts.TAHOMA_12.drawString(text, textX, textY, 0xff888888);
                textX += Fonts.TAHOMA_12.getStringWidth(text);
            }
            int unread = channel.history != null ? channel.history.unreadMessages : 0;
            if (unread > 0) {
                String count = String.valueOf(ChatColor.BOLD) + unread;
                float badgeX = textX + 1.5F;
                float badgeY = tab.y + 2.0F;
                float countWidth = Fonts.TAHOMA_10.getStringWidth(count);
                NvgDraw.fillRoundedRect(0x96e95a5a, badgeX, badgeY,
                        textX + 3.5F + countWidth, tab.y + 8.0F, 1.5F);
                Fonts.TAHOMA_10.drawString(count, textX + 2.5F, tab.y + 4.25F, 0xbeffffff);
            }
        }
        NvgContext.flushFrame();
    }
    public void layout() {
        float cursor = x;
        for (ClickableRegion<ChatChannel> tab : tabs) {
            ChatChannel channel = (ChatChannel) tab.owner;
            String text = channel.name;
            float textWidth = Fonts.TAHOMA_12.getStringWidth(text);
            if (channel.history != null) {
                if (channel == Adjust.chatManager.selected) channel.history.unreadMessages = 0;
                else if (channel.history.unreadMessages > 0) {
                    textWidth += Fonts.TAHOMA_10.getStringWidth(String.valueOf(ChatColor.BOLD)
                            + channel.history.unreadMessages) + 3.0F;
                }
            }
            tab.x = cursor - 2.5F;
            tab.y = y - 2.0F - 1.5F;
            tab.width = textWidth + 5.0F;
            tab.height = Fonts.TAHOMA_12.size + 4.0F;
            cursor += textWidth + 7.0F;
        }
    }
}
