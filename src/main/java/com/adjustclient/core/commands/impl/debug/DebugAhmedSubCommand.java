package com.adjustclient.core.commands.impl.debug;

import com.adjustclient.core.commands.ParentCommand;
import com.adjustclient.core.commands.SubCommand;
import com.adjustclient.managers.credits.CreditsRenderer;
import com.adjustclient.managers.util.ServiceUtil;

public class DebugAhmedSubCommand extends SubCommand {
    public Object field16;

    public DebugAhmedSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        boolean wasEnabled = ServiceUtil.ahmedElSisiMode;
        ServiceUtil.ahmedElSisiMode = !wasEnabled;
        CreditsRenderer.INSTANCE = wasEnabled ? null : new CreditsRenderer();
        sendMessage(createMessage().format("Turned Ahmed El-Sisi {}!", ServiceUtil.ahmedElSisiMode ? "on" : "off"));
    }
}
