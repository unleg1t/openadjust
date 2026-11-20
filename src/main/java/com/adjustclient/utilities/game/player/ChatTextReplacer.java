package com.adjustclient.utilities.game.player;

import com.adjustclient.core.events.*;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.anticheat.ServerTracker;
import java.util.*;

public final class ChatTextReplacer implements Listener {
    private static final Map<String, String> GLOBAL_ALIASES;
    private static final Map<String, String> HYPIXEL_ALIASES;
    static {
        Map<String, String> global = new LinkedHashMap<>();
        global.put("/gmc", "/gamemode creative");
        global.put("/gms", "/gamemode survival");
        global.put("/gmsp", "/gamemode spectator");
        global.put("/gma", "/gamemode adventure");
        GLOBAL_ALIASES = Collections.unmodifiableMap(global);
        Map<String, String> hypixel = new LinkedHashMap<>();
        hypixel.put("/rq", "/requeue");
        hypixel.put("/play bw1", "/play bedwars_eight_one");
        hypixel.put("/play bw2", "/play bedwars_eight_two");
        hypixel.put("/play bw3", "/play bedwars_four_three");
        hypixel.put("/play bw4", "/play bedwars_four_four");
        hypixel.put("/play swnormal", "/play solo_normal");
        hypixel.put("/play swinsane", "/play solo_insane");
        hypixel.put("/play swnormalteams", "/play teams_normal");
        hypixel.put("/play swinsaneteams", "/play teams_insane");
        hypixel.put("/play classic", "/play duels_classic_duel");
        hypixel.put("/play sumo", "/play duels_sumo_duel");
        hypixel.put("/play uhc", "/play duels_uhc_duel");
        hypixel.put("/play tnttag", "/play tnt_tntag");
        hypixel.put("/play tntrun", "/play tnt_tntrun");
        HYPIXEL_ALIASES = Collections.unmodifiableMap(hypixel);
    }
    private Object field4;

    @EventHandler public void onChatSend(ChatSendEvent event) {
        if (!event.cancelled && event.addToHistory) event.message = replace(event.message);
    }

    public String replace(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        String result = replace(text, lower, GLOBAL_ALIASES);
        if (result != null) return result;
        if (ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL) {
            result = replace(text, lower, HYPIXEL_ALIASES);
            if (result != null) return result;
        }
        return text;
    }

    public String replace(String original, String lower, Map<String, String> aliases) {
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            String key = entry.getKey();
            if (lower.equals(key) || lower.startsWith(key + " "))
                return entry.getValue() + original.substring(key.length());
        }
        return null;
    }

    public List<String> getCompletions(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        int start = lower.lastIndexOf(' ') + 1;
        Set<String> matches = new LinkedHashSet<>();
        collectCompletions(lower, start, GLOBAL_ALIASES, matches);
        if (ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL)
            collectCompletions(lower, start, HYPIXEL_ALIASES, matches);
        return new ArrayList<>(matches);
    }

    public void collectCompletions(String lower, int start, Map<String, String> aliases, Set<String> results) {
        for (String key : aliases.keySet()) {
            if (key.startsWith(lower)) {
                int end = key.indexOf(' ', start);
                if (end < 0) end = key.length();
                results.add(key.substring(start, end));
            }
        }
    }
}

