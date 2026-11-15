package com.adjustclient.command;

import com.adjustclient.event.Listener;
import com.adjustclient.util.MinecraftInstance;
import java.util.function.Consumer;

public abstract class CommandBase implements Listener, MinecraftInstance, Command {
    protected final String name;
    public Object field8;
    public String[] aliases;
    public Consumer<CommandContext> execution;
    public boolean hidden;

    protected CommandBase(String name) { this.name = name; }

    @Override public String getName() { return name; }

    public void completeExecution() {
        if (execution == null) throw new IllegalCallerException("Execution is null");
        Consumer<CommandContext> result = execution;
        execution = null;
        result.accept(new CommandContext());
    }

    public String[] tabComplete(String input, String[] args) { return null; }

    public int getExecutionDelay() { return 300; }

    public abstract boolean execute(String label, String[] args);
}
