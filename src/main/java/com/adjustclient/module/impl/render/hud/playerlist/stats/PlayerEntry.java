package com.adjustclient.module.impl.render.hud.playerlist.stats;

import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.module.impl.render.hud.playerlist.RowTint;
import com.adjustclient.module.impl.render.hud.playerlist.StatColorScale;
import com.adjustclient.module.impl.render.hud.playerlist.StatValue;
import com.adjustclient.module.impl.render.hud.playerlist.StatsSwitchMap;
import com.adjustclient.module.impl.render.hud.playerlist.TargetReason;
import com.adjustclient.module.impl.render.hud.playerlist.TeamColors;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.util.Timer;
import com.adjustclient.render.animation.Animation;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;

import java.awt.Color;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.adjustclient.ui.hud.notification.Notifications;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.adjustclient.Adjust;
import com.adjustclient.game.rewound.RewoundGame;
import com.adjustclient.hypixel.HypixelGameState;
import com.adjustclient.module.impl.player.autorewound.AutoRewoundModule;
import com.adjustclient.hypixel.TabListTracker;
import com.adjustclient.hypixel.api.client.AntiSniperApiClient;
import com.adjustclient.hypixel.api.client.CubelifyApiClient;
import com.adjustclient.hypixel.api.client.UrchinApiClient;
import com.adjustclient.hypixel.api.model.ApiResponse;
import com.adjustclient.hypixel.api.model.ApiResult;
import com.adjustclient.hypixel.api.model.antisniper.AntiSniperWinstreakResponse;
import com.adjustclient.hypixel.api.model.cubelify.CubelifyPingResponse;
import com.adjustclient.hypixel.api.model.cubelify.CubelifyResponse;
import com.adjustclient.hypixel.api.model.cubelify.CubelifyTag;
import com.adjustclient.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.hypixel.api.model.player.HypixelPlayerStats;
import com.adjustclient.hypixel.api.model.player.stats.GameStats;
import com.adjustclient.hypixel.api.model.player.stats.SkyWarsStats;
import com.adjustclient.hypixel.api.model.player.stats.WoolGamesStats;
import com.adjustclient.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.hypixel.game.murdermystery.MurderMysteryGameState;
import com.adjustclient.hypixel.game.pit.PitEnchant;
import com.adjustclient.hypixel.game.pit.PitEnchantEntry;
import com.adjustclient.lunar.LunarClientService;
import com.adjustclient.lunar.websocket.LunarTabLogoCache;
import com.adjustclient.module.impl.exploit.playerdetect.PlayerDetectModule;
import com.adjustclient.render.ColorUtil;
import com.adjustclient.server.ServerType;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.service.servertracker.FriendServerTracker;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.ui.hud.notification.Notification;
import com.adjustclient.user.ClientUser;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.TimeUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.entity.EntityTracker;
import com.adjustclient.util.anticheat.player.PlayerKillStats;
import com.adjustclient.util.anticheat.player.PlayerStatsCache;
import com.adjustclient.util.anticheat.player.TabPlayer;
import com.adjustclient.util.anticheat.player.suspicion.SuspicionScorer;
import net.minecraft.util.ChatColor;

public final class PlayerEntry implements Comparable<PlayerEntry> {

    /** Pit bounty in the scoreboard team suffix: "§6§l100g" style (section sign, optional digits and section sign, 'l', digits, 'g'). */
    public static final Pattern BOUNTY_PATTERN = Pattern.compile("§(?:\\d+§)?l(\\d+)g");

    public final TabPlayer player;
    public String name;
    public String suffix = "";
    public String distanceText;
    public String healthText;
    public HypixelPlayer hypixelStats;
    public final Timer requestTimer;
    public String group;
    public String bounty;
    public String gold;
    public String kd;
    public String onlineTime;
    public RowTint status = RowTint.NONE;
    public Double progress;
    public String seen;
    public Integer level;
    public Vec3 position;
    public CubelifyTag[] tags = new CubelifyTag[0];
    public com.adjustclient.lunar.websocket.LunarTabLogo customColor;
    public UUID uuid;
    public UUID lastTagUuid;
    public StatValue<?> tkd;
    public StatValue<?> wl;
    public StatValue<?> ws;
    public ColoredIntStat ping;
    public String enchants;
    public final Animation animation;
    public int nameColor;
    public int teamColor = 0xDFB9B9B9;
    public int statusColor;
    public int levelColor;
    public boolean invisible;
    public boolean apiRequested;
    public boolean npc;
    public boolean noEntity;
    public boolean nameResolved;
    public boolean rewoundRealPlayer;

    public PlayerEntry(TabPlayer player) {
        this.player = player;
        this.requestTimer = new Timer(0L);
        this.animation = new Animation(com.adjustclient.render.animation.Easing.LINEAR, 111L);
        this.tags = new CubelifyTag[0];
    }

    private GameProfile profile() {
        return this.player.info.getGameProfile();
    }

