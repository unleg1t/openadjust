package com.adjustclient.module.impl.render.esp;

import com.adjustclient.Adjust;
import com.adjustclient.tag.PlayerTag;
import com.adjustclient.util.anticheat.entity.TrackedFakePlayer;
import java.util.function.Predicate;
import net.minecraft.entity.player.EntityPlayer;

public enum EspPlayerFlag {
    WHITELISTED("Whitelisted", "FRD", tracked -> {
        return Adjust.INSTANCE.tagManager.getTag(((EntityPlayer) tracked.getEntity()).getGameProfile().getName()) == PlayerTag.WHITELIST;
    }),
    TARGETED("Targeted", "ENM", tracked -> {
        return Adjust.INSTANCE.tagManager.getTag(((EntityPlayer) tracked.getEntity()).getGameProfile().getName()) == PlayerTag.TARGET;
    }),
    HACKER("Hacker", "HKR", tracked -> {
        var player = com.adjustclient.module.impl.exploit.playerdetect.hackerdetect.HackerDetector.PLAYERS.get(tracked.entityId);
        return player != null && player.cheater;
    }),
    MURDERER("Murderer", "MDR", tracked -> {
        return com.adjustclient.module.impl.exploit.murdermystery.MurderMysteryModule.hasRole(
                tracked.getEntity().getDisplayName().getFormattedText(),
                com.adjustclient.hypixel.game.murdermystery.MurderMysteryRole.MURDERER);
    }),
    DETECTIVE("Detective", "DTC", tracked -> {
        return com.adjustclient.module.impl.exploit.murdermystery.MurderMysteryModule.hasRole(
                tracked.getEntity().getDisplayName().getFormattedText(),
                com.adjustclient.hypixel.game.murdermystery.MurderMysteryRole.DETECTIVE);
    }),
    AUTO_BLOCK("Auto block", "AB", tracked -> {
        var player = com.adjustclient.module.impl.exploit.playerdetect.hackerdetect.HackerDetector.PLAYERS.get(tracked.entityId);
        return player != null && player.heldItemCheckFailed;
    }),
    CREATIVE_MODE("Creative Mode", "GMC", tracked -> {
        var server = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
        return (server == null || server.antiCheatModel == null || server.antiCheatModel.serverType != com.adjustclient.server.ServerType.HYPIXEL)
                && ((EntityPlayer) tracked.getEntity()).capabilities.isCreativeMode;
    }),
    TALKING("Talking", "TLK", tracked -> {
        var voiceChat = com.adjustclient.module.impl.player.voicechat.VoiceChatModule.INSTANCE;
        var streams = voiceChat != null ? voiceChat.getAudioStreamManager() : null;
        var stream = streams != null ? streams.streams.get(tracked.getEntity().getUniqueID()) : null;
        return stream != null && stream.isTalking();
    }),
    BLOCKING("Blocking", "BLK", tracked -> {
        return ((EntityPlayer) tracked.getEntity()).isBlocking();
    }),
    SNEAKING("Sneaking", "SNK", tracked -> {
        return tracked.getEntity().isSneaking();
    });

    public final String name;
    public final String label;
    public final Predicate<TrackedFakePlayer> predicate;
    public final java.awt.Color defaultColor;
    public final boolean enabledByDefault;
    public Object field36;
    EspPlayerFlag(String name, String label, Predicate<TrackedFakePlayer> predicate) {
        this.name = name;
        this.label = label;
        this.predicate = predicate;
        enabledByDefault = ordinal() < 8;
        defaultColor = switch (ordinal()) {
            case 0 -> new java.awt.Color(70, 70, 200);
            case 1 -> new java.awt.Color(160, 40, 40);
            case 2 -> new java.awt.Color(200, 70, 70);
            case 3 -> new java.awt.Color(208, 67, 67);
            case 4 -> new java.awt.Color(26, 86, 154);
            case 5 -> new java.awt.Color(255, 217, 99);
            case 6 -> new java.awt.Color(149, 59, 190);
            case 7 -> new java.awt.Color(60, 222, 87);
            default -> null;
        };
    }
    public boolean test(TrackedFakePlayer entity) { return predicate.test(entity); }
}
