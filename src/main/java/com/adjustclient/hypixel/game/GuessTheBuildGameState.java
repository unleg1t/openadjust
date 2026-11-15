package com.adjustclient.hypixel.game;

import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.hypixel.HypixelGameState;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.*;
import org.apache.commons.io.IOUtils;
import net.minecraft.util.ChatColor;

public final class GuessTheBuildGameState extends HypixelGameState {
    public String hint;
    public final List<String> matches = new ArrayList<>();
    public final List<String> guesses = new ArrayList<>();
    public Object field56;
    public static List<String> themes;
    static {
        themes = List.of();
        CompletableFuture.runAsync(() -> loadThemes());
    }
    public GuessTheBuildGameState() { super(HypixelGame.GUESS_THE_BUILD); }
    public void updateMatches() {
        matches.clear();
        if (hint == null) return;
        char[] pattern = hint.toCharArray();
        for (String theme : themes) {
            if (guesses.contains(theme.toLowerCase())) continue;
            char[] word = theme.toCharArray();
            if (pattern.length != word.length) continue;
            boolean match = true;
            for (int i = 0; i < pattern.length; i++) {
                if (pattern[i] == '_' ? word[i] == ' ' : pattern[i] != word[i]) { match = false; break; }
            }
            if (match) matches.add(theme);
        }
    }
    public static void loadThemes() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create("https://raw.githubusercontent.com/DrRed96/Hypixel-Guess-The-Build-Theme-List/refs/heads/master/themes.txt").toURL().openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(2000);
            String text = IOUtils.toString(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            mc.addScheduledTask(() -> parseThemes(text));
            return;
        } catch (Exception ignored) { }
        try (InputStream input = mc.getResourceManager().getResource(new ResourceLocation("adjust", "data/guess_the_build.txt")).getInputStream()) {
            String text = IOUtils.toString(input, StandardCharsets.UTF_8);
            Logger.log(LogLevel.WARN, true, "Using backup guess the build theme list");
            mc.addScheduledTask(() -> parseThemes(text));
        } catch (Exception exception) {
            Logger.log(LogLevel.ERROR, true, "Couldn't load guess the build theme list");
            NotificationBuilder notification = new NotificationBuilder();
            notification.title = "Error";
            notification.parts = new ArrayList<>();
            notification.actions = new ArrayList<>();
            notification.addText("Couldn't load guess the build themes.", (int) 4290690237L);
            notification.lifetimeSteps = 100;
            notification.show();
        }
    }
    @Override public void onPacket(Packet<?> packet, boolean before) {
        super.onPacket(packet, before);
        if (!before || !(packet instanceof S02PacketChat)) return;
        S02PacketChat chat = (S02PacketChat) packet;
        IChatComponent message = chat.getChatComponent();
        if (chat.getType() == 2) {
            String[] parts = message.getFormattedText().split("§e");
            String value = StringUtils.stripControlCodes(parts[parts.length - 1]).trim();
            hint = !value.isEmpty() && value.matches("[A-Za-z_ ]+") ? value.trim() : null;
            return;
        }
        List<IChatComponent> siblings = message.getSiblings();
        if (siblings.size() == 4 && siblings.get(1).getUnformattedText().matches("\\d")
                && "/".equals(siblings.get(2).getUnformattedText()) && siblings.get(3).getUnformattedText().matches("\\d")) {
            hint = null;
            matches.clear();
            guesses.clear();
            return;
        }
        if (siblings.size() < 4) return;
        IChatComponent last = siblings.get(siblings.size() - 1);
        if (last.getChatStyle() == null) return;
        ChatColor color = last.getChatStyle().getColor();
        if (color != ChatColor.GRAY && color != ChatColor.WHITE) return;
        String text = last.getUnformattedText();
        if (text.startsWith(": ")) guesses.add(text.substring(2).toLowerCase().trim());
    }
    public static void parseThemes(String text) {
        themes = Arrays.stream(text.split("\n"))
                .filter(line -> !line.isBlank())
                .map(line -> line.trim()).toList();
    }
    @Override public void onUpdate() { updateMatches(); }
}
