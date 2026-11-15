package com.adjustclient.module.impl.render.streamer;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.StepAttemptEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.util.MinecraftInstance;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentScore;
import net.minecraft.util.ChatComponentSelector;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;

public final class StreamerModule extends Module implements MinecraftInstance {
    public static final Pattern CHAT_SENDER = Pattern.compile("^(?:\\[[^]]+]\\s+)*([A-Za-z0-9_]{1,16})\\s*:\\s");
    public static com.google.common.collect.BiMap<String, String> nameMap;
    public static Map<String, String> reverseNameMap;
    public static StreamerModule INSTANCE;
    public static Object nameRefreshLock;
    public static com.adjustclient.util.Timer nameRefreshTimer;
    private static final Pattern SERVER_ID = Pattern.compile("\\b(?:dynamiclobby|mini|mega)\\d+[A-Z]{1,2}\\b");
    private static final ArrayList<String> names = new ArrayList<>();
    public static ModeSetting<StreamerNameType> nameType;
    public StreamerName serverNames;

    static { nameRefreshLock = new Object(); nameRefreshTimer = new com.adjustclient.util.Timer(); }

    public StreamerModule() {
        super("Streamer", Category.RENDER);
        INSTANCE = this;
        nameType = new ModeSetting<>(this, "Name Type", StreamerNameType.USERNAMES, StreamerNameType.values());
    }

