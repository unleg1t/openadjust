package com.adjustclient.core.commands;

public abstract class CommandExecutor extends CommandBase {
    protected CommandExecutor(String name) { super(name); }

    @Override public boolean execute(String label, String[] args) {
        run(label, args);
        return true;
    }

    protected abstract void run(String label, String[] args);
}
