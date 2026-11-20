package com.adjustclient.core.commands.impl;

import com.adjustclient.Adjust;
import com.adjustclient.core.bind.*;
import com.adjustclient.core.commands.CommandBase;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.KeyBindingUpdateEvent;
import com.adjustclient.core.events.MouseButtonInputEvent;
import com.adjustclient.managers.config.ConfigurationManager;
import com.adjustclient.managers.util.ServiceUtil;
import com.adjustclient.utilities.ChatMessage;
import com.adjustclient.utilities.CommandCompleter;
import com.adjustclient.utilities.vfs.LoadOptions;
import java.util.*;
import org.lwjgl.input.Keyboard;
import net.minecraft.util.ChatColor;

public final class BindCommand extends CommandBase {
    private String pendingName;
    private Bind pendingBind;
    public Object field32;

    public BindCommand() {
        super("Bind");
        field8 = "Modify client bindings.";
        aliases = new String[] {"bind"};
    }

    @EventHandler(priority = 100) public void onKey(KeyBindingUpdateEvent event) {
        if (pendingBind == null || execution == null) return;
        Bind bind = pendingBind;
        String name = pendingName;
        pendingBind = null;
        pendingName = null;
        toggleKey(bind, name, new KeyboardKey(event.keyCode));
        completeExecution();
        event.cancelled = true;
    }

    @EventHandler(priority = 100) public void onMouseButton(MouseButtonInputEvent event) {
        if (pendingBind == null || execution == null || event.button < 0 || !event.down) return;
        Bind bind = pendingBind;
        String name = pendingName;
        pendingBind = null;
        pendingName = null;
        toggleKey(bind, name, new MouseKey(event.button));
        completeExecution();
        event.cancelled = true;
    }

    public void clear(Bind bind) {
        List<String> names = new ArrayList<>();
        for (BindKey key : bind.keys) names.add(key.getName());
        int count = bind.keys.size();
        bind.clearKeys();
        ChatMessage message = createMessage().format("Cleared {} keybind" + (count == 1 ? "" : "s") + " from {}", count, bind.name);
        appendNames(message, names);
        sendMessage(message);
    }

    public void clear(BindKey key) {
        List<String> names = new ArrayList<>();
        for (Bind bind : collectVisibleBinds()) {
            if (bind.keys.contains(key)) {
                bind.removeKey(key);
                names.add(bind.name);
            }
        }
        int count = names.size();
        ChatMessage message = createMessage().format("Cleared {} bind" + (count == 1 ? "" : "s") + " from key {}", count, key.getName());
        appendNames(message, names);
        sendMessage(message);
    }

    public boolean edit(Bind bind, String argument) {
        if (argument == null) {
            pendingBind = bind;
            pendingName = bind.name;
            sendMessage(createMessage().format("Press any key to bind {}...", pendingName));
            return false;
        }
        BindKey key = parseKey(argument);
        if (key != null) {
            toggleKey(bind, bind.name, key);
        } else if (argument.equalsIgnoreCase("list")) {
            ChatMessage message = createMessage().format("{} has {} binds assigned to it", bind.name, bind.keys.size());
            message.format(bind.keys.isEmpty() ? "." : ": ");
            appendKeys(message, bind);
            sendMessage(message);
        } else if (argument.equalsIgnoreCase("clear") || argument.equalsIgnoreCase("none")) {
            sendMessage(createMessage().format("Cleared {} binds from {}.", bind.keys.size(), bind.name));
            if (!bind.keys.isEmpty()) {
                bind.clearKeys();
                ServiceUtil.markConfigurationDirty();
            }
        } else sendUnknownArgument(argument, "list", "clear");
        return true;
    }

    public void appendKeys(ChatMessage message, Bind bind) {
        boolean first = true;
        for (BindKey key : bind.keys) {
            if (!first) { message.color = ChatColor.GRAY; message.append(", "); }
            message.color = ChatColor.WHITE;
            message.append(key instanceof ScrollKey ? (((ScrollKey) key).keyCode() == 1069 ? "Scroll Up" : "Scroll Down") : key.getName());
            first = false;
        }
    }

    public void appendNames(ChatMessage message, List<String> names) {
        if (names.isEmpty()) { message.append("."); return; }
        message.append(": ");
        for (int i = 0; i < names.size(); i++) {
            if (i != 0) { message.color = ChatColor.GRAY; message.append(", "); }
            message.color = ChatColor.WHITE;
            message.append(names.get(i));
        }
    }

    @Override public String[] tabComplete(String input, String[] args) {
        if (input.startsWith("config ")) return CommandCompleter.complete(input.substring(input.indexOf(' ') + 1), CommandCompleter.CONFIGURATIONS);
        return Arrays.stream(CommandCompleter.complete(input, CommandCompleter.BINDS)).sorted().toArray(String[]::new);
    }

