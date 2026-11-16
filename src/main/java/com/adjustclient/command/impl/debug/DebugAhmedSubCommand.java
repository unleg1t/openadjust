package com.adjustclient.command.impl.debug;

import com.adjustclient.command.ParentCommand;
import com.adjustclient.command.SubCommand;
import com.adjustclient.service.credits.CreditsRenderer;
import com.adjustclient.service.util.ServiceUtil;

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
