package com.adjustclient.core.commands.impl;

import com.adjustclient.Adjust;
import com.adjustclient.core.commands.CommandBase;
import com.adjustclient.modules.Module;
import com.adjustclient.utilities.CommandCompleter;
import java.util.LinkedHashSet;

public final class ToggleCommand extends CommandBase {
    public Object field24;
    public ToggleCommand(String name, String... aliases) {
        super(name);
        this.field8 = "Toggles a module.";
        this.aliases = aliases;
    }

    @Override public String[] tabComplete(String input, String[] args) {
        LinkedHashSet<String> matches = new LinkedHashSet<>();
        CommandCompleter.completeModules(matches, input.toLowerCase());
        return matches.toArray(new String[0]);
    }

    @Override public boolean execute(String label, String[] args) {
        if (args.length == 0) { sendTooLittleArguments(); return true; }
        if (args.length >= 2) { sendTooManyArguments(); return true; }
        Module module = Adjust.INSTANCE.moduleManager.get(args[0]);
        if (module == null) { sendUnknownModule(args[0]); return true; }
        module.toggle();
        sendMessage(createMessage().format("Toggled {} " + (module.isEnabled() ? "on" : "off") + ".", module.getName()));
        return false;
    }
}
