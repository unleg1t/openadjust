package com.adjustclient.core.commands.impl.debug;

import com.adjustclient.core.commands.ParentCommand;
import com.adjustclient.core.commands.SubCommand;
import com.adjustclient.utilities.HolidayTheme;

public class DebugSeasonSubCommand extends SubCommand {
    public static HolidayTheme holidayOverride;
    public Object field16;

    public DebugSeasonSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        if (args.length != 1) { sendTooLittleArguments(); return; }
        String message;
        if (args[0].equalsIgnoreCase("christmas")) {
            if (holidayOverride == HolidayTheme.CHRISTMAS) {
                holidayOverride = null;
                message = "Disabled Christmas mode!";
            } else {
                holidayOverride = HolidayTheme.CHRISTMAS;
                message = "Enabled Christmas mode!";
            }
        } else if (args[0].equalsIgnoreCase("halloween")) {
            if (holidayOverride == HolidayTheme.HALLOWEEN) {
                holidayOverride = null;
                message = "Disabled Halloween mode!";
            } else {
                holidayOverride = HolidayTheme.HALLOWEEN;
                message = "Enabled Halloween mode!";
            }
        } else return;
        sendMessage(createMessage().format(message));
    }
}
