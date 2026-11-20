package com.adjustclient.utilities.anticheat.entity;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.modules.impl.render.hud.playerlist.stats.BedWarsTeam;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.anticheat.ServerTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;

public final class TrackedPlayer extends TrackedLivingEntity {
    public Entity field52;
    public Object field56;
    public TrackedPlayer(EntityLivingBase entity) { super(entity); }

    public static BedWarsTeam parseTeam(String text) {
        BedWarsTeam result = null;
        for (int index = 0; index + 1 < text.length(); index += 2) {
            if (text.charAt(index) != '\u00a7') return result;
            char code = Character.toLowerCase(text.charAt(index + 1));
            if (code == 'r' || "0123456789abcdef".indexOf(code) >= 0) {
                result = null;
                for (BedWarsTeam team : BedWarsTeam.VALUES) {
                    if (team.chatColor.getFormattingCode() == code) { result = team; break; }
                }
            }
        }
        return result;
    }

    public boolean matchesType(int mask) {
        if ((field52 != null || entity.hasCustomName()) && (mask & 0x20) == 0) {
            var blocker = ServerTracker.INSTANCE.serverState;
            if (blocker != null && blocker.antiCheatModel != null
                    && blocker.antiCheatModel.serverType == ServerType.HYPIXEL
                    && Adjust.gameStateTracker.state instanceof BedWarsGameState) {
                BedWarsGameState game = (BedWarsGameState) Adjust.gameStateTracker.state;
                if (game.startTimer != null) {
                    String label = entity.getDisplayName().getFormattedText();
                    if (parseTeam(label) == null && field52 != null) label = field52.getDisplayName().getFormattedText();
                    BedWarsTeam targetTeam = parseTeam(label);
                    if (targetTeam != null) {
                        Minecraft mc = Minecraft.getMinecraft();
                        BedWarsTeam ownTeam = game.findPlayerTeam(mc.getSession().getUsername());
                        if (ownTeam == null && mc.thePlayer.getTeam() != null)
                            ownTeam = BedWarsTeam.byTeamPrefix((net.minecraft.scoreboard.ScorePlayerTeam) mc.thePlayer.getTeam());
                        if (ownTeam != null && targetTeam == ownTeam) return false;
                    }
                }
            }
        }
        return super.matchesType(mask);
    }

    public void update() {
        super.update();
        field52 = null;
        var blocker = ServerTracker.INSTANCE.serverState;
        if (blocker == null || blocker.antiCheatModel == null
                || blocker.antiCheatModel.serverType != ServerType.HYPIXEL) return;
        if (!(Adjust.gameStateTracker.state instanceof BedWarsGameState)) return;
        BedWarsGameState game = (BedWarsGameState) Adjust.gameStateTracker.state;
        if (game.startTimer == null || removed) return;
        double closest = Double.MAX_VALUE;
        for (Entity other : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            boolean aligned = (Math.abs(other.posX - entity.posX) <= 0.03125 && Math.abs(other.posZ - entity.posZ) <= 0.03125)
                    || (Math.abs(other.posX - entity.prevPosX) <= 0.03125 && Math.abs(other.posZ - entity.prevPosZ) <= 0.03125)
                    || (Math.abs(other.prevPosX - entity.posX) <= 0.03125 && Math.abs(other.prevPosZ - entity.posZ) <= 0.03125)
                    || (Math.abs(other.serverPosX / 32.0 - entity.serverPosX / 32.0) <= 0.03125
                    && Math.abs(other.serverPosZ / 32.0 - entity.serverPosZ / 32.0) <= 0.03125);
            double height = Math.abs(other.posY - entity.posY);
            if (aligned && height <= 3.0 && other instanceof EntityArmorStand && !other.isDead
                    && other.hasCustomName() && parseTeam(other.getCustomNameTag()) != null && height < closest) {
                field52 = other;
                closest = height;
            }
        }
    }
}
