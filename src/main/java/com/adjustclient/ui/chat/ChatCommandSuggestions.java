package com.adjustclient.ui.chat;

import com.adjustclient.command.CommandBase;
import com.adjustclient.property.CommandManager;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.util.ChatMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiPersistentChat;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.ChatColor;
import net.minecraft.util.IChatComponent;

public final class ChatCommandSuggestions {
    public static Slot819b slot819b;
    public static final class Slot819b { public Field12 field12; }
    public static final class Field12 { public String field4; }
    public CommandManager commands;
    public CommandBase current;
    public String alias;
    public Object field16;
    public Object field20;

    public static void appendChatSeparator(ChatMessage message) {
        Minecraft mc = Minecraft.getMinecraft();
        int width = (int) Math.floor(mc.gameSettings.chatWidth * 280.0F + 40.0F);
        int count = (int) (width / (float) mc.fontRendererObj.getStringWidth("-"));
        if (count < 1) return;
        int section = count >>> 2;
        for (int i = 0; i < count; i++) {
            message.color = i % section == 0 ? ChatColor.WHITE : ChatColor.GRAY;
            message.append("-");
        }
    }
    public String getLabel(String text) {
        if (text == null || text.isEmpty()) return "";
        String label = text.substring(1);
        int space = label.indexOf(' ');
        return space == -1 ? label : label.substring(0, space);
    }
    public void updateInput(GuiChat screen) {
        String text = screen.inputField.getText();
        int limit = 100;
        if (commands.isCommand(text)) {
            int cursor = screen.inputField.getCursorPosition();
            String beforeCursor = cursor > 0 && cursor < text.length() ? text.substring(0, cursor) : text;
            current = findCommand(beforeCursor);
            if (current != null) {
                alias = current.aliases[0];
                limit = current.getExecutionDelay() + text.substring(1).indexOf(' ') + 1;
            }
        } else current = null;
        if (slot819b != null && slot819b.field12 != null && slot819b.field12.field4 != null
                && slot819b.field12.field4.contains("Shell")) limit = 30000;
        screen.inputField.setMaxStringLength(limit);
    }
    public String[] primaryAliases(Collection<CommandBase> source) {
        List<String> result = new ArrayList<>(source.size());
        for (CommandBase command : source) {
            if (command.hidden) continue;
            result.add("." + command.aliases[0]);
        }
        return result.toArray(new String[0]);
    }
    public String[] complete(String text, String[] args) {
        String label = getLabel(text);
        CommandManager manager = (CommandManager) CommandManager.INSTANCE;
        if (manager == null) return new String[0];
        List<CommandBase> exact = manager.findCommands(label, true);
        if (!exact.isEmpty() && text.contains(" ")) {
            String[] completions = exact.getFirst().tabComplete(text.substring(text.indexOf(' ') + 1), args);
            return completions == null ? new String[0] : completions;
        }
        if (text.length() < 2) {
            Collection<CommandBase> source = label.isEmpty() ? manager.view : manager.findCommands(label, false);
            return primaryAliases(source);
        }
        return matchingAliases(text);
    }

    public String[] complete(String text, String word) {
        String[] args = text != null && text.contains(" ")
                ? text.substring(text.indexOf(' ') + 1).split(" ", -1)
                : new String[0];
        return complete(text == null ? "" : text, args);
    }
    public void updateInputColor(GuiChat screen) {
        String prefix = String.valueOf(UserConfiguration.COMMAND_PREFIX.getChar());
        screen.inputField.setTextColor(com.adjustclient.module.impl.render.hud.HUDModule.getHudColor().getRGB());
    }
    public static void appendSeparator(GuiPersistentChat chat, ChatMessage message) {
        Minecraft mc = chat.mc;
        int width = (int) Math.floor(mc.gameSettings.chatWidth * 280.0F + 40.0F);
        int count = (int) (width / (float) Minecraft.getMinecraft().fontRendererObj.getStringWidth("-"));
        if (count < 1) return;
        int section = count >>> 2;
        for (int i = 0; i < count; i++) {
            message.color = i % section == 0 ? ChatColor.WHITE : ChatColor.GRAY;
            message.append("-");
        }
    }
    public List<ChatLine> withManual(GuiPersistentChat chat, Object context, List<ChatLine> lines) {
        if (current == null) return lines;
        List<ChatLine> result = new ArrayList<>(lines);
        ChatMessage message = new ChatMessage();
        appendSeparator(chat, message);
        message.append("\n");
        message.color = ChatColor.GRAY;
        message.append("Command manual: ").append(current.getName()).append(" ");
        message.color = ChatColor.GRAY;
        message.append(Arrays.toString(current.aliases)).append("\n").append("\n");
        message.color = ChatColor.GRAY;
        message.append(current.field8).append("\n").append("\n");
        message.color = ChatColor.GRAY;
        message.append(String.valueOf(UserConfiguration.COMMAND_PREFIX.getChar()) + current.aliases[0] + " ").append("\n");
        appendSeparator(chat, message);
        int width = (int) Math.floor(chat.getChatWidth() / chat.mc.gameSettings.chatScale);
        for (IChatComponent component : GuiUtilRenderComponents.splitText(message.getComponent(), width,
                Minecraft.getMinecraft().fontRendererObj, false, false)) {
            com.adjustclient.util.ChatUtil.setColorSupplier(component, () -> {
                java.awt.Color animated = com.adjustclient.module.impl.render.hud.HUDModule.animatedColor;
                return (animated != null ? animated
                        : com.adjustclient.module.impl.render.hud.HUDModule.colorSetting.getColor()).getRGB();
            });
            ChatLine line = new ChatLine(0, component, 0);
            line.lineString = component;
            line.messageComponent = component;
            line.originalComponent = component;
            line.timestamp = System.currentTimeMillis();
            line.firstWrappedLine = true;
            result.add(0, line);
        }
        return result;
    }
    public CommandBase findCommand(String text) {
        List<CommandBase> matches = commands.findCommands(getLabel(text), text.substring(1).contains(" "));
        return matches.isEmpty() ? null : matches.getFirst();
    }
    public static boolean doesNotMatch(String text, String candidate) {
        return !candidate.toLowerCase(Locale.getDefault()).startsWith(text.toLowerCase(Locale.getDefault()));
    }
    public String[] matchingAliases(String text) {
        List<String> aliases = new ArrayList<>();
        for (CommandBase command : ((CommandManager) CommandManager.INSTANCE).view) {
            if (command.hidden) continue;
            for (String label : command.aliases) aliases.add("." + label);
        }
        aliases.removeIf(candidate -> doesNotMatch(text, candidate));
        return aliases.toArray(new String[0]);
    }
    public static final Supplier<Integer> ACCENT = () -> {
        return com.adjustclient.module.impl.render.hud.HUDModule.getHudColor().getRGB();
    };
}
