package com.adjustclient.command.impl.debug;

import com.adjustclient.command.ParentCommand;
import com.adjustclient.logging.Logger;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.util.ClipboardUtil;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.lwjgl.glfw.GLFW;

public final class DebugCommand extends ParentCommand {
    public static DebugCommand INSTANCE;
    public Object field28;

    public DebugCommand(String name) {
        super(name);
        INSTANCE = this;
        hidden = true;
        field8 = "Shh...";
        aliases = new String[] {"secrettoggle", "stg"};
        subCommands.add(new DebugToggleSubCommand(this, "debug"));
        subCommands.add(new DebugClientNameSubCommand(this, "clientname"));
        subCommands.add(new DebugEpsteinSubCommand(this, "epstein"));
        subCommands.add(new DebugGameSubCommand(this, "gamedebug"));
        subCommands.add(new DebugFpsLimitSubCommand(this, "limit_fps").setHidden(true));
        subCommands.add(new DebugBotSubCommand(this, "botdebug"));
        subCommands.add(new DebugNearbySubCommand(this, "players"));
        subCommands.add(new DebugSeasonSubCommand(this, "season").setHidden(true));
        subCommands.add(new DebugServerSubCommand(this, "serverdebug").setHidden(true));
        subCommands.add(new DebugVersionSubCommand(this, "wtfver"));
        subCommands.add(new DebugSyncRotationsSubCommand(this, "sync"));
        subCommands.add(new DebugAhmedSubCommand(this, "ahmed_el_sisi").setHidden(true));
    }

    public static byte[] transformDebugData(byte[] data) {
        String keyString = "FKAFfasdpDSffFff884";
        byte[] key = keyString.getBytes(StandardCharsets.UTF_16BE);
        for (int index = 0; key.length != 0 && index < data.length; index++) {
            data[index] ^= (byte) ((index + (index + 4) % 5 + 84) ^ key[index % key.length]);
        }
        return data;
    }

    public void printDebugData(String title, String data) {
        String encoded = Base64.getEncoder().encodeToString(transformDebugData(data.getBytes(StandardCharsets.UTF_8)));
        sendMessage(createMessage().format(title + " data has been copied to clipboard and printed to your console."));
        Logger.log(LogLevel.DEBUG, true, title + ": {}", encoded);
        ClipboardUtil.copyToClipboard(encoded); // Logs "Failed to set clipboard with '{}'" on failure.
    }
}