    @EventHandler public void getScoreboardName(com.adjustclient.event.ScoreboardEntryEvent event) {
        if (!isEnabled() && com.adjustclient.service.config.user.UserConfiguration.field2f84 == null) return;
        Score score = event.score;
        Scoreboard scoreboard = score.getScoreScoreboard();
        String text = ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(score.getPlayerName()), score.getPlayerName());
        String replacement = text.length() < 4 ? null : maskNamesInText(text);
        if (replacement != null) {
            Score masked = new Score(scoreboard, score.getObjective(), replacement);
            masked.setScorePoints(score.getScorePoints());
            masked.setLocked(score.isLocked());
            event.score = masked;
        }
        if (isEnabled() && text.startsWith(net.minecraft.util.ChatColor.GRAY.toString())
                && text.indexOf('/') >= 0 && text.contains(" ") && text.split(" ").length == 3) {
            String last = text.split(" ")[2];
            String stripped = StringUtils.stripControlCodes(last);
            if (stripped != null) {
                String name = stripped.replaceAll("[^a-zA-Z0-9]", "");
                if (serverNames == null || !serverNames.matches(name)) serverNames = new StreamerName(name);
                String maskedText = text.substring(0, text.lastIndexOf(' ')) + " " + last.substring(0, 2) + serverNames.display(true);
                Score masked = new Score(scoreboard, score.getObjective(), maskedText);
                masked.setScorePoints(score.getScorePoints());
                masked.setLocked(score.isLocked());
                event.score = masked;
            }
        }
    }
    public static String maskNamesInText(String text) {
        if (nameMap == null) return null;
        boolean truncated = text.contains("§7...§7");
        String result = null;
        for (Map.Entry<String, String> entry : nameMap.entrySet()) {
            String current = result == null ? text : result;
            String name = entry.getKey();
            if (truncated && name.length() >= 12) {
                String prefix = name.substring(0, 11);
                if (current.contains(prefix)) {
                    result = current.replaceAll(Pattern.quote(prefix) + "(.){1,2}§7\\.\\.\\.§7", entry.getValue());
                    continue;
                }
            }
            if (current.contains(name)) result = current.replace(name, entry.getValue());
        }
        return result;
    }
    public static String restoreNameInText(String text, String from) {
        if (nameMap == null) return text.replace(from, from);
        String original;
        synchronized (nameRefreshLock) {
            original = nameMap.inverse().getOrDefault(from, from);
        }
        return text.replace(from, original);
    }
    public static String maskNameIfEnabled(String name) { return INSTANCE.isEnabled() ? INSTANCE.replaceNames(name) : name; }
    public static String lookupMaskedName(String text) {
        if (nameMap == null) return text;
        synchronized (nameRefreshLock) { return nameMap.getOrDefault(text, text); }
    }
    @Override public void onEnable() {
        boolean active = INSTANCE.isEnabled()
                || com.adjustclient.service.config.user.UserConfiguration.field2f84 != null;
        if ((nameMap != null) != active) {
            synchronized (nameRefreshLock) {
                nameMap = active ? com.google.common.collect.HashBiMap.create() : null;
                reverseNameMap = nameMap == null ? null : nameMap.inverse();
            }
            INSTANCE.serverNames = null;
        }
        refreshWorldNames();
        nameRefreshTimer.reset();
        com.adjustclient.Adjust.chatManager.minecraftChannel.history.refreshChat();
    }
    @Override public void onDisable() {
        boolean active = INSTANCE.isEnabled()
                || com.adjustclient.service.config.user.UserConfiguration.field2f84 != null;
        if ((nameMap != null) != active) {
            synchronized (nameRefreshLock) {
                nameMap = active ? com.google.common.collect.HashBiMap.create() : null;
                reverseNameMap = nameMap == null ? null : nameMap.inverse();
            }
            INSTANCE.serverNames = null;
        }
        com.adjustclient.Adjust.chatManager.minecraftChannel.history.refreshChat();
    }
    @Override public String getDescription() { return "Hides player names and game information."; }
    @EventHandler public void onChatReceived(com.adjustclient.event.ChatLineEvent event) {
        if (!isEnabled() && com.adjustclient.service.config.user.UserConfiguration.field2f84 == null) return;
        if (!event.pre || event.chat != com.adjustclient.Adjust.chatManager.minecraftChannel.history) return;
        try {
            var server = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
            if (server != null && server.antiCheatModel != null
                    && server.antiCheatModel.serverType == com.adjustclient.server.ServerType.HYPIXEL) {
                String sender = getChatSenderName(event.component);
                if (sender != null) replaceNames(sender);
            }
            if (event.component instanceof ChatComponentTranslation translation
                    && "chat.type.text".equals(translation.getKey())) {
                Object[] arguments = translation.getFormatArgs();
                if (arguments.length != 0) {
                    Object first = arguments[0];
                    String sender = first instanceof IChatComponent chat ? chat.getUnformattedTextForChat()
                            : first instanceof String ? (String) first : null;
                    if (sender != null) {
                        sender = StringUtils.stripControlCodes(sender);
                        if (sender != null && Pattern.matches("^\\w{2,16}$", sender)) replaceNames(sender);
                    }
                }
            }
            IChatComponent copy = event.component.createCopy();
            replaceComponent(copy);
            event.component = copy;
        } catch (RuntimeException error) {
            com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.WARN, true,
                    "Failed to replace chat message: {}", error);
        }
    }
    @EventHandler public void onWorldJoinPacket(com.adjustclient.event.DirectionalPacketEvent event) {
        if (event.isCancelled()) return;
        if (!isEnabled() && com.adjustclient.service.config.user.UserConfiguration.field2f84 == null) return;
        if (!event.outgoing && event.packet instanceof net.minecraft.network.play.server.S01PacketJoinGame) {
            serverNames = null;
            synchronized (nameRefreshLock) {
                if (nameMap != null) {
                    nameMap = com.google.common.collect.HashBiMap.create();
                    reverseNameMap = nameMap.inverse();
                }
            }
            nameRefreshTimer.reset();
        }
    }
    @EventHandler public void onPacketReceive(com.adjustclient.event.PacketAppliedEvent event) {
        if (!isEnabled() && com.adjustclient.service.config.user.UserConfiguration.field2f84 == null) return;
        if (event.packet instanceof net.minecraft.network.play.server.S38PacketPlayerListItem packet) {
            if (packet.getAction() != net.minecraft.network.play.server.S38PacketPlayerListItem.Action.ADD_PLAYER) return;
            for (net.minecraft.network.play.server.S38PacketPlayerListItem.AddPlayerData data : packet.getEntries()) {
                replaceNames(data.getProfile().getName());
            }
        } else if (event.packet instanceof net.minecraft.network.play.server.S01PacketJoinGame) {
            refreshWorldNames();
        }
    }
    public static String replaceKnownNames(String text) {
        if (nameMap == null) return text;
        for (Map.Entry<String, String> entry : nameMap.entrySet())
            text = text.replace(entry.getKey(), entry.getValue());
        return text;
    }
    private void maskServerIds(IChatComponent component) {
        if (component.getChatStyle().getColor() != net.minecraft.util.ChatColor.GREEN) return;
        ChatComponentText text = (ChatComponentText) component;
        String original = text.getUnformattedTextForChat();
        if (!original.contains(" ")) return;
        for (String word : original.split(" ")) {
            String name = word.replaceAll("[^a-zA-Z0-9]", "");
            if (SERVER_ID.matcher(name).matches()) {
                StreamerName replacement = new StreamerName(name);
                text.text = original.replaceAll(name, replacement.display(false));
            }
        }
    }
    public static String getChatSenderName(IChatComponent component) {
        String clickedName = extractProfileClickName(component);
        if (clickedName != null) return clickedName;
        for (IChatComponent sibling : component.getSiblings()) {
            clickedName = extractProfileClickName(sibling);
            if (clickedName != null) return clickedName;
        }
        String formatted = component.getFormattedText();
        int colon = formatted.indexOf(':');
        if (colon < 2 || !(formatted.regionMatches(true, colon - 2, "§f", 0, 2)
                || formatted.regionMatches(true, colon - 2, "§7", 0, 2))) return null;
        java.util.regex.Matcher matcher = CHAT_SENDER.matcher(StringUtils.stripControlCodes(formatted));
        if (!matcher.find()) return null;
        String sender = matcher.group(1);
        return sender != null && sender.matches("^\\w{2,16}$") ? sender : null;
    }
    public static String replaceName(String text, String name) {
        return text.replace(name, replaceNames(name));
    }
    public static IChatComponent extractSenderComponent(IChatComponent component, String sender) {
        String formatted = component.getFormattedText();
        int end = formatted.indexOf(':');
        if (end < 0) return new ChatComponentText(sender);
        if (end >= 2 && (formatted.regionMatches(true, end - 2, "§f", 0, 2)
                || formatted.regionMatches(true, end - 2, "§7", 0, 2))) end -= 2;
        return new ChatComponentText(formatted.substring(0, end));
    }
    public static String extractProfileClickName(IChatComponent component) {
        net.minecraft.event.ClickEvent click = component.getChatStyle().getChatClickEvent();
        if (click == null || click.getAction() != net.minecraft.event.ClickEvent.Action.RUN_COMMAND) return null;
        String command = click.getValue();
        if (!command.regionMatches(true, 0, "/viewprofile ", 0, 13)) return null;
        String name = command.substring(13).trim();
        return name.matches("^\\w{2,16}$") ? name : null;
    }
    private void replaceComponent(IChatComponent component) {
        if (component instanceof ChatComponentText text) {
            text.text = replaceKnownNames(text.getUnformattedTextForChat());
            maskServerIds(text);
        } else if (component instanceof ChatComponentTranslation translation) {
            Object[] arguments = translation.getFormatArgs();
            for (int i = 0; i < arguments.length; i++) {
                Object argument = arguments[i];
                if (argument instanceof IChatComponent child) replaceComponent(child);
                else if (argument instanceof String text) arguments[i] = replaceKnownNames(text);
            }
        }
        for (IChatComponent sibling : component.getSiblings()) replaceComponent(sibling);
    }
    public static String replaceNames(String text) {
        if (nameMap == null) return text;
        synchronized (nameRefreshLock) {
            String existing = nameMap.get(text);
            if (existing != null) return existing;
            if (nameMap.size() >= 1025 && nameRefreshTimer.hasElapsed(60000L)) refreshPlayerNames();
            String replacement = text;
            if (text != null && Pattern.matches("^\\w{2,16}$", text)) {
                String localName = com.adjustclient.service.config.user.UserConfiguration.field2f84;
                if (localName != null && mc.getSession().getUsername().equals(text)) replacement = localName;
                else if (INSTANCE.isEnabled()) replacement = nameType.getValue().createGenerator(text).generate();
            }
            int suffix = 0;
            String unique;
            do {
                unique = String.valueOf(replacement) + (suffix == 0 ? "" : Integer.toString(suffix));
                suffix++;
            } while (reverseNameMap.get(unique) != null);
            nameMap.forcePut(text, unique);
            return unique;
        }
    }
    public static void refreshPlayerNames() {
        synchronized (nameRefreshLock) {
            if (nameMap != null) {
                nameMap = com.google.common.collect.HashBiMap.create();
                reverseNameMap = nameMap.inverse();
            }
        }
        nameRefreshTimer.reset();
        refreshWorldNames();
    }
    public static void refreshWorldNames() {
        if (nameMap == null) return;
        replaceNames(mc.getSession().getUsername());
        if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) return;
        for (NetworkPlayerInfo player : mc.thePlayer.sendQueue.getPlayerInfoMap()) {
            replaceNames(player.getGameProfile().getName());
        }
    }
    private void rebuildFromPlayerList() {
        synchronized (nameRefreshLock) {
            if (nameMap != null) {
                nameMap = com.google.common.collect.HashBiMap.create();
                reverseNameMap = nameMap.inverse();
            }
        }
        nameRefreshTimer.reset();
        refreshWorldNames();
    }

    private static String createReplacement(String name) {
        StreamerNameType type = nameType.getValue();
        String generated = type.createGenerator(name).generate();
        return generated == null || generated.isEmpty() ? "Player" : generated;
    }
}
