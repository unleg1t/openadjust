package com.adjustclient.core.commands;

import com.adjustclient.utilities.MinecraftInstance;

public abstract class SubCommand implements MinecraftInstance, Command {
    protected final ParentCommand parent;
    protected final String name;
    protected boolean hidden;

    protected SubCommand(ParentCommand parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public SubCommand setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    @Override public String getName() { return parent.name; }

    public String[] tabComplete(String input, String[] args) { return null; }

    public abstract void run(String label, String[] args);
}
