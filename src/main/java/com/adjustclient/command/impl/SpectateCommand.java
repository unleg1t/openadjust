package com.adjustclient.command.impl;

import com.adjustclient.command.CommandExecutor;
import com.adjustclient.module.impl.render.freecam.FreecamModule;
import com.adjustclient.util.PlayerNameCompleter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.entity.EntityPlayerSP;

public class SpectateCommand extends CommandExecutor {
    public Object field24;
    public SpectateCommand(String name, String... aliases) {
        super(name);
        this.field8 = "Spectate any player in your range.";
        this.aliases = aliases;
    }

    @Override public String[] tabComplete(String input, String[] args) {
        return PlayerNameCompleter.complete(input, true);
    }

    @Override protected void run(String label, String[] args) {
        if (args.length == 0) { sendTooLittleArguments("<player>"); return; }
        if (args.length >= 2) { sendTooManyArguments("<player>"); return; }
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityPlayer) || entity instanceof EntityPlayerSP) continue;
            EntityPlayer player = (EntityPlayer) entity;
            if (!player.getGameProfile().getName().equalsIgnoreCase(args[0])) continue;
            FreecamModule.activateForPlayer(player);
            sendMessage(createMessage().format("Spectating {}!", player.getGameProfile().getName()));
            return;
        }
        sendMessage(createMessage().format("No player by the name of {} is visible.", args[0]));
    }
}
