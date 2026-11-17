package com.adjustclient.game.rewound;

import com.adjustclient.event.ScoreboardRenderEvent;
import com.adjustclient.module.impl.player.autorewound.feature.PitFeature;
import com.adjustclient.util.TimeUtil;
import com.adjustclient.util.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatColor;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;

public final class RewoundPitGameState extends RewoundGameState {
    public static final Pattern KILL_PATTERN = Pattern.compile("^[A-Z ]*KILL! on .+? \\+([\\d,]+)XP \\+([\\d,.]+)g");
    public static final Pattern ASSIST_PATTERN = Pattern.compile("^ASSIST! \\d+% on .+? \\+([\\d,]+)XP \\+([\\d,.]+)g");
    public static final Pattern BOUNTY_CLAIMED_PATTERN = Pattern.compile("^BOUNTY CLAIMED! \\[\\d+\\] (\\S+) killed .+ for ([\\d,]+)g");

    public boolean inSpawn = true;
    public final Timer spawnTimer = new Timer();
    public Integer spawnHeight;
    public Vec3 spawnPosition;
    public StreakStats displayStreak;
    public StreakStats currentStreak = new StreakStats();
    public int spawnMarkerId = -1;
    public int lastStreak;
    public boolean nearSpawn;
    public boolean streakActive;
    public final Object lock = new Object();

    public RewoundPitGameState() {
        super(RewoundGame.PIT);
    }

    @Override
    public boolean isInSpawn() {
        return nearSpawn || !createdTimer.hasElapsed(1500L);
    }

    @Override
    public void onPlayerMove(C03PacketPlayer packet) {
        if (!inSpawn || packet == null || !packet.isMoving()) return;
        if (spawnHeight == null) {
            findSpawn();
        }
        if (spawnHeight != null && spawnTimer.hasElapsed(1200L) && packet.getPositionY() <= spawnHeight - 7.0D) {
            inSpawn = false;
        }
        if (inSpawn) {
            Entity marker = mc.theWorld.getEntityByID(spawnMarkerId);
            nearSpawn = marker != null && mc.thePlayer.getDistanceSqToEntity(marker) <= 4096.0D;
        } else {
            nearSpawn = false;
        }
    }

