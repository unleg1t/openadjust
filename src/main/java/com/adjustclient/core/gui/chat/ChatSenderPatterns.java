package com.adjustclient.core.gui.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;

public final class ChatSenderPatterns {
    public static final Pattern[] SENDER_PATTERNS;
    public static final Pattern DISPLAY_NAME;
    static {
        SENDER_PATTERNS = new Pattern[] {
            Pattern.compile("^\\s*<([^\\s:\\[\\]<>]+)>\\s"),
            Pattern.compile("^\\s*(?:(?:Party|Guild|Officer|Co-op|Team|All)\\s*>\\s*)?(?:(?:From|To)\\s+)?(?:\\[[^]\\r\\n]+]\\s*)*([^\\s:\\[\\]<>]+)(?:\\s+\\[[^]\\r\\n]+])?\\s*:\\s", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^\\s*([^\\s:\\[\\]<>]+)\\s+whispers to you:\\s"),
            Pattern.compile("^\\s*You whisper to\\s+([^\\s:\\[\\]<>]+):\\s")
        };
        DISPLAY_NAME = Pattern.compile("^\\s*(?:\\[[^]\\r\\n]+]\\s*)*([^\\s:\\[\\]<>]+)(?:\\s+\\[[^]\\r\\n]+])?\\s*$");
    }
    public static void drawHead(NetworkPlayerInfo player, int x, int y, int alpha) {
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, (float) alpha / 255.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(
                com.google.common.base.Objects.firstNonNull(player.getLocationSkin(), DefaultPlayerSkin.getDefaultSkin(player.getGameProfile().getId())));
        Gui.drawScaledCustomSizeModalRect(x, y, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
        Gui.drawScaledCustomSizeModalRect(x, y, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
    public static NetworkPlayerInfo findSender(IChatComponent component) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || component == null || component.getChatStyle().colorSupplier != null) return null;
        String text = StringUtils.stripControlCodes(component.getFormattedText());
        String sender = null;
        for (Pattern pattern : SENDER_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) { sender = matcher.group(1); break; }
        }
        if (sender == null) return null;
        NetworkPlayerInfo info = mc.thePlayer.sendQueue.getPlayerInfo(sender);
        if (info != null) return info;
        for (NetworkPlayerInfo candidate : mc.thePlayer.sendQueue.getPlayerInfoMap()) {
            if (candidate.getDisplayName() == null) continue;
            Matcher matcher = DISPLAY_NAME.matcher(StringUtils.stripControlCodes(candidate.getDisplayName().getFormattedText()));
            if (matcher.matches() && matcher.group(1).equalsIgnoreCase(sender)) return candidate;
        }
        return null;
    }
}
