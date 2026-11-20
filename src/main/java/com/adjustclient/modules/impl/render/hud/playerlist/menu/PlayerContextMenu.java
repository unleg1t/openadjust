package com.adjustclient.modules.impl.render.hud.playerlist.menu;

import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.modules.impl.render.hud.playerlist.HoverCardState;
import com.adjustclient.modules.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.utilities.anticheat.entity.TrackedEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.regex.Pattern;
import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.HypixelGameState;
import com.adjustclient.modules.impl.exploit.playerdetect.PlayerDetectModule;
import com.adjustclient.modules.impl.exploit.playerdetect.hackerdetect.data.TrackedPlayer;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.core.gui.stats.HypixelStatsScreen;
import com.adjustclient.utilities.BrowserUtil;
import com.adjustclient.utilities.anticheat.ServerTracker;

public final class PlayerContextMenu extends ContextMenu {
    public static net.minecraft.entity.player.EntityPlayer spectateTarget;

    /** The lambda in ViolationsMenu ("Back") creates the menu with {@code new PlayerContextMenu(owner)} and calls {@link #build()}. */
    public PlayerContextMenu(HoverCardState owner) {
        this.owner = owner;
        this.groups = new ArrayList<>();
    }

    /** SkyCrypt profile page (https://sky.shiiyu.moe/stats/<name>). The name is looked up in the nick cache first. */
    public static void openSkyCrypt(PlayerEntry entry) {
        String name = entry.player.info.getGameProfile().getName();
        com.adjustclient.modules.impl.exploit.playerdetect.hypixel.nick.NickDetector nicks = PlayerDetectModule.INSTANCE.nickDetector;
        if (nicks != null && nicks.nickedPlayers.containsKey(name)) {
            com.adjustclient.modules.impl.exploit.playerdetect.hypixel.nick.NickedPlayer real = nicks.nickedPlayers.get(name);
            if (real != null) name = real.realName;
        }
        String url = "https://sky.shiiyu.moe/stats/" + name;
        try {
            BrowserUtil.openUrl(url);                                              // open the URL (Desktop.browse)
        } catch (Exception e) {
            new NotificationBuilder("Error").addText("Couldn't open link in browser.", 0xFFBEBCBD).show();   // notification
        }
    }

    /** Sends "/report <name>" through the chat command queue. */
    public void report(HoverCardState owner) {
        PlayerEntry entry = owner.entry;
        String name = entry.player.info.getGameProfile().getName();
        String command = "/report " + name;
        Adjust.chatMessageSender.sendMessage(command, false);
        ContextMenu.close();
    }

    /** PitPanda profile page (https://pitpanda.rocks/players/<name>). */
    public static void openPitPanda(PlayerEntry entry) {
        String name = entry.player.info.getGameProfile().getName();
        com.adjustclient.modules.impl.exploit.playerdetect.hypixel.nick.NickDetector nicks = PlayerDetectModule.INSTANCE.nickDetector;
        if (nicks != null && nicks.nickedPlayers.containsKey(name)) {
            com.adjustclient.modules.impl.exploit.playerdetect.hypixel.nick.NickedPlayer real = nicks.nickedPlayers.get(name);
            if (real != null) name = real.realName;
        }
        String url = "https://pitpanda.rocks/players/" + name;
        try {
            BrowserUtil.openUrl(url);
        } catch (Exception e) {
            new NotificationBuilder("Error").addText("Couldn't open link in browser.", 0xFFBEBCBD).show();
        }
    }

    public void showStatistics(PlayerEntry entry) {
        ContextMenu.close();
        com.mojang.authlib.GameProfile profile = entry.player.info.getGameProfile();
        String name = profile.getName();
        String uuid = profile.getId().toString();
        com.adjustclient.modules.impl.exploit.playerdetect.hypixel.nick.NickDetector nicks = PlayerDetectModule.INSTANCE.nickDetector;
        if (nicks != null && nicks.nickedPlayers.containsKey(name)) {
            com.adjustclient.modules.impl.exploit.playerdetect.hypixel.nick.NickedPlayer real = nicks.nickedPlayers.get(name);
            if (real == null) {
                new NotificationBuilder("Player List").addText(
                        "This feature is unavailable since that player is nicked.", 0xFFBEBCBD).show();
                return;
            }
            uuid = real.realUuid.toString();
        }
        uuid = uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
        Minecraft.getMinecraft().displayGuiScreen(new HypixelStatsScreen(null, uuid));
    }

    public void build() {
        HoverCardState owner = this.owner;
        this.groups.clear();

        this.groups.add(new MenuGroup("Tags",
                (entry, tracked) -> tracked != null && ((TrackedEntity) tracked).getEntity().L(),
                new MenuItem("Whitelist", (entry, tracked) -> {
                    toggleMark(entry, com.adjustclient.core.tag.PlayerTag.WHITELIST);
                    ContextMenu.close();
                }),
                new MenuItem("Target", (entry, tracked) -> {
                    toggleMark(entry, com.adjustclient.core.tag.PlayerTag.TARGET);
                    ContextMenu.close();
                })));

        this.groups.add(new MenuGroup("Analysis",
                (entry, tracked) -> tracked == null || ((TrackedEntity) tracked).getEntity().L(),
                new MenuItem("Spectate", (entry, tracked) -> {
                    spectateTarget = (EntityPlayer) ((TrackedEntity) tracked).getEntity();
                    ContextMenu.close();
                }),
                new MenuItem("Violations", (entry, tracked) -> {
                    TrackedEntity trackedPlayer = owner.trackedPlayer;
                    TrackedPlayer record = trackedPlayer == null ? null
                            : com.adjustclient.modules.impl.exploit.playerdetect.hackerdetect.HackerDetector.PLAYERS
                                    .get(trackedPlayer.entityId);
                    owner.menu = new ViolationsMenu(owner, record);
                })));

        com.adjustclient.utilities.anticheat.blocker.BlockerState serverInfo = ServerTracker.INSTANCE.serverState;
        if (serverInfo == null || serverInfo.antiCheatModel == null || serverInfo.antiCheatModel.serverType != ServerType.HYPIXEL) {
            return;
        }
        this.groups.add(new MenuGroup("Hypixel",
                new MenuItem("Statistics", (entry, tracked) -> this.showStatistics(entry)),
                new MenuItem("Report", (entry, tracked) -> this.report(owner),
                        (entry, tracked) -> tracked != null && ((TrackedEntity) tracked).getEntity().L())));

        HypixelGameState state = Adjust.gameStateTracker.state;
        Boolean flag = state.lobby;
        if (flag == null || flag) {
            return;
        }
        switch (MenuGameSwitchMap.GAME[state.game.ordinal()]) {
            case 1:
                this.groups.add(new MenuGroup("The Pit",
                        new MenuItem("PitPanda", (entry, tracked) -> openPitPanda(entry))));
                break;
            case 2:
                this.groups.add(new MenuGroup("SkyBlock",
                        new MenuItem("SkyCrypt", (entry, tracked) -> openSkyCrypt(entry))));
                break;
            default:
                break;
        }
    }

    private static void toggleMark(PlayerEntry entry, com.adjustclient.core.tag.PlayerTag markType) {
        com.adjustclient.core.settings.property.TagManager props = Adjust.INSTANCE.tagManager;
        String name = entry.player.info.getGameProfile().getName();
        if (props.getTag(name) == markType) {
            props.remove(name);                       // remove
        } else {
            props.setLoadedTag(name, markType, System.currentTimeMillis());
        }
    }
}
