package com.adjustclient.command.impl;

import com.adjustclient.command.ParentCommand;
import com.adjustclient.command.SubCommand;
import com.adjustclient.module.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.util.PlayerNameCompleter;
import net.minecraft.client.network.NetworkPlayerInfo;

public class VoiceMuteSubCommand extends SubCommand {
    public Object field16;
    public VoiceMuteSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public String[] tabComplete(String input, String[] args) {
        String[] parts = input.split("\\s+");
        return PlayerNameCompleter.complete(parts[parts.length - 1], true);
    }

    @Override public void run(String label, String[] args) {
        if (args.length != 1) { sendTooLittleArguments(); return; }
        NetworkPlayerInfo player = mc.thePlayer.sendQueue.getPlayerInfo(args[0]);
        if (player == null) {
            sendMessage(createMessage().format("Couldn't find player with username {}.", args[0]));
            return;
        }
        boolean muted = VoiceChatModule.INSTANCE.toggleMute(player.getGameProfile().getId());
        sendMessage(createMessage().format(muted ? "{} is now muted." : "{} is no longer muted.",
                player.getGameProfile().getName()));
    }
}
