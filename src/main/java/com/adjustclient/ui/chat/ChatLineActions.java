package com.adjustclient.ui.chat;

import com.adjustclient.Adjust;
import com.adjustclient.render.font.image.ImageLoader;
import com.adjustclient.render.font.image.NvgDraw;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiPersistentChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

public final class ChatLineActions {
    public static final ResourceLocation COPY;
    public static final List<String> COPY_HELP;
    public static final ResourceLocation DELETE;
    public static final List<String> DELETE_HELP;
    static {
        COPY = new ResourceLocation("adjust", "textures/chat/copy.png");
        DELETE = new ResourceLocation("adjust", "textures/chat/delete.png");
        COPY_HELP = List.of("§e§lCopy To Clipboard", "§b§lNORMAL CLICK§r §8- §7Full Message",
                "§b§lCTRL CLICK§r §8- §7Single Line", "", "§e§lModifiers", "§b§lALT§r §8- §7Formatting Codes");
        DELETE_HELP = List.of("§e§lDelete From History", "§b§lNORMAL CLICK§r §8- §7Full Message",
                "§b§lCTRL CLICK§r §8- §7Single Line");
    }
    public static float getActionsX(GuiPersistentChat chat) {
        Minecraft mc = chat.mc;
        float scale = mc.gameSettings.chatScale;
        int width = (int) Math.floor(mc.gameSettings.chatWidth * 280.0F + 40.0F);
        return ((float) Math.ceil((float) width / scale) + 5.0F) * scale + 2.0F;
    }
    public static boolean canDelete(GuiPersistentChat chat, ChatLine line) {
        return chat != Adjust.chatManager.globalChannel.history
                && chat.drawnChatLines.contains(line);
    }
    public static List<String> drawActions(GuiPersistentChat chat, int mouseX, int mouseY, int screenHeight) {
        int index = getLineIndex(chat, mouseX, mouseY, screenHeight);
        if (index < 0) return Collections.emptyList();
        ChatLine line = chat.getVisibleChatLines().get(index);
        float scale = chat.mc.gameSettings.chatScale;
        float left = getActionsX(chat);
        float top = screenHeight - 28.0F - 9 * (index - chat.scrollPos + 1) * scale;
        float size = 9.0F * scale;
        boolean deletable = canDelete(chat, line);
        float copyX = deletable ? left : left + 10.0F * scale;
        drawIcon(COPY, mouseX, mouseY, copyX, top, scale);
        if (deletable) {
            float deleteX = left + 10.0F * scale;
            drawIcon(DELETE, mouseX, mouseY, deleteX, top, scale);
            if (mouseX >= deleteX && mouseX < deleteX + size && mouseY >= top && mouseY < top + size) return DELETE_HELP;
        }
        if (mouseX >= copyX && mouseX < copyX + size && mouseY >= top && mouseY < top + size) return COPY_HELP;
        return Collections.emptyList();
    }
    public static boolean mouseClicked(GuiPersistentChat chat, int mouseX, int mouseY, int screenHeight, int button) {
        if (button != 0) return false;
        int index = getLineIndex(chat, mouseX, mouseY, screenHeight);
        if (index < 0) return false;
        ChatLine line = chat.getVisibleChatLines().get(index);
        float scale = chat.mc.gameSettings.chatScale;
        float left = getActionsX(chat);
        float top = screenHeight - 28.0F - 9 * (index - chat.scrollPos + 1) * scale;
        float size = 9.0F * scale;
        boolean deletable = canDelete(chat, line);
        float copyX = left + 10.0F * scale;
        if (deletable && mouseX >= copyX && mouseX < copyX + size && mouseY >= top && mouseY < top + size) {
            return chat.removeChatLine(line, GuiScreen.isCtrlKeyDown());
        }
        if (deletable) copyX = left;
        if (mouseX < copyX || mouseX >= copyX + size || mouseY < top || mouseY >= top + size) return false;
        IChatComponent component = GuiScreen.isCtrlKeyDown() ? line.lineString : line.messageComponent;
        String text = GuiScreen.isAltKeyDown() ? component.getFormattedText() : StringUtils.stripControlCodes(component.getUnformattedText());
        GuiScreen.setClipboardString(text);
        return true;
    }
    public static void drawIcon(ResourceLocation image, int mouseX, int mouseY, float x, float y, float scale) {
        float size = 9.0F * scale;
        boolean hovered = mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
        int alpha = (int) ((Minecraft.getMinecraft().gameSettings.chatOpacity * 0.9F + 0.1F) * 255.0F);
        NvgDraw.fillRect(hovered ? 0x80ffffff : 0x80000000, x, y, x + size, y + size);
        ImageLoader.getImage(image, 32).draw((alpha << 24) | 0xffffff, x, y, size, size);
    }
    public static int getLineIndex(GuiPersistentChat chat, int mouseX, int mouseY, int screenHeight) {
        if (chat == null || !(Minecraft.getMinecraft().currentScreen instanceof GuiChat)
                || Minecraft.getMinecraft().gameSettings.chatVisibility == net.minecraft.entity.player.EntityPlayer.EnumChatVisibility.HIDDEN) return -1;
        float scale = chat.mc.gameSettings.chatScale;
        if (scale <= 0.0F || Float.isInfinite(scale)) return -1;
        if (mouseX < 2 || mouseX >= getActionsX(chat) + scale * 20.0F) return -1;
        List<ChatLine> lines = chat.getVisibleChatLines();
        int row = (int) Math.ceil((screenHeight - 28.0F - mouseY) / (9.0F * scale)) - 1;
        int visible = Math.min(chat.getChatHeight() / 9, lines.size() - chat.scrollPos);
        if (row < 0 || row >= visible) return -1;
        int index = row + chat.scrollPos;
        return lines.get(index) == null ? -1 : index;
    }
}
