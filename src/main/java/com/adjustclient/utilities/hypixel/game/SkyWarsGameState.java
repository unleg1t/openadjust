package com.adjustclient.utilities.hypixel.game;

import com.adjustclient.utilities.hypixel.HypixelGame;
import com.google.gson.JsonObject;
import com.adjustclient.utilities.game.server.GameCountdown;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Locale;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.util.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public final class SkyWarsGameState extends TabTrackingGameState implements ScoreboardLineProvider, AliveCountProvider {
    public GameCountdown countdown;
    public Boolean largeGame;
    public Long countdownEnd;
    public Integer heightLimit;
    public Integer initialFloor;
    public Object field72;
    public static JsonObject heightLimits;
    static { heightLimits = maps == null ? null : maps.getAsJsonObject("height_limits").getAsJsonObject("skywars"); }
    public SkyWarsGameState() { super(HypixelGame.SKYWARS); }
    public boolean isLargeGame() { return largeGame != null && largeGame; }
    public int getTrackingDelay() { return 10000; }
    @Override public void onPacket(Packet<?> packet, boolean before) {
        super.onPacket(packet, before);
        if (!before) return;
        if (packet instanceof S08PacketPlayerPosLook) {
            if (initialFloor != null || lobby == null || lobby || (startTimer != null) != isLargeGame()) return;
            S08PacketPlayerPosLook pos = (S08PacketPlayerPosLook) packet;
            if (pos.getX() * pos.getX() + pos.getZ() * pos.getZ() > 32.0) initialFloor = (int) pos.getY() - 2;
            return;
        }
        if (!(packet instanceof S3EPacketTeams) || lobby == null || lobby) return;
        S3EPacketTeams team = (S3EPacketTeams) packet;
        Pair<GameCountdown, Boolean> parsed = parseCountdown(team);
        if (parsed.getRight()) {
            GameCountdown old = countdown;
            countdown = parsed.getLeft();
            if (countdown != null && old == null) countdownEnd = System.currentTimeMillis() + countdown.countdown() + 1000;
            return;
        }
        if (startTimer != null) return;
        String text = team.getPrefix() + team.getSuffix();
        if ("team_4".equals(team.getName())) {
            if (!text.contains(": §a")) return;
            String key = StringUtils.stripControlCodes(text.split(": §a")[1]).toLowerCase(Locale.ENGLISH).replaceAll(" ", "_");
            if (heightLimits != null && heightLimits.has(key)) heightLimit = heightLimits.get(key).getAsInt();
        } else if ("team_6".equals(team.getName())) {
            if (!text.matches(".*\u00a7a\\d{1,2}.*")) { countdownEnd = null; return; }
            IntArrayList numbers = extractNumbers(text);
            if (!numbers.isEmpty()) {
                long end = System.currentTimeMillis() + 1000L * numbers.getInt(numbers.size() - 1);
                if (numbers.size() >= 2) end += 60000L * numbers.getInt(numbers.size() - 2);
                countdownEnd = end;
            }
        } else if (largeGame == null && a_(team)) largeGame = true;
    }
    public static IntArrayList extractNumbers(String text) {
        IntArrayList result = new IntArrayList();
        for (String part : text.contains(" ") ? text.split(" ") : new String[]{text}) {
            String digits = part.replaceAll("\\D", "");
            if (!digits.isEmpty()) result.add(Integer.parseInt(digits));
        }
        return result;
    }
    @Override public Integer getAdditionalData() { return heightLimit; }
}
