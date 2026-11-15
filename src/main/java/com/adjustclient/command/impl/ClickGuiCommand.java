package com.adjustclient.command.impl;

import com.adjustclient.command.CommandExecutor;
import com.adjustclient.module.impl.render.ClickGui;

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
