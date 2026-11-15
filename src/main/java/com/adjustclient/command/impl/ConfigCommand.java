package com.adjustclient.command.impl;

import com.adjustclient.Adjust;
import com.adjustclient.command.CommandExecutor;
import com.adjustclient.service.config.*;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.util.*;
import com.adjustclient.vfs.*;
import com.adjustclient.logging.*;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.stream.*;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;
import net.minecraft.util.ChatColor;

public class ConfigCommand extends CommandExecutor {
    public Object field24;
    public ConfigCommand(String name) {
        super(name);
        field8 = "Load, save, delete or create new configurations.";
        aliases = new String[] {"configuration", "config", "cfg"};
    }

    public enum LoadMode {
        ALL, VISUALS, BINDS, DEFENSES;
        public Object field16;
    }

    public static IChatComponent createIdComponent(int id) {
        String encoded = encodeId(id);
        ChatMessage message = new ChatMessage(new ChatComponentText("").appendText(" "));
        message.color = ChatColor.RED;
        message.italic = true;
        message.append("id: " + encoded);
        ChatMessage hint = new ChatMessage();
        hint.color = ChatColor.GRAY;
        hint.append("Click to copy!");
        message.getComponent().getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hint.getComponent()));
        message.getComponent().getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "", () -> {
            copyId(encoded);
        }));
        return message.getComponent();
    }

    public void listConfigurations(ConfigurationService service) {
        ChatMessage message = new ChatMessage();
        message.color = ChatColor.GRAY;
        message.append("You have ").append(service.configurations.size());
        message.color = ChatColor.GRAY;
        message.append(" configurations").append("\n");
        for (ConfigurationManager config : service.configurations) {
            IChatComponent name = new ChatComponentText(config.name);
            if (config.id != service.selected.id) name.getChatStyle().setColor(ChatColor.GRAY);
            IChatComponent entry = new ChatComponentText("").appendSibling(name);
            ChatMessage hover = new ChatMessage(new ChatComponentText("").appendSibling(
                    new ChatComponentText(config.name).setChatStyle(new ChatStyle().setBold(true))));
            hover.append("\n");
            hover.color = ChatColor.GRAY;
            if (config.lastUsed == 0L) hover.append("Never used");
            else hover.append("Last used ").append(TimeUtil.timeAgo(config.lastUsed, " ago"));
            hover.append("\n");
            hover.color = ChatColor.GRAY;
            hover.append("Created on ").append(TimeUtil.DATE_FORMAT.format(new Date(config.created)));
            entry.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.getComponent()));
            entry.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "", () -> {
                ConfigurationManager current = service.find(config.id);
                if (current != null) requestLoad(service, LoadMode.ALL, current);
            }));
            message.append(entry).append(" ");
        }
        sendReplacingMessage(message);
    }

    public void sendError(ErrorReport error) {
        sendMessage(createMessage().format(error.message, error.arguments));
    }

    public static String encodeId(int id) {
        char[] digits = Integer.toString(id).toCharArray();
        for (int i = 0; i < digits.length; i++) digits[i] = (char) (122 - digits[i]);
        return new String(digits);
    }

    public static void copyId(String id) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(id), null);
    }

    public static byte[] serializeConfiguration() {
        return VirtualFileSystem.INSTANCE.emptyConfig();
    }

    public void loadConfiguration(String name, LoadMode mode, ConfigurationService service) {
        ConfigurationManager config = service.findByName(name);
        if (config == null) sendUnknownConfiguration(name);
        else requestLoad(service, mode, config);
    }

    public void requestLoad(ConfigurationService service, LoadMode mode, ConfigurationManager config) {
        ChatMessage message = createMessage();
        ConfigurationLoadCallback apply = (status, bytes) -> {
            applyConfiguration(mode, config, service, status, bytes);
        };
        service.requestData(config, (status, bytes) -> {
            handleLoadReply(message, config, apply, status, bytes);
        });
    }

    public static void handleLoadReply(ChatMessage message, ConfigurationManager config, ConfigurationLoadCallback callback, int status, byte[] bytes) {
        if (status == 193435832) {
            mc.addScheduledTask(() -> {
                callback.onReply(status, bytes);
            });
            return;
        }
        switch (status) {
            case 19105381:
                message.append("You don't own this configuration (logic bug, please report this!).");
                break;
            case 7434855:
                message.append("Cryptographic failure with configuration (" + (ConfigurationService.dataKey != null ? "V" : "K") + ").");
                break;
            case 152532851:
                message.append("An unexpected error occurred when trying to load your configuration (please report this!).");
                break;
            default: return;
        }
        message.append(createIdComponent(config.id));
        ChatUtil.addChatMessage(message.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
    }

    public void applyConfiguration(LoadMode mode, ConfigurationManager config, ConfigurationService service, int status, byte[] bytes) {
        LoadOptions options = new LoadOptions();
        options.keybindManager = Adjust.INSTANCE.bindManager;
        options.modules = mode != LoadMode.BINDS;
        options.keybinds = mode != LoadMode.VISUALS;
        options.properties = mode != LoadMode.BINDS;
        options.partial = mode == LoadMode.VISUALS;
        options.bedDefenseOnly = mode == LoadMode.DEFENSES;
        if (VirtualFileSystem.INSTANCE.load(bytes, options)) {
            ChatMessage message = createMessage();
            message.append("Configuration ").append(config.name);
            message.color = ChatColor.GRAY;
            message.append(" loaded!");
            if (mode == LoadMode.BINDS) { message.color = ChatColor.GRAY; message.append(" (binds only)"); }
            else if (mode == LoadMode.VISUALS) { message.color = ChatColor.GRAY; message.append(" (visuals only)"); }
            else if (mode == LoadMode.DEFENSES) { message.color = ChatColor.GRAY; message.append(" (bed defenses only)"); }
            sendPersistentMessage(message);
            if (mode == LoadMode.ALL) service.select(config);
            else com.adjustclient.service.util.ServiceUtil.markConfigurationDirty();
        } else {
            ChatMessage message = createMessage();
            if (mode == LoadMode.DEFENSES) {
                message.append("Could not load bed defenses from configuration ").append(config.name);
                message.color = ChatColor.GRAY;
                message.append(". It may not contain saved defenses or its data may be invalid.");
            } else {
                message.append("Failed to deserialize file system (please report this!).").append(createIdComponent(config.id));
            }
            sendPersistentMessage(message);
        }
    }

    public void handleSaveReply(ConfigurationManager config, int status) {
        ChatMessage message = createMessage();
        switch (status) {
            case 262843055:
                message.append("Configuration ").append(config.name);
                message.color = ChatColor.GRAY;
                message.append(" saved!");
                sendPersistentMessage(message);
                VirtualFileSystem.INSTANCE.dirtyTimer = null;
                VirtualFileSystem.INSTANCE.lastSaveTimer.reset();
                return;
            case 2071427963:
                message.append("Invalid configuration name (logic bug, please report this!).");
                message.append(createIdComponent(config.id));
                break;
            case -2121828233:
                message.append("You don't own this configuration in order to modify it (logic bug, please report this!).");
                message.append(createIdComponent(config.id));
                break;
            case -1073738824:
                message.append("An unexpected error occurred during saving (please report this!).");
                message.append(createIdComponent(config.id));
                break;
            case -1073738827:
                message.append("An unexpected error occurred (please report this!).");
                message.append(createIdComponent(config.id));
                break;
            default:
                return;
        }
        sendPersistentMessage(message);
    }

    public void createConfiguration(String name) {
        if (!ConfigurationService.isValidConfigName(name)) {
            sendPersistentMessage(createMessage().append("Configuration names must be alphanumeric with less than 16 characters."));
            return;
        }
        byte[] bytes = VirtualFileSystem.INSTANCE.emptyConfig();
        if (bytes == null) return;
        ConfigurationService service = Adjust.INSTANCE.screenStack.configurations;
        ConfigurationManager created = service != null ? service.createLocal(name, bytes) : null;
        ChatMessage message = createMessage();
        if (created != null) {
            message.append("Created a new configuration by the name of ").append(name);
            message.color = ChatColor.GRAY;
            message.append("!");
        } else {
            message.append("A configuration with that name already exists.");
        }
        sendPersistentMessage(message);
    }

    @SuppressWarnings("unused")
    private void vendorCreateConfiguration(String name) {
        if (name.isEmpty() || name.length() >= 17 || !name.matches("^[a-zzA-Z0-9 !@#$%^&~*_\\-=+,./\"';:]*$")
                || name.startsWith(" ") || name.endsWith(" ")) {
            sendPersistentMessage(createMessage().append("Configuration names must be alphanumeric with less than 16 characters."));
            return;
        }
        byte[] bytes = VirtualFileSystem.INSTANCE.emptyConfig();
        if (bytes == null) return;
        new com.adjustclient.backend.api.request.config.CreateConfigurationRequest(name, bytes).toPacket().sendAsync(
                response -> {
                    vendorHandleCreateReply(name, response);
                });
    }

    @SuppressWarnings("unused")
    private void vendorHandleCreateReply(String name, com.adjustclient.backend.api.response.BackendResponse response) {
        ChatMessage message = createMessage();
        if (response.status == com.adjustclient.backend.api.response.ResponseStatus.OK) {
            message.append("Created a new configuration by the name of ").append(name);
            message.color = ChatColor.GRAY;
            message.append("!");
        } else if (response.body instanceof com.adjustclient.backend.api.response.TextResponseBody) {
            message.append(((com.adjustclient.backend.api.response.TextResponseBody) response.body).value);
        } else {
            message.append("Unexpected error.");
            sendPersistentMessage(message);
            Logger.log(LogLevel.ERROR, true, "Failed to create configuration due to: {}", response);
            return;
        }
        sendPersistentMessage(message);
    }

    public void removeConfiguration(String name, ConfigurationService service) {
        ConfigurationManager config = service.findByName(name);
        if (config == null) { sendUnknownConfiguration(name); return; }
        if (service.selected != null && config.id == service.selected.id) {
            ChatMessage message = createMessage();
            message.append("You are currently using ").append(config.name);
            message.color = ChatColor.GRAY;
            message.append(" and may not remove it until you switch to another configuration.");
            sendPersistentMessage(message);
            return;
        }
        ChatMessage message = createMessage();
        if (service.deleteLocal(config)) {
            message.append("Configuration ").append(config.name);
            message.color = ChatColor.GRAY;
            message.append(" was removed!");
        } else {
            message.append("Unable to remove configuration.");
        }
        sendPersistentMessage(message);
    }

    @SuppressWarnings("unused")
    private void vendorRemoveConfiguration(String name, ConfigurationService service) {
        ConfigurationManager config = service.findByName(name);
        if (config == null) { sendUnknownConfiguration(name); return; }
        if (config.id == service.selected.id) {
            ChatMessage message = createMessage();
            message.append("You are currently using ").append(config.name);
            message.color = ChatColor.GRAY;
            message.append(" and may not remove it until you switch to another configuration.");
            sendPersistentMessage(message);
            return;
        }
        new com.adjustclient.backend.api.request.config.DeleteConfigurationRequest(config).toPacket().sendAsync(
                response -> {
                    vendorHandleRemoveReply(config, response);
                });
    }

    @SuppressWarnings("unused")
    private void vendorHandleRemoveReply(ConfigurationManager config,
            com.adjustclient.backend.api.response.BackendResponse response) {
        ChatMessage message = createMessage();
        if (response.status == com.adjustclient.backend.api.response.ResponseStatus.OK) {
            message.append("Configuration ").append(config.name);
            message.color = ChatColor.GRAY;
            message.append(" was removed!");
        } else if (response.body instanceof com.adjustclient.backend.api.response.TextResponseBody) {
            message.append(((com.adjustclient.backend.api.response.TextResponseBody) response.body).value);
        } else {
            message.append("Unexpected error.");
            sendPersistentMessage(message);
            Logger.log(LogLevel.ERROR, true, "Failed to remove configuration due to: {}", response);
            return;
        }
        sendPersistentMessage(message);
    }

    public void shareConfiguration(String name, String username, ConfigurationService service) {
        ConfigurationManager config = service.findByName(name);
        if (config == null) { sendUnknownConfiguration(name); return; }
        sendPersistentMessage(createMessage().append("Sharing configurations is unavailable in offline mode."));
    }

    @SuppressWarnings("unused")
    private void vendorShareConfiguration(String name, String username, ConfigurationService service) {
        ConfigurationManager config = service.findByName(name);
        if (config == null) { sendUnknownConfiguration(name); return; }
        ((com.adjustclient.property.UserManager) Adjust.INSTANCE.field40).lookup(username, user -> {
            if (user == null) { sendUnknownUser(username); return; }
            if (user.useClientColor) {
                sendMessage(createMessage().format("You cannot share a configuration with yourself."));
                return;
            }
            java.util.function.Consumer<ErrorReport> report = error -> {
                sendError(error);
            };
            new com.adjustclient.backend.api.request.friends.UnblockUserRequest(config, user).toPacket().sendAsync(
                    response -> {
                        if (response.status == com.adjustclient.backend.api.response.ResponseStatus.OK) {
                            report.accept(new ErrorReport("Shared configuration {} with {}! They have 30 seconds to accept it.", config.name, user.name));
                        } else if (response.body instanceof com.adjustclient.backend.api.response.EnumResponseBody
                                && ((com.adjustclient.backend.api.response.EnumResponseBody) response.body).value instanceof com.adjustclient.backend.api.error.ConfigurationError) {
                            var error = (com.adjustclient.backend.api.error.ConfigurationError) ((com.adjustclient.backend.api.response.EnumResponseBody) response.body).value;
                            report.accept(error.handler.apply(user.name));
                        } else {
                            report.accept(new ErrorReport("Failed to share configuration with {}.", user.name));
                        }
                    });
        });
    }

    @Override public String[] tabComplete(String input, String[] ignored) {
        List<String> parts = new ArrayList<>(Arrays.asList(input.split(" ")));
        if (input.endsWith(" ")) parts.add("");
        String[] args = parts.toArray(new String[0]);
        if (args.length == 0) return null;
        String[] commands = {"create", "save", "load", "loadvisuals", "loaddefenses", "remove", "send"};
        if (StringUtil.containsIdentity(args[0], commands)) {
            if (args.length >= 3 && args[0].equalsIgnoreCase("send")) return CommandCompleter.completeUsers(args[2]);
            if (args.length >= 2) return CommandCompleter.complete(args[1], CommandCompleter.CONFIGURATIONS);
        }
        Set<String> matches = new LinkedHashSet<>();
        CommandCompleter.addMatching(commands, matches, args[0].toLowerCase());
        return matches.toArray(new String[0]);
    }

    @Override protected void run(String label, String[] args) {
        ConfigurationService service = Adjust.INSTANCE.screenStack.configurations;
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) { listConfigurations(service); return; }
        String command = args[0];
        if (command.equalsIgnoreCase("reset") || command.equalsIgnoreCase("clear")) {
            if (args.length == 1) resetConfiguration(null);
            else if (args.length == 2) resetConfiguration(args[1]);
            else sendTooManyArguments();
            return;
        }
        if (args.length >= 2) {
            if (command.equalsIgnoreCase("create")) { createConfiguration(args[1]); return; }
            if (command.equalsIgnoreCase("save")) { saveConfiguration(args[1], service); return; }
            if (command.equalsIgnoreCase("load") || command.equalsIgnoreCase("loadvisuals")
                    || command.equalsIgnoreCase("loadbinds") || command.equalsIgnoreCase("loaddefenses")) {
                LoadMode mode = command.equalsIgnoreCase("loadvisuals") ? LoadMode.VISUALS
                        : command.equalsIgnoreCase("loadbinds") ? LoadMode.BINDS
                        : command.equalsIgnoreCase("loaddefenses") ? LoadMode.DEFENSES : LoadMode.ALL;
                loadConfiguration(args[1], mode, service);
                return;
            }
            if (args.length >= 3 && (command.equalsIgnoreCase("send") || command.equalsIgnoreCase("share"))) {
                shareConfiguration(args[1], args[2], service); return;
            }
            if (command.equalsIgnoreCase("remove") || command.equalsIgnoreCase("delete")) {
                removeConfiguration(args[1], service); return;
            }
        }
        sendUnknownArgument(command, "create <config>", "save <config>", "load <config>", "loadvisuals <config>",
                "loadbinds <config>", "loaddefenses <config>", "remove <config>", "send <config> <user>", "list", "reset [module]");
    }

    public static int resetSettings(com.adjustclient.setting.SettingContainer container) {
        int count = 0;
        for (com.adjustclient.setting.Setting<?> setting : container.getSettings()) {
            if (setting instanceof com.adjustclient.setting.SettingContainer)
                count += resetSettings((com.adjustclient.setting.SettingContainer) setting);
            if (UserConfiguration.isUserSetting(setting)) continue;
            setting.reset();
            count++;
        }
        return count;
    }

    public void resetConfiguration(String moduleName) {
        if (moduleName == null) {
            ChatMessage message = createMessage();
            int modules = resetModules();
            int settings = resetAllSettings();
            int bindings = resetBindings();
            message.format("Reset {} modules, {} settings and {} bindings back to their defaults.", modules, settings, bindings);
            sendMessage(message);
            return;
        }
        for (com.adjustclient.module.Module module : Adjust.INSTANCE.moduleManager.objects) {
            if (!moduleName.equalsIgnoreCase(module.getName())
                    && !moduleName.equalsIgnoreCase(module.getName().replaceAll(" ", ""))) continue;
            ChatMessage message = createMessage();
            message.format("Reset {} with {} settings back to default.", module.getName(), resetSettings(module));
            sendMessage(message);
            module.syncHidden();
            return;
        }
        sendUnknownModule(moduleName);
    }

    public static int resetBindings() {
        for (com.adjustclient.bind.Bind bind : Adjust.INSTANCE.bindManager.view) {
            if (!bind.mutable || bind.field12 == null) continue;
            bind.clearKeys();
            for (com.adjustclient.bind.BindKey key : bind.field12) bind.setKey(key);
        }
        return Adjust.INSTANCE.bindManager.view.size();
    }

    public static int resetModules() {
        for (com.adjustclient.module.Module module : Adjust.INSTANCE.moduleManager.view) module.syncHidden();
        return Adjust.INSTANCE.moduleManager.view.size();
    }

    public static int resetAllSettings() {
        List<com.adjustclient.setting.SettingContainer> containers = new ArrayList<>(Adjust.INSTANCE.moduleManager.view);
        containers.add((com.adjustclient.setting.SettingContainer) Adjust.INSTANCE.field28);
        int count = 0;
        for (com.adjustclient.setting.SettingContainer container : containers) count += resetSettings(container);
        return count;
    }

    public void saveConfiguration(String name, ConfigurationService service) {
        ConfigurationManager config = service.findByName(name);
        if (config == null) { sendUnknownConfiguration(name); return; }
        byte[] bytes = serializeConfiguration();
        if (bytes == null) return;
        service.save(config, bytes, status -> {
            handleSaveReply(config, status);
        });
    }
}

