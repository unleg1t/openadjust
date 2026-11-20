package com.adjustclient.core.settings.property;

import com.adjustclient.core.commands.CommandBase;
import com.adjustclient.core.commands.CommandContext;
import com.adjustclient.core.commands.impl.*;
import com.adjustclient.core.commands.impl.debug.DebugCommand;
import com.adjustclient.core.events.ChatSendEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.KeyInputEvent;
import com.adjustclient.core.events.Listener;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.managers.config.user.CommandPrefixProperty;
import com.adjustclient.utilities.ChatMessage;
import com.adjustclient.utilities.ChatUtil;
import com.adjustclient.utilities.KeyboardUtil;
import com.adjustclient.utilities.MinecraftInstance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatColor;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import com.adjustclient.managers.config.user.UserConfiguration;
import com.adjustclient.core.tag.PlayerTag;

public final class CommandManager extends Manager<CommandBase> implements Listener, MinecraftInstance {
    public static CommandManager INSTANCE;
    public static int commandKeyState;
    public Object field16;

    public CommandManager() {
        INSTANCE = this;
        add(new HelpCommand());
        add(new BindCommand());
        add(new ToggleCommand("Toggle", "toggle"));
        add(new ClickGuiCommand("Click GUI", "clickgui"));
        add(new VClipCommand("Vertical Clip"));
        add(new HClipCommand("Horizontal Clip"));
        add(new UsernameCommand("Username"));
        add(new NickCommand("Nickname"));
        add(new StatsCommand("Hypixel Statistics"));
        add(new SayCommand("Say", "say"));
        add(new ConfigCommand("Configuration"));
        add(new WhitelistCommand("Whitelist", PlayerTag.WHITELIST));
        add(new TargetCommand("Target", PlayerTag.TARGET));
        add(new NicksCommand("Nicks"));
        add(new VanishCommand("Vanished Players"));
        add(new PartyCommand("Party"));
        add(new MessageCommand());
        add(new ReplyCommand("Reply", "reply"));
        add(new FriendCommand());
        add(new BlockCommand("Block"));
        add(new InviteCommand());
        add(new DiscordCommand("Discord"));
        add(new SpectateCommand("Spectate", "spectate", "spec"));
        add(new ApiKeyCommand("API Key"));
        add(new VoiceCommand("Voice Chat", "mute", "voicechat", "vchat"));
        add(new GiveCommand("Give"));
        add(new CubelifyCommand("Cubelify Token", "cubelifytoken", "ctoken"));
        add(new EmojiCommand("Emojis", "emojis", "emoji", "emotes", "emote"));
        add(new DebugCommand("Debug"));
    }

    @EventHandler
    public void onChatSend(ChatSendEvent event) {
        if (event.isCancelled() || !event.addToHistory) return;
        String message = event.message;
        if (!isCommand(message)) return;
        event.cancelled = true;
        int space = message.indexOf(' ');
        String label = space == -1 ? message.substring(1) : message.substring(1, space);
        String rest = space == -1 ? "" : message.substring(space + 1).trim();
        String[] args = rest.isEmpty() ? new String[0] : rest.split(" ");
        CommandBase command = findCommand(label, true);
        if (command == null) {
            reportUnknownCommand(label);
            return;
        }
        command.execution = new ResultConsumer(label);
        if (command.execute(label, args)) {
            command.completeExecution();
        }
    }

    @EventHandler
    public void onKeyInput(KeyInputEvent event) {
        if (!event.down) return;
        if (mc.theWorld == null || mc.currentScreen != null || com.adjustclient.utilities.ScreenUtil.isAdjustScreenOpen()) return;
        char prefix = ((CommandPrefixProperty) UserConfiguration.COMMAND_PREFIX).getChar();
        if (event.character == prefix) {
            int key = org.lwjgl.input.Keyboard.getEventKey();
            mc.displayGuiScreen(new GuiChat(String.valueOf(prefix), key, KeyboardUtil.getKeyCharacter(key)));
            event.cancelled = true;
        }
    }

    public boolean isCommand(String text) {
        return !text.isEmpty() && text.charAt(0) == ((CommandPrefixProperty) UserConfiguration.COMMAND_PREFIX).getChar();
    }

    @Override
    public boolean matches(CommandBase command, Object key) {
        return key instanceof Class<?> && command.getClass() == key;
    }

    public void reportUnknownCommand(String label) {
        ChatComponentText prefix = new ChatComponentText("Adjust ");
        prefix.setChatStyle(new ChatStyle().setBold(true));
        ChatComponentText root = new ChatComponentText("");
        root.appendSibling(prefix);
        ChatMessage message = new ChatMessage(root);
        if (label.isEmpty()) {
            message.color = (ChatColor) ChatColor.GRAY;
            message.append("No command was provided. ");
        } else {
            message.color = (ChatColor) ChatColor.GRAY;
            message.append("Command ");
            message.color = (ChatColor) ChatColor.WHITE;
            message.append(label);
            message.color = (ChatColor) ChatColor.GRAY;
            message.append(" was not found. ");
            List<CommandBase> suggestions = findCommands(label, false);
            if (!suggestions.isEmpty()) {
                message.color = (ChatColor) ChatColor.GRAY;
                message.append("Did you mean: ");
                for (int i = 0; i < suggestions.size(); ++i) {
                    String[] aliases = suggestions.get(i).aliases;
                    if (aliases == null || aliases.length == 0) continue;
                    String alias = aliases[0];
                    message.color = (ChatColor) ChatColor.WHITE;
                    message.clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "", () -> {
                        if (mc.currentScreen instanceof GuiChat) {
                            ((GuiChat) mc.currentScreen).inputField.setText(
                                ((CommandPrefixProperty) UserConfiguration.COMMAND_PREFIX).getChar() + alias + " ");
                        }
                    });
                    message.append(alias);
                    if (i != suggestions.size() - 1) {
                        message.color = (ChatColor) ChatColor.GRAY;
                        message.append(", ");
                    }
                }
            }
        }
        ChatUtil.addChatMessage(message.component, 0, true, ChatUtil.DEFAULT_COLOR);
    }

    public List<CommandBase> findCommands(String text, boolean exact) {
        if (text.isEmpty()) return Collections.emptyList();
        String lower = text.toLowerCase();
        List<CommandBase> found = null;
        for (CommandBase command : view) {
            if ((!exact && command.hidden) || command.aliases == null) continue;
            for (String alias : command.aliases) {
                if (exact ? alias.equals(lower) : alias.startsWith(lower)) {
                    if (found == null) found = new ArrayList<>();
                    found.add(command);
                    break;
                }
            }
        }
        return found == null ? Collections.emptyList() : found;
    }

    public CommandBase findCommand(String text, boolean exact) {
        List<CommandBase> found = findCommands(text, exact);
        return found.isEmpty() ? null : found.get(0);
    }

    public static class ResultConsumer implements Consumer<CommandContext> {
        final String label;
        boolean supplied;
        final CommandManager owner = null;

        public ResultConsumer(String label) { this.label = label; }

        @Override
        public void accept(CommandContext result) {
            if (!supplied) {
                supplied = true;
                return;
            }
            Logger.log(LogLevel.WARN, true, "Command '{}' supplied more than one result(s).", label);
        }
    }
}