    public int getLevelColor() {
        BooleanSetting levelColorHighlight = HUDModule.INSTANCE_ALT.playerList.levelColorHighlight;      // HUD > Player List > Other > Level Color Highlight
        if (!levelColorHighlight.isEnabled()) {
            return 0xFFEFEFEF;
        }
        return this.levelColor;
    }

    // ------------------------------------------------------------------------------------------------------------------------------
    // Ping

    public void updatePing(CubelifyPingResponse result) {
        int pingValue;
        if (result != null && result.data != null) {
            pingValue = result.data.today.avg;
        } else {
            TabListTracker.Entry cached;
            synchronized (Adjust.tabListTracker.entries) {
                Object value = Adjust.tabListTracker.entries.get(this.uuid); // HashMap keyed by UUID
                if (!(value instanceof TabListTracker.Entry)) {
                    this.ping = new ColoredIntStat("?", new Color(206, 186, 122, 240));
                    return;
                }
                cached = (TabListTracker.Entry) value;
            }
            pingValue = cached.average;
        }
        this.ping = new ColoredIntStat(Integer.valueOf(pingValue), Integer.toString(pingValue));
    }

    /**
     * Winstreak from the API result: shows the number (or the number plus a marker when the streak is not exact) coloured by the thresholds
     * 5 / 10 / 20 / 35 / 50 / 75 / 100, and marks the player as a High WS auto-target.
     */
    public void updateWinstreak(Integer threshold, AntiSniperWinstreakResponse result) {
        if (result == null || result.overallWinstreak == null) {
            return;
        }
        Integer streak = result.overallWinstreak;
        Boolean exact = result.overallAccurate;
        if (exact != null && exact) {
            this.ws = new StatValue<>(streak, Integer.toString(streak), StatColorScale.WIN_STREAK);
        } else {
            String text = streak + "*";               // 1-character marker constant
            ChatColor chatColor;
            int s = streak;
            if (s < 5) {
                chatColor = ChatColor.GRAY;
            } else if (s < 10) {
                chatColor = ChatColor.WHITE;
            } else if (s < 20) {
                chatColor = ChatColor.GOLD;
            } else if (s < 35) {
                chatColor = ChatColor.DARK_GREEN;
            } else if (s < 50) {
                chatColor = ChatColor.RED;
            } else if (s < 75) {
                chatColor = ChatColor.DARK_RED;
            } else if (s < 100) {
                chatColor = ChatColor.LIGHT_PURPLE;
            } else {
                chatColor = ChatColor.DARK_PURPLE;
            }
            int color = Minecraft.getMinecraft().fontRendererObj.getColorCode(chatColor.getFormattingCode()) | 0xF0000000;
            this.ws = new StatValue<>(text, color);
        }
        if (threshold == null) {
            return;
        }
        if (streak >= threshold) {
            this.autoTarget(TargetReason.HIGH_WS);
        }
    }

