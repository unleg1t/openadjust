package com.adjustclient.utilities.anticheat.model.check;
import com.adjustclient.utilities.anticheat.model.AntiCheatCheck;
import com.adjustclient.utilities.Timer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
public final class JoinGameCheck extends AntiCheatCheck {
    public boolean field6;
    public Timer timer;
    public Integer previousScore;
    public Object field16;
    public boolean onPacket(Packet<?> packet) {
        if (field6) {
            if (packet instanceof S01PacketJoinGame) field6 = false;
            return false;
        }
        if (!(packet instanceof S3CPacketUpdateScore)) {
            if (timer != null && timer.hasElapsed(60L)) {
                timer = null;
                return previousScore != null;
            }
            return false;
        }
        S3CPacketUpdateScore score = (S3CPacketUpdateScore) packet;
        String name = score.getPlayerName();
        if (name.isEmpty() || name.length() >= 3 || score.getScoreAction() != S3CPacketUpdateScore.Action.CHANGE) return false;
        if (!name.chars().anyMatch(c -> c >= 256)) return false;
        int value = score.getScoreValue();
        if (previousScore != null) {
            if (value < 1 || value != previousScore - 1) {
                timer = null;
                previousScore = null;
                field6 = true;
                return false;
            }
        } else {
            timer = new Timer();
        }
        previousScore = value;
        timer.reset();
        return false;
    }
}
