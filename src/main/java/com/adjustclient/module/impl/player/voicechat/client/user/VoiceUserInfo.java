package com.adjustclient.module.impl.player.voicechat.client.user;

import com.adjustclient.module.impl.player.voicechat.VoiceChatModule;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public class VoiceUserInfo {
    public static final UUID NO_CHANNEL = new UUID(0L, 0L);
    public final UUID userId;
    public final List<VoiceMute> reports = new ArrayList<>();
    public final List<TimestampedValue> mutes = new ArrayList<>();
    public final List<ModeratorNote> notes = new ArrayList<>();
    public final List<MuteCategory> muteCategories = new ArrayList<>();
    public UUID channelId = NO_CHANNEL;
    public String channelName;
    public TimestampedValue serverMute = new TimestampedValue(0, 0, null, null, null, null);
    public TimestampedValue localMute = new TimestampedValue(0, 0, null, null, null, null);
    public VoiceMute mostReportedReason;
    public boolean field44;
    public boolean field45;

    public VoiceUserInfo(UUID userId) { this.userId = userId; }

    public synchronized void read(JsonObject json) {
        mutes.clear();
        reports.clear();
        notes.clear();
        muteCategories.clear();
        channelId = NO_CHANNEL;
        channelName = null;
        if (json.has("channel")) {
            JsonObject channel = json.getAsJsonObject("channel");
            channelId = UUID.fromString(channel.get("id").getAsString());
            JsonElement name = channel.get("name");
            channelName = name == null ? null : name.getAsString();
        }
        TimestampedValue active = null;
        if (json.has("mutes")) {
            JsonArray array = json.getAsJsonArray("mutes");
            for (int i = 0; i < array.size(); i++) {
                JsonObject entry = array.get(i).getAsJsonObject();
                long start = entry.get("time_start").getAsLong();
                long end = entry.get("time_end").getAsLong();
                String reason = entry.get("reason").getAsString();
                String by = entry.get("muted_by_name").getAsString();
                String unmuted = entry.has("unmuted_by_name") ? entry.get("unmuted_by_name").getAsString() : null;
                String username = entry.has("main_username") ? entry.get("main_username").getAsString() : null;
                TimestampedValue mute = new TimestampedValue(start, end, reason, by, unmuted, username);
                mutes.add(mute);
                if (active == null && System.currentTimeMillis() < mute.timeEnd) active = mute;
            }
        } else if (json.has("mute_time_end")) {
            long end = json.get("mute_time_end").getAsLong();
            active = json.has("mute_reason")
                    ? TimestampedValue.create(end, json.get("mute_reason").getAsString())
                    : TimestampedValue.create(end);
        }
        serverMute = active == null ? new TimestampedValue(0, 0, null, null, null, null) : active;
        if (json.has("reports")) {
            JsonArray array = json.getAsJsonArray("reports");
            for (int i = 0; i < array.size(); i++) {
                JsonObject entry = array.get(i).getAsJsonObject();
                int id = entry.get("id").getAsInt();
                String reason = entry.get("reason").getAsString();
                String by = entry.get("reported_by").getAsString();
                long at = entry.get("reported_at").getAsLong();
                String address = entry.get("server_address").getAsString();
                UUID tape = entry.get("tape_id") == null ? null : UUID.fromString(entry.get("tape_id").getAsString());
                reports.add(new VoiceMute(id, reason, by, at, address, tape));
            }
        }
        if (json.has("notes")) {
            JsonArray array = json.getAsJsonArray("notes");
            for (int i = 0; i < array.size(); i++) {
                JsonObject entry = array.get(i).getAsJsonObject();
                notes.add(new ModeratorNote(entry.get("id").getAsInt(), entry.get("note").getAsString(),
                        entry.get("created_by_name").getAsString(), entry.get("updated_at").getAsLong()));
            }
        }
        if (json.has("mute_categories")) {
            JsonArray array = json.getAsJsonArray("mute_categories");
            for (int i = 0; i < array.size(); i++) {
                JsonObject entry = array.get(i).getAsJsonObject();
                muteCategories.add(new MuteCategory(entry.get("identifier").getAsString(),
                        entry.get("factor").getAsFloat(), entry.get("duration").getAsLong()));
            }
        }
        updateMostReportedReason();
    }

    public boolean isMuted() {
        TimestampedValue mute = serverMute;
        if (System.currentTimeMillis() >= mute.timeEnd) mute = localMute;
        else mute = serverMute;
        return mute.timeEnd > System.currentTimeMillis();
    }

    public boolean isSelf() {
        Session session = VoiceChatModule.INSTANCE.sessionManager.session;
        if (session == null) session = Minecraft.getMinecraft().getSession();
        return userId.equals(session.getProfile().getId());
    }

    public void updateMostReportedReason() {
        Map<String, Integer> counts = new HashMap<>();
        for (VoiceMute report : reports) {
            counts.put(MuteReason.fromId(report.reason).toLowerCase(), counts.getOrDefault(MuteReason.fromId(report.reason), 0) + 1);
        }
        String mostCommon = null;
        int highest = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > highest) {
                mostCommon = entry.getKey();
                highest = entry.getValue();
            }
        }
        if (mostCommon != null) {
            for (VoiceMute report : reports) {
                if (MuteReason.fromId(report.reason).equalsIgnoreCase(mostCommon)) {
                    mostReportedReason = report;
                    return;
                }
            }
        }
        mostReportedReason = null;
    }
}