    public void toggleKey(Bind bind, String name, BindKey key) {
        boolean bound = bind.keys.contains(key);
        if (bound) bind.removeKey(key); else bind.setKey(key);
        sendMessage(createMessage().format(bound ? "Unbound {} from {}." : "Bound {} to {}.", name,
                key instanceof ScrollKey ? (((ScrollKey) key).keyCode() == 1069 ? "Scroll Up" : "Scroll Down") : key.getName()));
        ServiceUtil.markConfigurationDirty();
    }

    public void load(ConfigurationManager configuration, int status, byte[] input) {
        LoadOptions options = new LoadOptions();
        options.keybindManager = Adjust.INSTANCE.bindManager;
        options.keybinds = true;
        if (Adjust.INSTANCE.fileSystem.load(input, options)) {
            sendPersistentMessage(createMessage().format("Loaded keybindings from configuration {}.", configuration.name));
            Adjust.INSTANCE.fileSystem.markDirty();
        } else {
            sendPersistentMessage(createMessage().format("Failed to load your keybindings from configuration {}!", configuration.name));
        }
    }

    @Override public boolean execute(String label, String[] args) {
        if (args.length == 0) { sendTooLittleArguments(); return true; }
        if (args.length > 2 && !(args[0].equalsIgnoreCase("config") && args.length == 3)) {
            sendTooManyArguments();
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) { list(); return true; }
        Bind bind = Adjust.INSTANCE.bindManager.getByName(args[0]);
        boolean reversed = false;
        if (bind == null && args.length >= 2) {
            bind = Adjust.INSTANCE.bindManager.getByName(args[1]);
            reversed = bind != null;
        }
        if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("clearall")) {
            if (args.length == 2 && args[1].equalsIgnoreCase("yes")) {
                int count = Adjust.INSTANCE.bindManager.clearKeys();
                sendMessage(createMessage().format("Cleared {} binds from everything.", count));
                return true;
            }
            if (args.length >= 2) {
                Bind target = Adjust.INSTANCE.bindManager.getByName(args[1]);
                if (target != null) { clear(target); return true; }
                BindKey key = parseKey(args[1]);
                if (key != null) { clear(key); return true; }
            }
            sendMessage(createMessage().format("You must confirm clearing all binds with {}.", args[0].toLowerCase() + " yes"));
            return true;
        }
        if (args[0].equalsIgnoreCase("load") || args[0].equalsIgnoreCase("config")) {
            if (args.length == 1) { sendTooLittleArguments(); return true; }
            String name = args[0].equalsIgnoreCase("config") ? args[1] : String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            var service = Adjust.INSTANCE.screenStack.configurations;
            ConfigurationManager configuration = service.findByName(name);
            if (configuration == null) { sendUnknownConfiguration(name); return true; }
            if (args[0].equalsIgnoreCase("load")) {
                ChatMessage message = createMessage();
                service.requestData(configuration, (status, bytes) -> ConfigCommand.handleLoadReply(message, configuration,
                        (com.adjustclient.managers.config.ConfigurationLoadCallback) (reply, data) -> load(configuration, reply, data), status, bytes));
                return true;
            }
            return edit(configuration.getOrCreateBind().bind, args.length == 2 ? null : args[2]);
        }
        if (bind == null) {
            sendUnknownArgument(args[0], "list", "load <config>", "config <config> [key]", "clear <yes>",
                    "<bind> [key]", "<bind> list", "<bind> clear");
            return true;
        }
        return edit(bind, reversed ? args[0] : args.length < 2 ? null : args[1]);
    }

    public BindKey parseKey(String name) {
        String upper = name.toUpperCase();
        int key = Keyboard.getKeyIndex(upper);
        if (key != 0) return new KeyboardKey(key);
        int mouse = MouseKey.parse(upper);
        return mouse == -1 ? ScrollKey.parse(upper) : new MouseKey(mouse);
    }

    public static List<Bind> collectVisibleBinds() {
        return Adjust.INSTANCE.bindManager.view.stream()
                .filter(bind -> bind.name != null)
                .filter(bind -> !bind.hidden)
                .collect(java.util.stream.Collectors.toList());
    }

    public void list() {
        List<Bind> binds = collectVisibleBinds();
        binds.removeIf(bind -> bind.keys.isEmpty());
        ChatMessage message = new ChatMessage().format("Listing {} binds below:", binds.size());
        for (Bind bind : binds) {
            message.append("\n");
            message.format("{} ({}): ", bind.name, bind.keys.size());
            boolean first = true;
            for (BindKey key : bind.keys) {
                if (!first) { message.color = ChatColor.GRAY; message.append(", "); }
                message.color = ChatColor.WHITE;
                message.append(key instanceof ScrollKey ? (((ScrollKey) key).keyCode() == 1069 ? "Scroll Up" : "Scroll Down") : key.getName());
                first = false;
            }
        }
        sendMessage(message);
    }
}
