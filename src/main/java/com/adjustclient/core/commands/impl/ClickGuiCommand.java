package com.adjustclient.core.commands.impl;

import com.adjustclient.core.commands.CommandExecutor;
import com.adjustclient.modules.impl.render.ClickGui;

public final class ClickGuiCommand extends CommandExecutor {
    public Object field24;
    public ClickGuiCommand(String name, String... aliases) {
        super(name);
        field8 = "Opens the Click GUI.";
        this.aliases = aliases;
    }

    @Override protected void run(String label, String[] args) {
        ClickGui.openClickGui();
    }
}