    @Override
    public void onPacket(Packet<?> packet, boolean before) {
        if (!before) return;
        if (packet instanceof S02PacketChat chat && chat.getType() != 2) {
            onChatMessage(StringUtils.stripControlCodes(chat.getChatComponent().getUnformattedText()));
        } else if (packet instanceof S08PacketPlayerPosLook position) {
            if (position.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y)) {
                return;
            }
            if (spawnHeight == null) {
                findSpawn();
            }
            if (spawnHeight != null && position.getY() >= spawnHeight - 5.0D && position.getY() <= spawnHeight + 4.5D) {
                nearSpawn = true;
                inSpawn = true;
                spawnTimer.reset();
            }
        }
        super.onPacket(packet, true);
    }

    public void findSpawn() {
        if (mc.theWorld == null || mc.theWorld.loadedEntityList == null) return;
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityArmorStand)) continue;
            String name = entity.getName().toLowerCase();
            if (name.contains("the hypixel pit") || name.contains("the rewound pit")) {
                spawnMarkerId = entity.getEntityId();
                spawnHeight = (int) entity.posY - 1;
                spawnPosition = new Vec3(entity.posX, entity.posY, entity.posZ);
                return;
            }
        }
    }

    private void onChatMessage(String message) {
        synchronized (lock) {
            StreakStats target = streakActive ? displayStreak : currentStreak;
            Matcher killMatcher = KILL_PATTERN.matcher(message);
            if (killMatcher.find()) {
                target.kills++;
                target.addRewards(killMatcher.group(1), killMatcher.group(2));
                return;
            }
            Matcher assistMatcher = ASSIST_PATTERN.matcher(message);
            if (assistMatcher.find()) {
                target.assists++;
                target.addRewards(assistMatcher.group(1), assistMatcher.group(2));
                return;
            }
            Matcher bountyMatcher = BOUNTY_CLAIMED_PATTERN.matcher(message);
            if (bountyMatcher.find()
                    && mc.getSession() != null
                    && bountyMatcher.group(1).equalsIgnoreCase(mc.getSession().getUsername())) {
                double claimed = Double.parseDouble(bountyMatcher.group(2).replace(",", ""));
                target.gold += claimed;
                target.bountiesClaimed += claimed;
            }
        }
    }

    private void updateStreakFromScoreboard(ScoreboardRenderEvent event) {
        Scoreboard scoreboard = event.objective.getScoreboard();
        int streak = -1;
        for (Score score : event.scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            String formatted = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName());
            String stripped = StringUtils.stripControlCodes(formatted).trim();
            if (stripped.startsWith("Streak:")) {
                String digits = stripped.replaceAll("[^0-9]", "");
                streak = digits.isEmpty() ? -1 : Integer.parseInt(digits);
                break;
            }
        }
        if (streakActive && (streak < 1 || lastStreak > streak)) {
            displayStreak.endTime = System.currentTimeMillis();
            streakActive = false;
            currentStreak = new StreakStats();
        }
        if (!streakActive && streak >= 1 && (lastStreak < 1 || lastStreak > streak)) {
            displayStreak = currentStreak;
            displayStreak.startTime = System.currentTimeMillis();
            streakActive = true;
            currentStreak = new StreakStats();
        }
        lastStreak = streak;
    }

    public void onScoreboardRender(ScoreboardRenderEvent event) {
        synchronized (lock) {
            updateStreakFromScoreboard(event);
            if (displayStreak == null || PitFeature.INSTANCE == null || !PitFeature.INSTANCE.shouldShowActiveStreak()) {
                return;
            }
            String title;
            long durationMillis;
            if (streakActive) {
                title = ChatColor.WHITE + "Streak (" + ChatColor.GREEN + "Active" + ChatColor.WHITE + ")";
                durationMillis = System.currentTimeMillis() - displayStreak.startTime;
            } else {
                title = ChatColor.WHITE + "Streak (" + ChatColor.YELLOW + "Last" + ChatColor.WHITE + ")";
                durationMillis = displayStreak.endTime - displayStreak.startTime;
            }
            List<String> lines = new ArrayList<>();
            lines.add("");
            lines.add(ChatColor.WHITE + "Kills: " + ChatColor.GREEN + displayStreak.kills);
            lines.add(ChatColor.WHITE + "Assists: " + ChatColor.GREEN + displayStreak.assists);
            lines.add("");
            lines.add(ChatColor.WHITE + "Gold: " + ChatColor.GOLD + String.format("%,.2fg", displayStreak.gold));
            lines.add(ChatColor.WHITE + "XP: " + ChatColor.AQUA + String.format("%,d", displayStreak.xp));
            lines.add("");
            if (displayStreak.bountiesClaimed > 0.0D) {
                lines.add(ChatColor.WHITE + "Bounties Claimed: " + ChatColor.GOLD + String.format("%,.0fg", displayStreak.bountiesClaimed));
                lines.add("");
            }
            lines.add(ChatColor.WHITE + "Duration: " + ChatColor.YELLOW + TimeUtil.formatDuration(durationMillis, false));
            event.overlay = new PitStreakScoreboard(title, lines);
        }
    }

    public static final class StreakStats {
        public int kills;
        public double gold;
        public double bountiesClaimed;
        public long startTime;
        public long endTime;
        public int assists;
        public int xp;

        public void addRewards(String xpText, String goldText) {
            xp += (int) Double.parseDouble(xpText.replace(",", ""));
            gold += Double.parseDouble(goldText.replace(",", ""));
        }
    }
}
