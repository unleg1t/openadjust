package com.adjustclient.core.commands.impl;

import com.adjustclient.core.commands.CommandExecutor;
import com.adjustclient.core.events.ChatSendEvent;
import com.adjustclient.core.events.EventBus;

public final class SayCommand extends CommandExecutor {
    public Object field24;
    public SayCommand(String name, String... aliases) {
        super(name);
        this.field8 = "Send a message to Minecraft chat without accessing commands.";
        this.aliases = aliases;
    }

    @Override protected void run(String label, String[] args) {
        if (args.length == 0) { sendTooLittleArguments(); return; }
        String message = String.join(" ", args);
        if (mc.thePlayer == null) return;
        ChatSendEvent event = new ChatSendEvent(message, false);
        EventBus.post(event);
        if (!event.cancelled) mc.thePlayer.sendChatMessage(event.message);
    }

    @Override public int getExecutionDelay() { return 100; }
}
