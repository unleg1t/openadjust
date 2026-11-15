package com.adjustclient.command.impl.debug;

import com.adjustclient.command.ParentCommand;
import com.adjustclient.command.SubCommand;
import com.adjustclient.Adjust;
import com.adjustclient.util.PlayerNameCompleter;
import com.adjustclient.module.impl.combat.antibot.AntiBotModule;
import com.adjustclient.module.impl.combat.antibot.BotMovementTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import java.util.Objects;

public class DebugBotSubCommand extends SubCommand {
    public Object field16;

    public DebugBotSubCommand(ParentCommand parent, String name) { super(parent, name); }

    public static String describeBot(EntityPlayer player) {
        if (!AntiBotModule.INSTANCE.isEnabled()) return "AntiBot not toggled";
        BotMovementTracker data = (BotMovementTracker) AntiBotModule.TRACKERS.get(player.getEntityId());
        if (data == null) return "No antibot data available";
        boolean bot = AntiBotModule.isBot(player.getEntityId());
        String trackedName = data.entity != null ? data.entity.getName() : null;
        return "teamStatus: " + data.field46
                + "\ntotalMoves: " + data.relativeMoves
                + "\nentityId: " + data.entityId + (data.entityId == player.getEntityId() ? " (matches)" : " (does NOT match)")
                + "\nname: " + trackedName + (Objects.equals(player.getGameProfile().getName(), trackedName) ? " (matches)" : " (does NOT match)")
                + "\nteleportBalance: " + data.teleports
                + "\ndefinitelyBot: " + (data.verdict != 0) + " (" + data.verdict + ")\nstatusPacket: " + data.metadataSeen
                + "\ndistanceSqMoved: " + data.squaredMovement + (data.lastPosition == null ? " (no last pos)" : "")
                + "\nteleportPacket: " + data.nonLookMoveSeen
                + "\ntotalExists: " + data.age
                + "\nlastRelMove: " + data.movementTimer.formatElapsed()
                + "\nlastTeleport: " + data.teleportTimer.formatElapsed()
                + "\nafkStreak: " + data.emptyMoveStreak
                + "\nConclusion (isBot()): " + (bot ? "Yes" : "No");
    }

    public static String describeTeam(EntityPlayer player) {
        if (player.worldObj == null) return null;
        ScorePlayerTeam team = player.worldObj.getScoreboard().getPlayersTeam(player.getGameProfile().getName());
        if (team == null) return null;
        return "getRegisteredName(): " + team.getRegisteredName()
                + "\ngetTeamName(): " + team.getTeamName()
                + "\ngetFlags(): " + team.func_98299_i()
                + "\ngetColorPrefix(): " + team.getColorPrefix()
                + "\ngetColorSuffix(): " + team.getColorSuffix()
                + "\ngetNameTagVisibility(): " + team.getNameTagVisibility()
                + "\ngetChatFormat(): " + team.getChatFormat()
                + "\ngetSeeFriendlyInvisiblesEnabled(): " + team.getSeeFriendlyInvisiblesEnabled()
                + "\ngetAllowFriendlyFire(): " + team.getAllowFriendlyFire()
                + "\ngetDeathMessageVisibility(): " + team.getDeathMessageVisibility();
    }

    @Override public String[] tabComplete(String input, String[] args) {
        String[] parts = input.split("\\s+");
        return parts.length == 0 ? null : PlayerNameCompleter.complete(parts[parts.length - 1], true);
    }

    @Override public void run(String label, String[] args) {
        if (args.length != 1) { sendTooLittleArguments(); return; }
        String report = describePlayer(args[0]);
        if (report == null) sendMessage(createMessage().format("No player found by {}! Make sure you're in range of the target before running this command.", args[0]));
        else ((DebugCommand) parent).printDebugData("Bot debug", report);
    }

    public static String describePlayer(String name) {
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer) entity;
            if (!player.getGameProfile().getName().equalsIgnoreCase(name)) continue;
            StringBuilder report = new StringBuilder();
            report.append("Issued By: ").append(Adjust.username).append(" (").append(mc.getSession().getUsername()).append(")\n");
            report.append("Entity: ").append(player.getGameProfile().getName()).append(" (").append(player.getEntityId()).append(")\n");
            report.append("Health: ").append(player.getHealth()).append("/").append(player.getMaxHealth()).append('\n');
            report.append("Absorption: ").append(player.getAbsorptionAmount()).append('\n');
            report.append("Ticks Existed: ").append(player.ticksExisted).append('\n');
            report.append("Total AntiBot Entries: ").append(AntiBotModule.TRACKERS.size()).append('\n');
            if (mc.thePlayer != null) report.append("Distance: ").append(player.getDistanceToEntity(mc.thePlayer)).append('\n');
            String team = describeTeam(player);
            if (team != null) report.append("---- Team ----\n").append(team).append('\n');
            report.append("---- Data ----\n").append(describeBot(player)).append('\n');
            return report.toString();
        }
        return null;
    }
}
