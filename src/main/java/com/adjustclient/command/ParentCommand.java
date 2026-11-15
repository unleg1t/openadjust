package com.adjustclient.command;

import com.adjustclient.util.CommandCompleter;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class ParentCommand extends CommandExecutor {
    protected final ArrayList<SubCommand> subCommands = new ArrayList<>();

    protected ParentCommand(String name) { super(name); }

    @Override public String[] tabComplete(String input, String[] args) {
        if (!input.contains(" ")) {
            return CommandCompleter.complete(input, (matches, prefix) -> {
                for (SubCommand command : subCommands) {
                    if (!command.hidden && command.name.toLowerCase().startsWith(prefix)) matches.add(command.name);
                }
            });
        }
        String[] parts = input.split("\\s+");
        if (parts.length == 0) return null;
        for (SubCommand command : subCommands) {
            if (command.name.equalsIgnoreCase(parts[0])) return command.tabComplete(input, args);
        }
        return null;
    }

    @Override protected void run(String label, String[] args) {
        if (args.length == 0) {
            sendTooLittleArguments(getSubCommandNames());
            return;
        }
        for (SubCommand command : subCommands) {
            if (command.name.equalsIgnoreCase(args[0])) {
                command.run(label, Arrays.copyOfRange(args, 1, args.length));
                return;
            }
        }
        sendUnknownArgument(args[0], getSubCommandNames());
    }

    protected String[] getSubCommandNames() {
        return subCommands.stream()
                .filter(command -> !command.hidden)
                .map(command -> command.name)
                .toArray(String[]::new);
    }
}
