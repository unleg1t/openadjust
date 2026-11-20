package com.adjustclient.utilities;

import com.adjustclient.Adjust;
import com.adjustclient.core.bind.Bind;
import com.adjustclient.core.commands.impl.BindCommand;
import com.adjustclient.managers.config.ConfigurationManager;
import com.adjustclient.core.settings.property.UserManager;
import com.adjustclient.managers.party.chat.QuickMessage;
import com.adjustclient.managers.party.ui.ContextMenuKey;
import com.adjustclient.managers.user.ClientUser;
import java.util.function.IntFunction;
import com.adjustclient.modules.Module;
import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.util.ChatColor;

public final class CommandCompleter {
    public static final BiConsumer<Set<String>, String> MODULES = (matches, prefix) ->
            completeModules(matches, prefix);
    public static final BiConsumer<Set<String>, String> BINDS = (matches, prefix) ->
            completeBinds(matches, prefix);
    public static final BiConsumer<Set<String>, String> CONFIGURATIONS = (matches, prefix) -> {
        for (ConfigurationManager configuration : Adjust.INSTANCE.screenStack.configurations.configurations) {
            String name = configuration.name;
            if (name != null && name.toLowerCase().startsWith(prefix)) matches.add(name);
        }
    };
    public static final BiConsumer<Set<String>, String> USERS = (matches, prefix) -> {
        for (ClientUser user : ((UserManager) Adjust.INSTANCE.field40).view) {
            if (user.name != null && user.name.toLowerCase().startsWith(prefix)) matches.add(user.name);
        }
    };
    public static final IntFunction<String[]> STRING_ARRAY = length -> new String[length];
    public static final java.util.function.Function<String[], BiConsumer<Set<String>, String>> MATCHING =
            values -> (matches, prefix) -> addMatching(values, matches, prefix);
    public static final java.util.function.BiFunction<com.adjustclient.managers.party.ui.PartyPanel, String, BiConsumer<Set<String>, String>> PARTY =
            (panel, input) -> (matches, prefix) -> completeParty(panel, input, matches, prefix);

    public static void completeParty(com.adjustclient.managers.party.ui.PartyPanel panel, String input, Set<String> matches, String prefix) {
        for (QuickMessage message : QuickMessage.MESSAGES) {
            String token = "{" + message.name + "}";
            if (token.toLowerCase().startsWith(prefix)) matches.add(token.toLowerCase());
        }
        for (ContextMenuKey member : panel.members) {
            String name = member.user.name;
            if (name.toLowerCase().startsWith(prefix)) matches.add(name);
        }
        Collections.addAll(matches, PlayerNameCompleter.complete(input, true));
    }

    public static String[] completeUsers(String input) {
        return input.contains(" ") ? null : complete(input, USERS);
    }

    public static void completeBinds(Set<String> matches, String prefix) {
        for (Bind bind : BindCommand.collectVisibleBinds()) {
            String name = bind.name.replaceAll(" ", "");
            if (name.toLowerCase().startsWith(prefix)) matches.add(name);
        }
    }

    public static String[] complete(String input, BiConsumer<Set<String>, String> provider) {
        String prefix = input.toLowerCase();
        Set<String> matches = new LinkedHashSet<>();
        provider.accept(matches, prefix);
        return matches.toArray(new String[0]);
    }

    public static void completeModules(Set<String> matches, String prefix) {
        for (Module module : Adjust.INSTANCE.moduleManager.view) {
            String name = module.getName().replaceAll(" ", "");
            if (name.toLowerCase().startsWith(prefix)) {
                matches.add("" + (module.isEnabled() ? ChatColor.GREEN : ChatColor.RED) + name + ChatColor.RESET);
            }
        }
    }

    public static void addMatching(String[] values, Set<String> matches, String prefix) {
        for (String value : values) {
            if (value.toLowerCase().startsWith(prefix)) matches.add(value);
        }
    }
}
