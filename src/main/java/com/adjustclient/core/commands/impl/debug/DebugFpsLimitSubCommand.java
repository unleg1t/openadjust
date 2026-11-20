package com.adjustclient.core.commands.impl.debug;

import com.adjustclient.core.commands.ParentCommand;
import com.adjustclient.core.commands.SubCommand;
import com.adjustclient.managers.credits.CreditsRenderer;
import com.adjustclient.managers.util.ServiceUtil;

public class DebugFpsLimitSubCommand extends SubCommand {
    public static boolean fpsLimitEnabled;
    public Object field16;

    public DebugFpsLimitSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        boolean wasEnabled = ServiceUtil.ahmedElSisiMode;
        ServiceUtil.ahmedElSisiMode = !wasEnabled;
        CreditsRenderer.INSTANCE = wasEnabled ? null : new CreditsRenderer();
        sendMessage(createMessage().format("Turned FPS limit {}!", fpsLimitEnabled ? "on" : "off"));
    }
}