    @Override
    public int compareTo(PlayerEntry other) {
        NetworkPlayerInfo a = this.player.info;
        NetworkPlayerInfo b = other.player.info;
        boolean bedWars = Adjust.gameStateTracker.state.game == HypixelGame.BED_WARS
                || (isOnRewound() && Adjust.rewoundGameTracker.state.game == RewoundGame.BEDWARS);
        if (!bedWars) {
            // vanilla tab list order: GuiPlayerTabOverlay.PlayerComparator
            return new net.minecraft.client.gui.GuiPlayerTabOverlay.PlayerComparator().compare(a, b);
        }
        int rankA = TeamColors.getTeamIndex(a);
        int rankB = TeamColors.getTeamIndex(b);
        if (rankB == rankA) {
            return new net.minecraft.client.gui.GuiPlayerTabOverlay.PlayerComparator().compare(a, b);
        }
        return rankB > rankA ? -1 : 1;
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    public void updateSeen() {
        Map<UUID, Integer> map = HUDModule.INSTANCE_ALT.playerList.counts;                 // HashMap<UUID, Integer>
        UUID id = profile().getId();
        Integer count = map == null ? null : map.get(id);
        this.seen = count == null ? null : Integer.toString(count);
    }

    /**
     * Requests the Urchin tags of the real UUID (once per UUID change, only if the player list's Hypixel API / Urchin option is on and the
     * request budget allows it). The result handler stores the tags clone in {@link #tags} on the main thread and reports them in chat.
     */
    public void fetchTags() {
        if (UserConfiguration.SERAPH_API_KEY.keyString == null) {
            return;
        }
        if (((com.adjustclient.hypixel.api.HypixelApiManager) Adjust.hypixelApiManager).seraphClient.requests.size() >= ((com.adjustclient.hypixel.api.HypixelApiManager) Adjust.hypixelApiManager).seraphClient.requestLimit) {   // request budget used up
            return;
        }
        UUID id = this.uuid != null ? this.uuid : profile().getId();
        if (id == null || (this.lastTagUuid != null && this.lastTagUuid.equals(id))) {
            return;
        }
        this.lastTagUuid = id;
        this.tags = new CubelifyTag[0];
        CompletableFuture<CubelifyResponse> future = ((com.adjustclient.hypixel.api.HypixelApiManager) Adjust.hypixelApiManager).urchinClient.getTags(id);
        future.thenAccept(result -> {
            this.tags = result.tags.clone();                                  // Urchin tag array
            // runs on the Minecraft thread (addScheduledTask when called from another thread)
            Minecraft mc = Minecraft.getMinecraft();
            Runnable report = () -> this.reportTags(id, result);
            if (mc.isCallingFromMinecraftThread()) {
                this.reportTags(id, result);
            } else {
                mc.addScheduledTask(report);
            }
        });
    }

    /**
     * The player's display name as a chat component: in Pit the Hypixel API nick team colour prefix + profile name; otherwise the
     * scoreboard team colour + name, or the profile name.
     */
    public IChatComponent getChatName() {
        HypixelGameState game = Adjust.gameStateTracker.state;
        if (game instanceof BedWarsGameState && game.startTimer != null) {
            GameProfile profile = profile();
            Map<String, BedWarsTeam> byName = ((BedWarsGameState) game).playerTeams;
            if (byName != null) {
                BedWarsTeam team = byName.get(profile.getName().toLowerCase());
                if (team != null) {
                    return new ChatComponentText(String.valueOf(team.chatColor) + profile.getName());
                }
            }
            Scoreboard scoreboard = Minecraft.getMinecraft().theWorld == null
                    ? null : Minecraft.getMinecraft().theWorld.getScoreboard();
            if (scoreboard != null && scoreboard.getPlayersTeam(profile.getName()) != null) {
                BedWarsTeam colored = BedWarsTeam.byTeamPrefix(this.player.info.getPlayerTeam());
                if (colored != null) {
                    return new ChatComponentText(String.valueOf(colored.chatColor) + profile.getName());
                }
            }
        }
        if (this.player.relationData != null) {
            return (IChatComponent) this.player.relationData;
        }
        NetworkPlayerInfo info = this.player.info;
        if (info.getDisplayName() == null) {
            ScorePlayerTeam team = info.getPlayerTeam();
            if (team == null) {
                return new ChatComponentText(info.getGameProfile().getName());
            }
            return new ChatComponentText(team.getColorPrefix() + info.getGameProfile().getName() + team.getColorSuffix());
        }
        return info.getDisplayName();
    }

    public void updateProgress() {
        double value = SuspicionScorer.score(this);
        this.progress = value;
        if (this.status == RowTint.NONE && value >= 1.0) {
            this.status = RowTint.SUSPICIOUS;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    public void update(boolean fullUpdate) {
        GameProfile profile = profile();
        String ign = profile.getName();
        if (this.name == null) {
            this.name = ign;
        }
        this.suffix = "";

        ClientUser user = ((FriendServerTracker) Adjust.INSTANCE.screenStack.serverTracker).findUser(ign);
        if (this.player.isSelf()) {                        // the local player
            BooleanSetting selfUsername = HUDModule.INSTANCE_ALT.playerList.selfUsername;   // HUD > Player List > Other > Self Username
            if (selfUsername.isEnabled()) {
                user = (ClientUser) ((com.adjustclient.property.UserManager) Adjust.INSTANCE.field40).field16;                           // own Adjust account
            }
        }
        if (user != null) {
            this.suffix = " " + ChatColor.BOLD + user.name;
        }
        if (this.progress != null && this.progress >= 1.0) {
            this.suffix = this.suffix + ChatColor.GRAY + " " + RandomUtil.round(100.0 * this.progress, 2) + "%";
        }
        this.nameColor = user == null ? HUDModule.getHudColor().getRGB() : user.getColor();
        this.bounty = null;
        this.rewoundRealPlayer = false;
        boolean onRewound = isOnRewound();
        if (onRewound && this.player.info.getDisplayName() != null
                && this.player.info.getDisplayName().getFormattedText().startsWith("§r§c* ")) {
            this.rewoundRealPlayer = true;
        }

        // scoreboard team of the player
        Minecraft mc = Minecraft.getMinecraft();
        Scoreboard scoreboard = mc.theWorld == null ? null : mc.theWorld.getScoreboard();
        ScorePlayerTeam team = scoreboard == null ? null : scoreboard.getPlayersTeam(ign);
        if (team != null) {
            if (fullUpdate) {
                this.updateHypixelStats(ign);
                this.updateKd(ign);
                this.parseTeamSuffix(team);
            } else if (onRewound) {
                this.updateKd(ign);
                this.parseRewoundTeamSuffix(team);
            } else if (ServerTracker.INSTANCE.serverState != null && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                    && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL) {    // connected to Hypixel
                this.updateKd(ign);
            }
        } else if (!fullUpdate) {
            if (onRewound || (ServerTracker.INSTANCE.serverState != null && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                    && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL)) {
                this.updateKd(ign);
            }
        } else {
            HypixelGameState state = (HypixelGameState) Adjust.gameStateTracker.state;
            if ((state.game.flags & 0x200) != 0 || this.player.realPlayer) {
                this.updateHypixelStats(ign);
                this.updateKd(ign);
            }
        }
        this.updateEntity(ign, team);
        this.updateSeen();
        if (fullUpdate) {
            HypixelGameState state = Adjust.gameStateTracker.state;
            if (state instanceof BedWarsGameState) {
                BedWarsGameState pit = (BedWarsGameState) state;
                if (pit.startTimer != null || pit.mode != null) {
                    this.fetchTags();
                }
            }
        }

        // distance
        if (this.npc) {
            this.healthText = "";
            this.distanceText = "";
        } else if (this.position != null) {
            int distance = 0;
            if (!this.invisible && mc.thePlayer != null) {
                double dx = mc.thePlayer.posX - this.position.xCoord;
                double dy = mc.thePlayer.posY - this.position.yCoord;
                double dz = mc.thePlayer.posZ - this.position.zCoord;
                distance = (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
            }
            this.distanceText = distance + "m";
        }

        this.statusColor = (Integer) this.status.tint.test(this, this);

        // Bed Wars: team colour and bold name
        HypixelGameState gameState = Adjust.gameStateTracker.state;
        if (gameState instanceof MurderMysteryGameState) {
            com.adjustclient.hypixel.game.murdermystery.MurderMysteryPlayer info = ((MurderMysteryGameState) gameState).getPlayer(ign);
            if (info != null) {
                this.teamColor = info.role.field16.getRGB();
                this.name = ChatColor.BOLD + this.name;                          // bold
            }
        }

        // ping
        if (!fullUpdate) {
            int p = this.player.info.getResponseTime();
            this.ping = new ColoredIntStat(Integer.valueOf(p), Integer.toString(p));
        } else {
            Boolean flag = (Boolean) Adjust.gameStateTracker.state.lobby;
            if (flag != null && flag) {
                NetworkPlayerInfo info = this.player.info;
                int p = info.getResponseTime();
                if (p >= 1) {
                    this.ping = new ColoredIntStat(Integer.valueOf(p), Integer.toString(p));
                } else {
                    this.ping = new ColoredIntStat("?", new Color(206, 186, 122, 240));
                }
            } else if (flag != null) {
                String token = UserConfiguration.INSTANCE.cubelifyToken;
                if (token == null || !Pattern.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.[0-9a-f]{128}", token)) {
                    Integer cached = Adjust.tabListTracker.getAveragePing(this.uuid);
                    if (cached == null) {
                        this.ping = new ColoredIntStat("?", new Color(206, 186, 122, 240));
                    } else {
                        this.ping = new ColoredIntStat(Integer.valueOf(cached.intValue()), Integer.toString(cached.intValue()));
                    }
                }
            }
        }
        this.customColor = LunarClientService.INSTANCE.tabLogoCache.getTabLogo(profile().getId());
    }

    public void autoTarget(TargetReason type) {
        GameProfile profile = this.player.info.getGameProfile();
        GameProfile self = Minecraft.getMinecraft().thePlayer.getGameProfile();
        if (profile.equals(self)) {
            return;
        }
        BooleanSetting setting = (BooleanSetting) HUDModule.INSTANCE_ALT.playerList.autoTargetSettings.get(type);
        if (setting != null && setting.isEnabled()) {
            ServiceUtil.markDetectedPlayer(profile.getName());
        }
    }

    /** Enchant lines of the leggings and the held item of the entity. */
    public List<PitEnchantEntry> getEnchantEntries(com.adjustclient.util.anticheat.entity.TrackedEntity holder) {
        EntityPlayer entity = (EntityPlayer) holder.getEntity();
        List<PitEnchantEntry> result = this.getEnchantList(entity.inventory.armorInventory[1]);
        result.addAll(this.getEnchantList(entity.inventory.getCurrentItem()));
        return result;
    }

    public void updateKd(String name) {
        HypixelGame game = Adjust.gameStateTracker.state.game;
        PlayerStatsCache cache = PlayerStatsCache.INSTANCE;
        if (cache == null) {
            cache = PlayerStatsCache.INSTANCE = new PlayerStatsCache();
        }
        PlayerKillStats stats = (PlayerKillStats) cache.stats.get(name);
        if (stats == null) {
            return;
        }
        int kills = stats.kills;
        boolean killsOnly = isOnRewound()
                ? (Adjust.rewoundGameTracker.state.game == RewoundGame.SKYWARS
                        || Adjust.rewoundGameTracker.state.game == RewoundGame.SURVIVAL_GAMES)
                : (game == HypixelGame.SKYWARS || game == HypixelGame.BLITZ_SG);
        if (killsOnly) {
            this.kd = Integer.toString(kills);
        } else if (kills >= 1) {
            int deaths = stats.deaths;
            if (deaths != 0) {
                this.kd = Float.toString(RandomUtil.round((float) kills / (float) deaths, 2));
            } else {
                this.kd = kills + ".0";
            }
        }
    }

    /**
     * Pit enchant lines of an item stack: "ExtraAttributes" / "CustomEnchants" NBT list, each element {Key, Level}.
     */
    public List<PitEnchantEntry> getEnchantList(ItemStack stack) {
        List<PitEnchantEntry> result = new ArrayList<>();
        if (stack == null) {
            return result;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return result;
        }
        if (!tag.hasKey("ExtraAttributes")) {
            return result;
        }
        NBTTagCompound extra = tag.getCompoundTag("ExtraAttributes");
        if (!extra.hasKey("CustomEnchants") || !extra.hasKey("Nonce")) {
            return result;
        }
        boolean sword = stack.getItem() != null && stack.getItem().getClass() == ItemSword.class;
        NBTTagList enchants = extra.getTagList("CustomEnchants", 10);
        for (int i = 0; i < enchants.tagCount(); i++) {
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);
            if (!enchant.hasKey("Key")) {
                continue;
            }
            try {
                PitEnchant pitEnchant = PitEnchant.valueOf(enchant.getString("Key").toUpperCase());
                int level = enchant.getInteger("Level");
                ChatColor color;
                if (sword) {
                    color = ChatColor.BLUE;
                } else if (extra.getInteger("Nonce") == 6) {
                    color = ChatColor.DARK_PURPLE;
                } else {
                    color = ChatColor.RED;
                }
                result.add(new PitEnchantEntry(pitEnchant, level, color));
            } catch (Exception ignored) {
                // Invalid custom enchant entries do not prevent reading subsequent entries.
            }
        }
        return result;
    }

    /**
     * Parses the scoreboard team suffix of the player.
     * Housing: leading spaces are removed and a "[Group]" suffix is stored without brackets (normalised) in {@link #group}.
     * Pit: the bounty ("§6§l100g" via {@link #BOUNTY_PATTERN}) is stored with thousands separators in {@link #bounty}.
     */
    public void parseTeamSuffix(ScorePlayerTeam team) {
        this.npc = this.isNpc(team);
        HypixelGame game = (HypixelGame) Adjust.gameStateTracker.state.game;
        if (game == HypixelGame.HOUSING) {
            String suffix = team.getColorSuffix();
            String stripped = StringUtils.stripControlCodes(suffix);
            while (stripped != null && stripped.startsWith(" ")) {
                stripped = stripped.replaceFirst(" ", "");
                suffix = suffix.replaceFirst(" ", "");
            }
            if (stripped.length() >= 3 && stripped.charAt(0) == '[' && stripped.charAt(stripped.length() - 1) == ']') {
                this.group = Normalizer.normalize(suffix.replaceFirst("\\[", "").replaceFirst("]$", ""), Normalizer.Form.NFKC);
            } else {
                this.group = suffix;
            }
        } else if (game == HypixelGame.THE_HYPIXEL_PIT) {
            Matcher matcher = BOUNTY_PATTERN.matcher(team.getColorSuffix());
            if (matcher.find()) {
                this.bounty = String.format("%,d", Integer.parseInt(matcher.group(1)));
            }
        }
    }

    public void parseRewoundTeamSuffix(ScorePlayerTeam team) {
        if (Adjust.rewoundGameTracker.state.game == RewoundGame.PIT && team.getColorSuffix() != null) {
            Matcher matcher = BOUNTY_PATTERN.matcher(team.getColorSuffix());
            if (matcher.find()) {
                try {
                    this.bounty = String.format("%,d", Integer.parseInt(matcher.group(1)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    private static boolean isOnRewound() {
        return ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.REWOUND;
    }

    public void updateTeamColor(boolean checkGame, ScorePlayerTeam team) {
        int color = 0xDFB9B9B9;
        if (ServerTracker.INSTANCE.serverState == null || ServerTracker.INSTANCE.serverState.antiCheatModel == null) {
            return;
        }
        ServerType serverType = ServerTracker.INSTANCE.serverState.antiCheatModel.serverType;
        if (serverType == ServerType.REWOUND) {
            if (team == null) {
                this.teamColor = color;
                return;
            }
            String prefix = team.getColorPrefix();
            if (prefix != null) {
                if (prefix.startsWith("§c* ")) {
                    prefix = prefix.substring(4);
                }
                if (Adjust.rewoundGameTracker.state.game == RewoundGame.PIT && prefix.length() >= 2) {
                    prefix = prefix.substring(0, 2);
                }
                String format = net.minecraft.client.gui.FontRenderer.getFormatFromString(prefix);
                if (format.length() >= 2) {
                    color = Minecraft.getMinecraft().fontRendererObj.getColorCode(format.charAt(1)) & 0xFFFFFF | 0xFF000000;
                }
            }
            this.teamColor = color;
            return;
        }
        if (serverType != ServerType.HYPIXEL) {
            return;
        }
        if (team != null && " §5[V]".equals(team.getColorSuffix())) {
            this.status = RowTint.VANISHED;
        }
        if (checkGame) {
            HypixelGame game = (HypixelGame) Adjust.gameStateTracker.state.game;
            if (!game.isAnyOf(new HypixelGame[] { HypixelGame.MEGA_WALLS, HypixelGame.BED_WARS, HypixelGame.SHEEP_WARS })) {
                this.teamColor = color;
                return;
            }
        }
        if (team != null) {
            HypixelGame game = (HypixelGame) Adjust.gameStateTracker.state.game;
            String prefix = team.getColorPrefix();
            if (game == HypixelGame.THE_HYPIXEL_PIT && prefix.length() >= 2) {
                prefix = prefix.substring(0, 2);
            }
            String format = net.minecraft.client.gui.FontRenderer.getFormatFromString(prefix);
            if (format.length() >= 2) {
                color = Minecraft.getMinecraft().fontRendererObj.getColorCode(format.charAt(1)) & 0xFFFFFF | 0xFF000000;
            }
        }
        this.teamColor = color;
    }

    /**
     * Refreshes everything derived from the entity: Enchants text (leggings + held item, comma separated), HP % (data watcher float 6 over
     * max health), position, the team colour and the "client" flags.
     */
    public void updateEntity(String name, ScorePlayerTeam team) {
        HypixelGameState gameState = Adjust.gameStateTracker.state;
        boolean checkGame = gameState.startTimer != null && (gameState.game.flags & 4) == 0;
        this.updateStatus(name);
        if (this.progress == null) {
            this.updateProgress();
        }
        com.adjustclient.util.anticheat.entity.TrackedEntity holder = ServerTracker.INSTANCE.entityTracker.getByName(name);   // entity wrapper by name
        this.noEntity = holder == null;
        if (holder == null) {
            this.updateTeamColor(checkGame, team);
            return;
        }
        List<PitEnchantEntry> ench = this.getEnchantEntries(holder);
        if (ench.size() == 0) {
            this.enchants = null;
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ench.size(); i++) {
                PitEnchantEntry e = ench.get(i);
                sb.append(e.color + e.enchant.displayName + " " + e.level);
                if (i < ench.size() - 1) {
                    sb.append(ChatColor.RESET);                                 // ChatColor RESET
                    sb.append(", ");
                }
            }
            this.enchants = sb.toString();
        }
        EntityPlayer entity = (EntityPlayer) holder.getEntity();
        float health = entity.getDataWatcher().getWatchableObjectFloat(6);
        float maxHealth = entity.getMaxHealth();
        this.healthText = (int) (health / maxHealth * 100.0F) + "%";
        this.position = new Vec3(entity.posX, entity.posY, entity.posZ);
        this.updateTeamColor(checkGame, team);
        boolean flag = entity.L();
        if (!flag) {
            if (this.status == RowTint.BLACKLISTED || this.status == RowTint.NONE || this.status == RowTint.SUSPICIOUS) {
                if (com.adjustclient.module.impl.exploit.playerdetect.hackerdetect.HackerDetector.isCheater(holder.entityId)) {
                    this.status = RowTint.HACKING;
                }
            }
        }
        if (flag || (this.player != null && this.player.info != null && this.player.info.getGameProfile() != null)) {
            this.invisible = ((EntityPlayer) holder.getEntity()).isUser();
        } else {
            this.updateTeamColor(checkGame, team);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    /**
     * Colour of a level: grey (166) -> light grey (237) up to 15, light grey -> gold (200,174,108) up to 55, gold -> red (227,88,88) up to
     * 300, red -> pink (213,101,161) up to 500, then pink.
     */
    public static int levelColor(int level) {
        Color gray = new Color(166, 166, 166, 255);
        Color light = new Color(237, 237, 237, 255);
        Color color = ColorUtil.blend(light, gray, Math.min(level / 15.0, 1.0));
        if (level < 16) {
            return color.getRGB();
        }
        Color gold = new Color(200, 174, 108, 255);
        color = ColorUtil.blend(gold, color, Math.min(level / 55.0, 1.0));
        if (level < 56) {
            return color.getRGB();
        }
        Color red = new Color(227, 88, 88, 255);
        color = ColorUtil.blend(red, color, Math.min(level / 300.0, 1.0));
        if (level < 301) {
            return color.getRGB();
        }
        Color pink = new Color(213, 101, 161, 255);
        color = ColorUtil.blend(pink, color, Math.min(level / 500.0, 1.0));
        if (level < 501) {
            return color.getRGB();
        }
        return ColorUtil.blend(pink, color, 1.0F).getRGB();
    }

    public void updateHypixelStats(String name) {
        HypixelGameState state = Adjust.gameStateTracker.state;
        if (this.hypixelStats != null && state != null) {
            if (!this.nameResolved) {
                this.name = "";
                HypixelPlayerStats container = this.hypixelStats.stats;
                HypixelGame game = state.game;
                if (container != null) {
                    if (game == HypixelGame.THE_HYPIXEL_PIT) {
                        this.gold = String.format("%,d", (int) container.pit.profile.cash);
                    } else {
                        GameStats model = container.getCurrentGameStats();   // per-game stats model
                        if (model != null) {
                            Integer lvl = null;
                            int mode = StatsSwitchMap.GAME[game.ordinal()];
                            if (mode == 1) {                                         // SkyWars
                                lvl = container.skyWars.getLevel();
                            } else if (mode == 2 || mode == 3) {                     // Bed Wars, Bed Wars Practice
                                lvl = this.hypixelStats.achievements == null ? null : this.hypixelStats.achievements.bedwarsLevel;
                            } else if (mode == 4 || mode == 5 || mode == 6) {        // Wool Wars, Sheep Wars, Capture The Wool
                                lvl = WoolGamesStats.getLevel(container.woolGames.progression.experience);
                            }
                            if (lvl != null) {
                                this.level = lvl;
                                this.levelColor = levelColor(lvl);
                                int limit = game == HypixelGame.SKYWARS ? 15 : 20;
                                if (limit <= lvl) {
                                    this.autoTarget(TargetReason.HIGH_LEVEL);
                                }
                            }
                            Float kdValue = model.getKillDeathRatio();
                            if (kdValue != null) {
                                Integer threshold = model.getKdThreshold();
                                this.tkd = new StatValue<>(kdValue, Float.toString(RandomUtil.round(kdValue, 2)), StatColorScale.KDR);
                                if (threshold != null && kdValue >= (float) threshold) {
                                    this.autoTarget(TargetReason.HIGH_KDR);
                                }
                            }
                            Float wlValue = model.getWinLossRatio();
                            if (wlValue != null) {
                                Integer threshold = model.getWlThreshold();
                                this.wl = new StatValue<>(wlValue, Float.toString(RandomUtil.round(wlValue, 2)), StatColorScale.WLR);
                                if (threshold != null && wlValue >= (float) threshold) {
                                    this.autoTarget(TargetReason.HIGH_WLR);
                                }
                            }
                            Integer streak = model.getWinstreak();
                            Integer streakThreshold = model.getWinstreakThreshold();
                            boolean missingStreak = false;
                            if (streak == null) {
                                if (model.getWins() != null) {
                                    this.ws = new StatValue<>("API off", new Color(202, 183, 124, 240));
                                    missingStreak = true;
                                }
                            } else if (streak != 0) {
                                this.ws = new StatValue<>(streak, Integer.toString(streak), StatColorScale.WIN_STREAK);
                                if (streakThreshold != null && streak >= streakThreshold) {
                                    this.autoTarget(TargetReason.HIGH_WS);
                                }
                            }
                            if (mode == 2 || mode == 3) {
                                if (Adjust.hypixelApiManager.isAntiSniperAvailable()) {
                                    UUID real = this.resolveUuid(name);
                                    if (real != null) {
                                        if (!this.player.isSelf()) {       // not the local player
                                            ((com.adjustclient.hypixel.api.HypixelApiManager) Adjust.hypixelApiManager).antiSniperClient.getBlacklist(real.toString())
                                                    .thenAccept(blacklisted -> this.onBlacklistResult(state, name, blacklisted));
                                        }
                                        if (missingStreak) {
                                            ((com.adjustclient.hypixel.api.HypixelApiManager) Adjust.hypixelApiManager).antiSniperClient.getWinstreak(real.toString())
                                                    .thenAccept(result -> this.updateWinstreak(streakThreshold, result));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Boolean lobby = (Boolean) Adjust.gameStateTracker.state.lobby;
                if (lobby != null && !lobby) {
                    String token = UserConfiguration.INSTANCE.cubelifyToken;
                    if (token != null && Pattern.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.[0-9a-f]{128}", token)) {
                        Adjust.hypixelApiManager.cubelifyClient.getPing(this.uuid)
                                .thenAccept(result -> this.updatePing(result));
                    }
                }
                if (this.isNicked(name)) {
                    this.name = this.name + ChatColor.ITALIC;
                }
                this.name = this.name + name;
                this.nameResolved = true;
            }
            if (this.hypixelStats.lastLogin == null) {
                this.onlineTime = "API off";
            } else {
                long since = System.currentTimeMillis() - this.hypixelStats.lastLogin.longValue();
                this.onlineTime = TimeUtil.formatDuration(since, false);
            }
            return;
        }
        // no stats yet
        if (HUDModule.INSTANCE_ALT.playerList.apiColumnsActive) {
            if (this.apiRequested) {
                return;
            }
            if (this.requestTimer.lastMs + 60000L > System.currentTimeMillis()) {
                return;
            }
            UUID id = this.resolveUuid(name);
            if (id == null) {
                if (!this.isNicked(name)) {
                    return;
                }
                this.name = ChatColor.ITALIC + name;                              // nicked: italic name
                return;
            }
            this.apiRequested = true;
            this.uuid = id;
            CompletableFuture<HypixelPlayer> request = Adjust.hypixelApiManager.getPlayer(id);
            request.whenComplete((result, error) -> {
                Minecraft mc = Minecraft.getMinecraft();
                Runnable apply = () -> {
                    this.apiRequested = false;
                    if (error != null || result == null) {
                        this.requestTimer.lastMs = System.currentTimeMillis();   // Timer.reset()
                    } else {
                        this.hypixelStats = result;
                        this.nameResolved = false;
                        this.updateProgress();
                    }
                };
                if (mc.isCallingFromMinecraftThread()) {
                    apply.run();
                } else {
                    mc.addScheduledTask(apply);
                }
            });
            return;
        }
        // Hypixel API option off
        if (this.isNicked(name)) {
            this.name = ChatColor.ITALIC + name;
        }
    }

    /**
     * Anti-Sniper blacklist answer: marks a blacklisted player accordingly and, if the HUD
     * notifications are enabled and the game is not in its "in game" state, a "Warning" notification
     * "Player <name> is blacklisted by AntiSniper API." is added (200 ms fade-in, three colour animations).
     */
    private void onBlacklistResult(HypixelGameState state, String name,
            com.adjustclient.hypixel.api.model.antisniper.AntiSniperBlacklistResponse response) {
        if (!response.blacklisted) {
            return;
        }
        if (state.startTimer == null) {
            Notifications notifications = HUDModule.INSTANCE_ALT.notifications;
            if (notifications != null && HUDModule.INSTANCE_ALT.isEnabled()) {
                synchronized (notifications.list) {
                    new com.adjustclient.ui.hud.notification.NotificationBuilder("Warning")
                            .addText("Player ", 0xFFBEBCBD).append(name)
                            .addText(" is blacklisted by AntiSniper API.", 0xFFBEBCBD).show();
                }
            }
        }
        if (this.status != RowTint.HACKING) {
            this.status = RowTint.BLACKLISTED;
        }
    }

    /**
     * True for rows that are NPCs / hidden in the lobby-like modes: needs a non-lobby game state, the entity flag {@link #invisible} makes it
     * true immediately; SkyWars: team prefix "§7" and a team name starting with 'a'; Bed Wars / Murder Mystery: team prefix "§7".
     */
    public boolean isNpc(ScorePlayerTeam team) {
        HypixelGameState state = (HypixelGameState) Adjust.gameStateTracker.state;
        Boolean lobby = state.lobby;
        if (lobby == null || lobby) {
            return false;
        }
        if (!state.isSpectator()) {
            return false;
        }
        if (state.startTimer == null) {
            return false;
        }
        if (this.invisible) {
            return true;
        }
        switch (StatsSwitchMap.GAME[state.game.ordinal()]) {
            case 1:                                                               // SKYWARS
                if (!"§7".equals(team.getColorPrefix())) {
                    return false;
                }
                String teamName = team.getRegisteredName();
                return teamName.length() != 0 && teamName.charAt(0) == 'a';
            case 2:                                                               // BED_WARS
            case 7:                                                               // MURDER_MYSTERY
                return "§7".equals(team.getColorPrefix());
            default:
                return false;
        }
    }

    /** Reports the tags of a player in chat (calls AutoHypixel with the chat name). */
    public void reportTags(UUID id, CubelifyResponse result) {
        com.adjustclient.module.impl.player.autohypixel.feature.chatstats.UrchinStatsProvider.displayOnce(id, this.getChatName(), result);
    }

    public UUID resolveUuid(String name) {
        com.adjustclient.module.impl.exploit.playerdetect.hypixel.nick.NickDetector detector = PlayerDetectModule.INSTANCE.nickDetector;
        if (detector != null && detector.nickedPlayers.containsKey(name)) {
            com.adjustclient.module.impl.exploit.playerdetect.hypixel.nick.NickedPlayer nick = detector.nickedPlayers.get(name);
            return nick == null ? null : nick.realUuid;
        }
        return this.player.info.getGameProfile().getId();
    }

    /** True if the name is in the nick cache. */
    public boolean isNicked(String name) {
        com.adjustclient.module.impl.exploit.playerdetect.hypixel.nick.NickDetector cache = PlayerDetectModule.INSTANCE.nickDetector;
        if (cache == null) {
            return false;
        }
        return cache.nickedPlayers.containsKey(name);
    }

    public void updateStatus(String name) {
        if (this.status == RowTint.BLACKLISTED) {
            return;
        }
        com.adjustclient.tag.PlayerTag property = Adjust.INSTANCE.tagManager.getTag(name);
        if (property == null) {
            this.status = this.rewoundRealPlayer
                    && (AutoRewoundModule.INSTANCE == null || !AutoRewoundModule.INSTANCE.shouldHideBots())
                    ? RowTint.REAL_PLAYER
                    : RowTint.NONE;
            this.progress = null;
            return;
        }
        switch (StatsSwitchMap.PAIR[property.ordinal()]) {
            case 1:
                this.status = RowTint.WHITELISTED;
                break;
            case 2:
                this.status = RowTint.TARGETED;
                break;
            default:
                throw new IllegalStateException();
        }
    }
}
