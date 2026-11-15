package com.adjustclient.ui.stats;

import com.adjustclient.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.TimeUtil;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import com.adjustclient.render.font.TextRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiSlot;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import com.adjustclient.Adjust;

class StatsList extends GuiSlot {
    private boolean statusPending;
    protected final HypixelStatsScreen screen;

    StatsList(HypixelStatsScreen screen) {
        super(Minecraft.getMinecraft(), screen.width, screen.height, 32, screen.height - 64, 10);
        this.screen = screen;
        registerScrollButtons(1, 1);
    }

    @Override protected void drawBackground() {
        if (mc.theWorld != null) Gui.drawRect(0, 0, screen.width, screen.height, (int) 3222278160L);
        else screen.drawDefaultBackground();
    }

    public String titleCase(String text) {
        if (text.contains(" ")) {
            StringBuilder result = new StringBuilder();
            for (String word : text.split(" ")) result.append(StringUtils.capitalize(word.toLowerCase())).append(" ");
            return result.toString().trim();
        }
        return StringUtils.capitalize(text.toLowerCase());
    }

    @Override protected void elementClicked(int index, boolean doubleClick, int mouseX, int mouseY) {}

    @Override protected void drawSlot(int index, int x, int y, int height, int mouseX, int mouseY) {
        Pair<String, String> row;
        try {
            row = screen.rows.get(index);
        } catch (IndexOutOfBoundsException exception) {
            return;
        }
        TextRenderer font = mc.fontRendererObj;
        int color = (index & 1) == 0 ? 0xFFFFFF : 9474192;
        font.drawString(row.getLeft(), x + 2, y + 1, color, 1.0F);
        font.drawString(row.getRight(), (int) (x + 215 - font.getStringWidth(row.getRight(), 1.0F)), y + 1, color, 1.0F);
    }

    @Override protected int getSize() { return screen.rows.size(); }

    @Override protected boolean isSelected(int index) { return false; }

    public void populate() {
        for (GuiButton button : screen.buttons) {
            if (button == screen.generalButton) button.enabled = false;
            else button.enabled = !screen.isUnavailable(button);
        }
        HypixelPlayer player = screen.player;
        screen.rows.add(new ImmutablePair<>("Rank", player.getRankPrefix()));
        if (player.networkLevel == null && player.networkExp != null)
            player.networkLevel = (Math.sqrt(player.networkExp + 15312.5) - 88.38834764831844) / 35.35533905932738;
        if (player.networkLevel != null)
            screen.rows.add(new ImmutablePair<>("Network Level", Double.toString(RandomUtil.round(player.networkLevel, 2))));
        var language = player.getUserLanguage();
        if (language != null) screen.rows.add(new ImmutablePair<>("Language", language.displayName));
        var channel = player.getChannel();
        if (channel != null) screen.rows.add(new ImmutablePair<>("Chat Channel", channel.displayName));
        screen.rows.add(new ImmutablePair<>("Karma", String.format("%,d", player.karma)));
        screen.rows.add(new ImmutablePair<>("Achievement Points", String.format("%,d", player.achievementPoints)));
        if (player.achievements != null && player.achievements.generalWins != null)
            screen.rows.add(new ImmutablePair<>("Total Wins", String.format("%,d", player.achievements.generalWins)));
        screen.rows.add(new ImmutablePair<>("First Joined", TimeUtil.timeAgo(player.firstLogin, " ago")));
        if (player.lastLogin != null) {
            if (player.lastLogout != null && player.lastLogin <= player.lastLogout) {
                screen.rows.add(new ImmutablePair<>("Last Online", TimeUtil.timeAgo(player.lastLogout, " ago")));
                screen.rows.add(new ImmutablePair<>("Online Time", TimeUtil.formatDuration(player.lastLogout - player.lastLogin, false)));
                return;
            }
            screen.rows.add(new ImmutablePair<>("Last Online", "Now"));
            screen.rows.add(new ImmutablePair<>("Online Time", TimeUtil.formatDuration(System.currentTimeMillis() - player.lastLogin, false)));
        } else {
            screen.rows.add(new ImmutablePair<>("Last Online", "API disabled"));
        }
        if (statusPending) return;
        statusPending = true;
        int[] insertionIndex = {screen.rows.size()};
        CompletableFuture.supplyAsync(com.adjustclient.Adjust.hypixelApiManager.statusTask(screen.uuid))
                .thenAccept(response -> {
                    statusPending = false;
                    if (response == null || screen.selected != this || response.session == null) return;
                    var session = response.session;
                    if (session.gameType != null) {
                        String game = StringUtils.isAllUpperCase(session.gameType) ? titleCase(session.gameType) : session.gameType;
                        screen.rows.add(insertionIndex[0]++, new ImmutablePair<>("Game", game));
                    }
                    if (session.mode != null)
                        screen.rows.add(insertionIndex[0]++, new ImmutablePair<>("Mode", titleCase(session.mode.replaceAll("_", " "))));
                    if (session.map != null)
                        screen.rows.add(insertionIndex[0], new ImmutablePair<>("Map", session.map));
                });
    }

    public String getName() { return "General"; }

    @Override protected int getContentHeight() { return getSize() * 10; }
}
