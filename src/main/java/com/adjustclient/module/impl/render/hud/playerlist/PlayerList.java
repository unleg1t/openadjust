package com.adjustclient.module.impl.render.hud.playerlist;

import com.adjustclient.module.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.module.impl.render.hud.playerlist.column.ColumnToggles;
import com.adjustclient.module.impl.render.hud.playerlist.column.PlayerListColumn;
import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.game.rewound.RewoundGameState;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.module.impl.player.autorewound.AutoRewoundModule;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.NvgFontRenderer;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.KeybindSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.util.Timer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatColor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import com.adjustclient.game.movable.Movable;
import com.adjustclient.hypixel.HypixelGameState;
import com.adjustclient.hypixel.api.HypixelApiManager;
import com.adjustclient.hypixel.api.model.ApiResult;
import com.adjustclient.hypixel.api.model.cubelify.CubelifyTag;
import com.adjustclient.module.impl.exploit.playerdetect.PlayerDetectModule;
import com.adjustclient.module.impl.exploit.playerdetect.hypixel.nick.NickedPlayer;
import com.adjustclient.module.impl.render.hud.playerlist.menu.PlayerContextMenu;
import com.adjustclient.module.impl.render.hud.playerlist.menu.ViolationsMenu;
import com.adjustclient.module.impl.render.hud.widget.HudWidget;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.server.ServerType;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.util.Vec2f;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.player.TabPlayer;

public final class PlayerList {

    // ------------------------------------------------------------------------------------------------------ settings / state
    public BooleanSetting enabled;
    public NumberSetting maxPlayers;
    public BooleanSetting hypixelApiEnabled;
    public ColumnToggles columns;
    public Movable movable;
    public Bind holdBind;
    public BooleanSetting autoTargetLevel;
    public BooleanSetting autoTargetWlr;
    public BooleanSetting autoTargetKdr;
    public BooleanSetting autoTargetWs;
    public BooleanSetting selfHighlighting;
    public BooleanSetting selfUsername;
    public BooleanSetting levelColorHighlight;
    public final Map<TargetReason, BooleanSetting> autoTargetSettings = new HashMap<>();
    public ExpandSetting group;
    public List<PlayerListColumn> columnList = new ArrayList<>();
    public List<PlayerEntry> entries = new ArrayList<>();
    public final Object lock = new Object();
    public float[] columnWidths;
    public HoverCardState hoverCard;
    public final Set<UUID> seen = new HashSet<>();
    public final Map<UUID, Integer> counts = new HashMap<>();
    public Animation scrollAnimation = new Animation(com.adjustclient.render.animation.Easing.EASE_OUT_EXPO, 150L);
    public static float cachedWheelDelta;
    public float width;
    public float height;
    public float scroll;
    public boolean needsSort;
    public boolean needsRebuild;
    public boolean apiColumnsActive;

    public PlayerList() {
        HUDModule hud = HUDModule.INSTANCE_ALT;

        Movable movable = Adjust.movableManager.registerMovable("playerList", new com.adjustclient.util.Vec2f(2.0F, 51.0F));
        this.movable = movable;

        // group "Player List": ExpandSetting(parent = HUD module, name); its constructor appends it to hud.getSettings()
        ExpandSetting group = new ExpandSetting(hud, "Player List");
        group.setActiveSupplier(() -> this.enabled.isEnabled());
        this.group = group;

        // Enabled (default true, "Yes"/"No")
        this.enabled = new BooleanSetting(group, "Enabled", true, "Yes", "No");
        this.enabled.addListener(() -> this.hoverCard = null);
        Runnable rebuild = () -> this.needsRebuild = true;
        this.enabled.addListener(rebuild);

        PlayerListColumn.registerColumns();

        this.holdBind = Adjust.INSTANCE.bindManager.register("module.playerlist.holdBind", mode -> {
        }, "Player List Bind");
        new KeybindSetting(group, "Hold Bind", this.holdBind);

        // Columns
        ExpandSetting columnsGroup = new ExpandSetting(group, "Columns");
        this.columns = new ColumnToggles(columnsGroup, rebuild);
        this.columns.settings.get(PlayerListColumn.SEEN).addListener(this::clearCaches);

        // Max. players
        this.maxPlayers = new NumberSetting(group, "Max. players", 20, 5, 50, 1);
        this.maxPlayers.addListener(rebuild);

        // Hypixel API
        ExpandSetting api = new ExpandSetting(group, "Hypixel API");
        api.setActiveSupplier(new HypixelApiEnabledSupplier(this));
        this.hypixelApiEnabled = new BooleanSetting(api, "Enabled", false, "Yes", "No");
        this.hypixelApiEnabled.addListener(this::updateColumns);
        api.getSettings().add(UserConfiguration.HYPIXEL_API_KEY.setting);
        api.getSettings().add(UserConfiguration.ANTI_SNIPER_API_KEY.setting);
        api.getSettings().add(UserConfiguration.URCHIN_API_KEY.setting);

        // Auto-Target
        ExpandSetting autoTarget = new ExpandSetting(group, "Auto-Target");
        autoTarget.setActiveSupplier(() -> {
            for (BooleanSetting setting : this.autoTargetSettings.values()) {
                if (setting.isEnabled()) {
                    return true;
                }
            }
            return false;
        });
        autoTarget.setDescription("Automatically mark players with these conditions met as targets.");
        this.autoTargetLevel = new BooleanSetting(autoTarget, "High Level", false, "On", "Off");
        this.autoTargetSettings.put(TargetReason.HIGH_LEVEL, this.autoTargetLevel);
        this.autoTargetWlr = new BooleanSetting(autoTarget, "High W/L", false, "On", "Off");
        this.autoTargetSettings.put(TargetReason.HIGH_WLR, this.autoTargetWlr);
        this.autoTargetKdr = new BooleanSetting(autoTarget, "High K/D", false, "On", "Off");
        this.autoTargetSettings.put(TargetReason.HIGH_KDR, this.autoTargetKdr);
        this.autoTargetWs = new BooleanSetting(autoTarget, "High WS", false, "On", "Off");
        this.autoTargetSettings.put(TargetReason.HIGH_WS, this.autoTargetWs);

        this.movable.canDrag = (BooleanSupplier) () -> this.enabled.isEnabled() && HUDModule.INSTANCE_ALT.isEnabled();

        // Other
        ExpandSetting other = new ExpandSetting(group, "Other");
        this.selfHighlighting = new BooleanSetting(other, "Self Highlighting", false, "On", "Off");
        this.selfHighlighting.description("Highlights your name with the HUD color.");
        this.selfUsername = new BooleanSetting(other, "Self Username", true, "On", "Off");
        this.selfUsername.description("Displays your Adjust username next to yourself.");
        this.levelColorHighlight = new BooleanSetting(other, "Level Color Highlight", true, "On", "Off");
        this.levelColorHighlight.description("Highlight Hypixel game levels by using different colors.");
    }

    /** Sets the scroll offset: clamped to 0 .. 8 * max(entries.size() - maxPlayers - 1, 0). */
    public void clampScroll(float value) {
        int max = this.maxPlayers.getValue().intValue();
        int overflow = this.entries.size() - max - 1;
        if (overflow < 1) {
            overflow = 0;
        }
        this.scroll = MathHelper.clamp_float(value, 0.0F, (float) overflow * 8.0F);
    }

    public boolean onMouse(int action, float mouseX, float mouseY) {
        HoverCardState card = this.hoverCard;
        if (action == 1) {
            if (card != null) {
                if (card.pinned) {
                    card.menuOpen = true;
                } else {
                    card.pinned = true;
                }
            }
        } else if (action == 0) {
            if (card != null && card.menu != null) {
                card.menu.click(mouseX, mouseY);
            }
        }

        float left = this.movable.updatePosition().x;
        float top = this.movable.updatePosition().y;
        if (mouseX < left || left + this.width < mouseX || mouseY < top || top + this.height + 9.0F < mouseY) {
            return false;
        }
        float wheel = (float) org.lwjgl.input.Mouse.getEventDWheel();
        float direction;
        if (wheel < 0.0F) {
            direction = 1.0F;
        } else if (wheel > 0.0F) {
            direction = -1.0F;
        } else {
            return false;
        }
        this.clampScroll(direction * 8.0F + this.scroll);
        return true;
    }

    /** Increments the counter of the player: {@code counts.put(uuid, counts.containsKey(uuid) ? counts.get(uuid) + 1 : 1)}. */
    public void countPlayer(UUID uuid) {
        if (this.counts.containsKey(uuid)) {
            this.counts.put(uuid, this.counts.get(uuid) + 1);
        } else {
            this.counts.put(uuid, 1);
        }
    }

    /** Clears the per player caches. */
    public void clearCaches() {
        this.seen.clear();
        this.counts.clear();
    }

    static float healthFraction(com.adjustclient.util.anticheat.entity.TrackedFakePlayer target, float partialTicks) {
        float result = 0.0F;
        if (target != null) {
            if (target.removed) {
                return 0.0F;
            }
            if (!target.stale) {
                EntityPlayer player = (EntityPlayer) target.entity;
                int previous = player.previousHurtTime;
                int current = player.hurtTime;
                int maximum = player.maxHurtTime;
                float interpolated = (float) previous + partialTicks * (float) (current - previous);
                float health = player.getDataWatcher().getWatchableObjectFloat(6);
                float value;
                float cap;
                if (0.0F < health) {
                    value = interpolated;
                    cap = (float) maximum;
                } else {
                    int v = player.deathTime;
                    if (v < 0) {
                        v = 0;
                    }
                    value = (float) v;
                    cap = 20.0F;
                }
                value = Math.min(value, cap);
                float fraction = value / (cap * 2.15F);
                if (0.0F <= fraction) {
                    result = fraction;
                }
            }
        }
        return result;
    }

    String getDisplayName(PlayerEntry entry) {
        String name = entry.player.info.getGameProfile().getName();
        com.adjustclient.module.impl.exploit.playerdetect.hypixel.nick.NickDetector holder = PlayerDetectModule.INSTANCE.nickDetector;
        if (holder == null) {
            return name;
        }
        Map<String, NickedPlayer> detected = holder.nickedPlayers;
        if (!detected.containsKey(name)) {
            return name;
        }
        NickedPlayer value = detected.get(name);
        if (value != null) {
            return value.realName;
        }
        // string concat: ChatColor.toString() (or "null") + name
        return ChatColor.ITALIC + name;
    }

    public void onPlayerAdded(TabPlayer player) {
        if (player.entry != null) {
            return;                                                               // already has an entry
        }
        player.entry = new PlayerEntry(player);
        this.rebuildEntries();
        this.layoutColumns(this.columnList);
    }

    void rebuildEntries() {
        ServerTracker tracker = ServerTracker.INSTANCE;
        boolean onHypixel = isOnHypixel();
        List<PlayerEntry> list = new ArrayList<>();
        for (TabPlayer tracked : tracker.playerTracker.players.values()) {
            PlayerEntry entry = tracked.entry;
            if (entry == null) {
                continue;
            }
            com.mojang.authlib.GameProfile profile = tracked.info.getGameProfile();
            boolean isSelf = Minecraft.getMinecraft().thePlayer.getGameProfile().equals(profile);
            if (!isSelf) {
                HypixelGameState state = Adjust.gameStateTracker.state;
                if (state.lobby != null && !state.lobby && state.game == HypixelGame.BED_WARS) {
                    this.seen.add(profile.getId());
                }
            }
            entry.update(onHypixel);
            if (AutoRewoundModule.INSTANCE != null
                    && AutoRewoundModule.INSTANCE.shouldHideBots()
                    && !entry.rewoundRealPlayer) {
                if (this.hoverCard != null && this.hoverCard.entry == entry) {
                    this.hoverCard = null;
                }
                continue;
            }
            list.add(entry);
        }
        java.util.Collections.sort((List) list);
        this.entries = list;
        this.needsSort = false;
        this.clampScroll(this.scroll);
    }

    private static boolean isOnHypixel() {
        com.adjustclient.util.anticheat.blocker.BlockerState serverInfo = ServerTracker.INSTANCE.serverState;
        return serverInfo != null && serverInfo.antiCheatModel != null && serverInfo.antiCheatModel.serverType == ServerType.HYPIXEL;
    }

    private static boolean isOnRewound() {
        com.adjustclient.util.anticheat.blocker.BlockerState serverInfo = ServerTracker.INSTANCE.serverState;
        return serverInfo != null && serverInfo.antiCheatModel != null && serverInfo.antiCheatModel.serverType == ServerType.REWOUND;
    }

    /**
     * Per tick work (HUDModule tick handler, only while the list is shown): updates every entry, then, depending on the dirty flags,
     * re-sorts the entries and/or reloads and re-lays out the columns.
     */
    public void tick() {
        boolean onHypixel = isOnHypixel();
        synchronized (this.lock) {
            for (PlayerEntry entry : this.entries) {
                entry.update(onHypixel);
            }
        }
        if (this.needsRebuild) {
            this.reload();
            this.needsRebuild = false;
            this.needsSort = false;
            this.layoutColumns(this.columnList);
        } else {
            if (this.needsSort) {
                synchronized (this.lock) {
                    this.entries.sort(null);
                }
                this.needsSort = false;
            }
            this.layoutColumns(this.columnList);
        }
    }

    /** Full reload: entries, columns, and the context menu component of the hover card. */
    public void reload() {
        this.rebuildEntries();
        this.updateColumns();
        HoverCardState card = this.hoverCard;
        if (card != null && card.menu != null) {
            if (!(card.menu instanceof ViolationsMenu)) {
                card.menu.build();
            }
        }
    }

    public void relayout() {
        this.rebuildEntries();
        this.layoutColumns(this.columnList);
    }

    public void updateColumns() {
        this.apiColumnsActive = false;
        List<PlayerListColumn> columns = new ArrayList<>(8);
        columns.add(PlayerListColumn.PLAYERS);
        columns.add(PlayerListColumn.DISTANCE);

        HypixelGameState state;
        if (isOnHypixel() && Boolean.FALSE.equals((state = Adjust.gameStateTracker.state).lobby)) {
            boolean api = this.hypixelApiEnabled.isEnabled()
                    && ((HypixelApiManager) Adjust.hypixelApiManager).isHypixelAvailable();
            boolean inGame = state.startTimer != null;
            boolean viaCase3 = false;
            if (state.game != null) {
                switch (PlayerListSwitchMap.GAME[state.game.ordinal()]) {
                case 1:      // SKYWARS
                    if (inGame) {
                        columns.add(PlayerListColumn.HP);
                        columns.add(PlayerListColumn.KILLS);
                    }
                    if (api) {
                        columns.add(PlayerListColumn.WS);
                        columns.add(PlayerListColumn.TKD);
                        columns.add(PlayerListColumn.WL);
                    }
                    break;
                case 2:      // DUELS
                    if (inGame) {
                        columns.add(PlayerListColumn.HP);
                    }
                    if (api) {
                        columns.add(PlayerListColumn.WS);
                        columns.add(PlayerListColumn.TKD);
                        columns.add(PlayerListColumn.WL);
                    }
                    break;
                case 3:      // BED_WARS (has its own tail, see below)
                    if (inGame) {
                        columns.add(PlayerListColumn.HP);
                        columns.add(PlayerListColumn.KD);
                    }
                    if (api) {
                        columns.add(PlayerListColumn.WS);
                        columns.add(PlayerListColumn.FKD);
                        columns.add(PlayerListColumn.WL);
                        columns.add(PlayerListColumn.PING);
                        columns.add(PlayerListColumn.ONLINE_TIME);
                        this.apiColumnsActive = true;
                    }
                    columns.add(PlayerListColumn.SEEN);
                    if (Adjust.hypixelApiManager != null && Adjust.hypixelApiManager.isSeraphAvailable()) {
                        columns.add(PlayerListColumn.TAGS);
                    }
                    viaCase3 = true;
                    break;
                case 4:      // BLITZ_SG
                    if (inGame) {
                        columns.add(PlayerListColumn.HP);
                        columns.add(PlayerListColumn.KILLS);
                    }
                    if (api) {
                        columns.add(PlayerListColumn.TKD);
                    }
                    break;
                case 5:      // UHC_CHAMPIONS
                    if (inGame) {
                        columns.add(PlayerListColumn.HP);
                    }
                    if (api) {
                        columns.add(PlayerListColumn.TKD);
                    }
                    break;
                case 6:      // THE_HYPIXEL_PIT
                    columns.add(PlayerListColumn.HP);
                    if (api) {
                        columns.add(PlayerListColumn.GOLD);
                    }
                    columns.add(PlayerListColumn.BOUNTY);
                    columns.add(PlayerListColumn.ENCHANTS);
                    break;
                case 7:      // HOUSING
                    columns.add(PlayerListColumn.HP);
                    columns.add(PlayerListColumn.GROUP);
                    columns.add(PlayerListColumn.KD);
                    break;
                case 8:      // SHEEP_WARS
                case 9:      // WOOL_WARS
                    columns.add(PlayerListColumn.HP);
                    if (api) {
                        columns.add(PlayerListColumn.TKD);
                        columns.add(PlayerListColumn.WL);
                    }
                    break;
                case 10:     // CAPTURE_THE_WOOL
                    columns.add(PlayerListColumn.HP);
                    if (api) {
                        columns.add(PlayerListColumn.TKD);
                    }
                    break;
                case 11: case 12: case 13: case 14: case 15: case 16: case 17: case 18: case 19:
                    // ATLAS, REPLAY, MURDER_MYSTERY, BUILD_BATTLE, BED_WARS_PRACTICE, HALLOWEEN/GRINCH/EASTER_SIMULATOR, TNT_TAG: only o and d
                    break;
                default:     // every other game
                    columns.add(PlayerListColumn.HP);
                    break;
                }
            }
            if (!viaCase3 && api) {
                columns.add(PlayerListColumn.PING);
                columns.add(PlayerListColumn.ONLINE_TIME);
                this.apiColumnsActive = true;
            }
        } else if (isOnRewound() && Boolean.FALSE.equals(Adjust.rewoundGameTracker.state.lobby)) {
            RewoundGameState rewoundState = Adjust.rewoundGameTracker.state;
            boolean inGame = rewoundState.startTimer != null;
            if (rewoundState.game != null) {
                switch (PlayerListSwitchMap.REWOUND_GAME[rewoundState.game.ordinal()]) {
                case 1:      // SKYWARS
                case 4:      // SURVIVAL_GAMES
                    if (inGame) {
                        columns.add(PlayerListColumn.HP);
                        columns.add(PlayerListColumn.KILLS);
                    }
                    break;
                case 2:      // DUELS
                    if (inGame) {
                        columns.add(PlayerListColumn.HP);
                    }
                    break;
                case 3:      // BEDWARS
                    if (inGame) {
                        columns.add(PlayerListColumn.HP);
                        columns.add(PlayerListColumn.KD);
                    }
                    break;
                case 5:      // PIT
                    columns.add(PlayerListColumn.HP);
                    columns.add(PlayerListColumn.BOUNTY);
                    columns.add(PlayerListColumn.ENCHANTS);
                    break;
                case 6:      // REPLAY
                    break;
                default:
                    columns.add(PlayerListColumn.HP);
                    break;
                }
            }
        } else {
            columns.add(PlayerListColumn.HP);
        }
        columns.add(PlayerListColumn.CLIENT);

        Predicate<PlayerListColumn> enabledColumn = column -> this.columns.isEnabled(column);
        columns.removeIf(enabledColumn.negate());
        this.layoutColumns(columns);
        this.columnList = columns;
    }

    public void layoutColumns(List<PlayerListColumn> columns) {
        float[] widths = new float[columns.size()];
        float offset = -this.scroll;               // rows scrolled out above the view have a negative y
        if (widths.length == 0) {
            this.columnWidths = widths;
            this.width = 0.0F;
            this.height = offset;
            return;
        }
        final float rowHeight = 8.0F;
        final float padding = 5.0F;
        NvgFontRenderer font = Fonts.TAHOMA_10;
        if (font == null) {
            this.columnWidths = widths;
            this.width = 0.0F;
            this.height = offset;
            return;
        }
        float totalWidth = 0.0F;
        float y = offset;
        int counted = 0;
        for (int i = 0; i < widths.length; i++) {
            PlayerListColumn column = columns.get(i);
            String header = column.getName();
            float widest = font.getStringWidth(header) + padding;
            synchronized (this.lock) {
                for (PlayerEntry entry : this.entries) {
                    float rowY = y;
                    if (rowY >= 0.0F) {
                        float w = column.getWidth(entry);
                        if (w > widest) {
                            widest = w;
                        }
                    }
                    if (i == 0) {
                        y = rowY + rowHeight;
                        if (0.0F < rowY) {
                            counted++;
                            if (counted > this.maxPlayers.getValue().intValue()) {
                                y = rowY;
                            }
                        }
                    }
                }
            }
            widths[i] = widest;
            totalWidth += widest;
        }
        this.columnWidths = widths;
        this.width = totalWidth;
        this.height = y;
    }

    public void render(com.adjustclient.event.ScreenRender2DEvent event) {
        if (this.columnWidths == null) {
            if (this.scrollAnimation != null) {
                this.scrollAnimation.value = 0.0F;
            }
            this.scroll = 0.0F;
            return;
        }
        Movable movable = this.movable;
        float panelWidth = this.width;
        float panelHeight = this.height;
        float mouseX = event.mouseX;
        float mouseY = event.mouseY;
        Minecraft mc = Minecraft.getMinecraft();

        float x;
        float y;
        if (movable != null) {
            movable.height = panelHeight + 9.0F;
            movable.width = panelWidth;
            movable.headerHeight = 9.0F;
            com.adjustclient.util.Vec2f position = movable.updatePosition();
            x = position.x;
            y = movable.updatePosition().y;
            movable.drawOverlay(event.scaledResolution);
        } else {
            x = panelWidth;
            y = panelHeight;
        }
        HudWidget.drawPanel(140, x, y, panelWidth, panelHeight);

        float animatedScroll = this.scrollAnimation.update(this.scroll);

        boolean hasScreen = mc.currentScreen != null;                      // mouse is usable only in a GUI
        boolean mouseInsideX = mouseX > x && (x + panelWidth) > mouseX;
        float contentTop = y + 9.0F;
        RenderUtil.pushScissor(event.scaledResolution, 1, x, contentTop, panelWidth, panelHeight);

        List<Runnable> deferred = new ArrayList<>();
        PlayerEntry hovered = null;
        float cursorX = x;
        for (int i = 0; i < this.columnList.size(); i++) {
            PlayerListColumn column = this.columnList.get(i);
            float columnWidth = this.columnWidths[i];
            column.x = columnWidth / 2.0F + cursorX;                   // column centre x
            synchronized (this.lock) {
                float rowY = contentTop + 3.5F - animatedScroll;
                for (PlayerEntry entry : this.entries) {
                    if (rowY < contentTop - 4.0F) {                         // above the visible area
                        rowY += 8.0F;
                        continue;
                    }
                    if (i == 0) {
                        // the hover test, the hover fade and the highlight are only done for the first column (`if (columnIndex != 0)` skips them)
                        boolean rowHovered = hasScreen && mouseInsideX && mouseY > rowY - 3.0F && mouseY <= rowY + 4.5F;
                        if (rowHovered) {
                            hovered = entry;
                        }
                        HoverCardState shownCard = this.hoverCard;
                        boolean highlighted = rowHovered || (shownCard != null && shownCard.entry == entry);
                        float fade = entry.animation.update(highlighted ? 1.0F : 0.0F);
                        int alpha = (int) (100.0F * fade);
                        if (alpha >= 0x100) {
                            throw new IllegalArgumentException("Color parameter outside of expected range: Alpha");
                        }
                        NvgDraw.fillRect((alpha << 24) | 0x9B9B9B, x + 0.5F, rowY - 3.0F, x + panelWidth - 0.5F, rowY + 4.5F);
                        if (entry.status != RowTint.NONE) {
                            NvgDraw.fillRect(entry.statusColor, x + 0.5F, rowY - 3.0F, x + panelWidth - 0.5F, rowY + 4.5F);
                        }
                    }
                    column.render(deferred, entry, cursorX, rowY);
                    // the break test comes after the draw: the row that crosses the bottom edge is still drawn (clipped by the scissor)
                    if (rowY + 8.0F > contentTop + panelHeight) {
                        break;
                    }
                    rowY += 8.0F;
                }
            }
            cursorX += columnWidth;
        }
        RenderUtil.popScissor();

        NvgFontRenderer headerFont = Fonts.TAHOMA_10;
        float headerY = y + 4.0F;
        for (PlayerListColumn column : this.columnList) {
            String title = column.getName();
            headerFont.drawString(title, column.x - headerFont.getStringWidth(title) * 0.5F, headerY, 0xDEF0F0F0);
        }

        if (!deferred.isEmpty()) {
            NvgContext.flushFrame();
            RenderUtil.pushScissor(event.scaledResolution, 1, x, contentTop, panelWidth, panelHeight);
            deferred.forEach(Runnable::run);
            RenderUtil.popScissor();                                    // pop scissor
        }
        // hover card of the row under the mouse (or the pinned one)
        this.drawHoverCard(event, hovered, mouseX, mouseY, event.partialTicks);
    }

    void drawHoverCard(com.adjustclient.event.ScreenRender2DEvent event, PlayerEntry hovered, float mouseX, float mouseY, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        HoverCardState card = this.hoverCard;
        if (hovered == null) {
            if (card == null) {
                return;
            }
        } else {
            RenderUtil.cursor = 221188;
            if (card == null) {
                card = new HoverCardState(hovered);
                this.hoverCard = card;
            }
            if (!card.pinned) {
                card.entry = hovered;
            } else if (card.menuOpen) {
                card.entry = hovered;
            }
            if (card.menuOpen) {
                PlayerContextMenu menu = new PlayerContextMenu(card);
                menu.build();
                card.menu = menu;
                card.position = new Vec2f(mouseX, mouseY);
                card.menuOpen = false;
            }
        }
        // the card follows the mouse as long as it is not pinned (and a screen is open)
        if (mc.currentScreen != null && !card.pinned) {
            card.position = new Vec2f(mouseX, mouseY);
        }
        PlayerEntry shown = card.entry;
        boolean notHovered = hovered == null || shown != hovered;
        com.adjustclient.util.anticheat.entity.TrackedFakePlayer target = ServerTracker.INSTANCE.entityTracker.getByName(shown.player.info.getGameProfile().getName());
        card.trackedPlayer = target;
        String name = this.getDisplayName(shown);
        NvgFontRenderer nameFont = Fonts.TAHOMA_12;
        float textWidth = nameFont.getStringWidth(name);

        // widest text decides the card width: name (Tahoma 12), "First login" / "<time> ago" (Tahoma 8 + 18), ping text (Tahoma 8); width = max(20, widest + 48)
        float widest = textWidth;
        com.adjustclient.hypixel.api.model.player.HypixelPlayer api = shown.hypixelStats;
        String language = null;
        String channel = null;
        if (api != null) {
            com.adjustclient.hypixel.api.model.player.HypixelLanguage lang = api.getUserLanguage();
            if (lang != null) {
                language = lang.displayName;
                widest = Math.max(widest, Fonts.TAHOMA_10.getStringWidth(language));
            }
            com.adjustclient.hypixel.api.model.player.HypixelChannel chatChannel = api.getChannel();
            if (chatChannel != null && chatChannel != com.adjustclient.hypixel.api.model.player.HypixelChannel.ALL) {
                channel = chatChannel.displayName;
                widest = Math.max(widest, Fonts.TAHOMA_8.getStringWidth(channel) + 18.0F);
            }
        }
        String ago = null;
        float recent = 0.0F;
        long now = System.currentTimeMillis();
        if (api != null && api.firstLogin != null) {
            long firstLogin = api.firstLogin;
            ago = com.adjustclient.util.TimeUtil.timeAgo(firstLogin, " ago");
            recent = Math.max(0.0F, Math.min(1.0F, (float) (firstLogin - (now - 172800000L)) / 1.728E8F));   // 1 = first login within the last two days
            widest = Math.max(widest, Math.max(Fonts.TAHOMA_8.getStringWidth(ago) + 18.0F, Fonts.TAHOMA_8.getStringWidth("First login") + 18.0F));
        }
        int ping;
        if (shown.ping != null && shown.ping.value != null) {
            ping = shown.ping.value;
        } else {
            ping = shown.player.info.getResponseTime();
            if (ping == 1 && target != null && target.entity.isUser()
                    && ServerTracker.INSTANCE.serverState != null
                    && ServerTracker.INSTANCE.serverState.packetWatcher != null) {
                ping = ServerTracker.INSTANCE.serverState.packetWatcher.getAverageDelay();
            }
        }
        if (ping != 1) {
            widest = Math.max(widest, Fonts.TAHOMA_8.getStringWidth(ping + "ms"));
        }
        float cardWidth = Math.max(20.0F, widest + 48.0F);
        float left = card.position != null ? card.position.x : mouseX;
        float top = card.position != null ? card.position.y : mouseY;
        float cardHeight = 38.0F;
        if (card.pinned) {
            if (card.menu == null) {
                PlayerContextMenu menu = new PlayerContextMenu(card);
                menu.build();
                card.menu = menu;
            }
            cardHeight += card.menu.layout(left + 2.5F, top + 41.5F);
        }

        float pinned = card.pinned ? 1.0F : 0.0F;
        boolean listed = this.entries.indexOf(shown) >= 0;
        if (card.position != null) {
            if (hovered != null && shown == hovered) {
                card.pinFade.update(pinned);
                card.visible.update(1.0F);
                card.hoverTimer = null;
            } else if (card.pinned && mouseX >= left && mouseX <= left + cardWidth && mouseY >= top && mouseY <= top + cardHeight && listed) {
                card.pinFade.update(1.0F);
                card.visible.update(1.0F);
                card.hoverTimer = null;
            } else if (card.pinned && mc.currentScreen != null && listed) {
                if (card.hoverTimer == null) {
                    card.hoverTimer = new Timer();
                }
                float visibleTarget = card.hoverTimer.lastMs + 1200L <= System.currentTimeMillis() ? 0.0F : 1.0F;
                card.pinFade.update(0.0F);
                card.visible.update(visibleTarget);
            } else {
                card.hoverTimer = null;
                card.pinFade.update(0.0F);
                card.visible.update(0.0F);
            }
        } else {
            card.pinFade.update(pinned);
            card.visible.update((hovered != null && shown == hovered) || card.pinned ? 1.0F : 0.0F);
        }
        card.grow.duration = 420L;                                 // duration of the size animation
        float grow = card.grow.update(cardHeight);               // current animated height

        // ---- drawing (NanoVG): scissor to the animated size, then background, border, separator, head frame, texts ----
        float fade = card.visible.value;                       // current visibility (0..1)
        RenderUtil.pushScissor(event.scaledResolution, 1, left, top - 0.5F, cardWidth, grow + 1.0F);   // scissor (x, y - 0.5, w, animated height + 1)
        int bgAlpha = (int) ((20.0F * card.pinFade.value + 140.0F) * fade);
        int background = (bgAlpha << 24) | 0x191919;             // Color(25, 25, 25, bgAlpha)
        NvgContext.pushEvenScale();
        Vector4f r = NvgContext.toPixels(left, top, left + cardWidth, top + grow);
        NvgDraw.fillRect(background, r.x, r.y, r.z, r.w);
        int textColor = ((int) (fade * 200.0F) << 24) | 0xE9E9E9;
        float lineWidth = NvgContext.scaleFactor == null ? 0.5F
                : (float) (NvgContext.roundScaledPixels ? Math.round(NvgContext.scaleFactor) : (int) (float) NvgContext.scaleFactor) * 0.5F;
        NvgDraw.strokeRect(false, background, r.x, r.y, r.z, r.w, lineWidth);
        if (card.pinned && grow > 38.0F) {
            Vector4f line = NvgContext.toPixels(left + 0.5F, top + 37.5F, left + cardWidth, top + 38.0F);
            NvgDraw.fillRect(background, r.x - 0.5F, line.y, line.z + 0.5F, line.w);
        }
        NvgContext.popEvenScale();
        if (card.pinned) {
            card.menu.render(left + 2.5F, top + 41.5F, cardWidth, mouseX, mouseY, fade);
        }

        // head frame: 32 x 32 square at (left + 3, top + 3); the row tint colour (alpha 140 * fade) when the player has a status, else the background colour
        float healthFlash = healthFraction(target, partialTicks);
        int frameColor = shown.status != RowTint.NONE ? ((shown.statusColor & 0xFFFFFF) | ((int) (fade * 140.0F) << 24)) : background;
        NvgContext.pushEvenScale();
        Vector4f head = NvgContext.toPixels(left + 3.0F, top + 3.0F, left + 3.0F + 32.0F, top + 3.0F + 32.0F);
        NvgDraw.strokeRect(true, frameColor, head.x, head.y, head.z, head.w, 0.5F);
        NvgContext.popEvenScale();

        float textX = left + 38.0F;                              // after the head (x + 38)
        int nameColor = textColor;
        net.minecraft.scoreboard.ScorePlayerTeam playerTeam = mc.theWorld != null ? shown.player.info.getPlayerTeam() : null;
        if (playerTeam != null) {
            String format = net.minecraft.client.gui.FontRenderer.getFormatFromString(playerTeam.getColorPrefix());
            if (format.length() > 1) {
                nameColor = (mc.fontRendererObj.getColorCode(format.charAt(1)) & 0xFFFFFF) | ((int) (fade * 255.0F) << 24);   // team colour of the name
            }
        }
        nameFont.drawString(name, textX, top + 4.5F, nameColor);
        float lineY = top + 11.5F;
        if (language != null) {
            Fonts.TAHOMA_10.drawString(language, textX, lineY, textColor);
            lineY += 6.0F;
        }
        float bottom = top + 38.0F;
        if (ago != null) {
            // "<time> ago" in red blended towards the text colour by how recent the first login is, "First login" above it
            int red = new java.awt.Color(255, 85, 85, Math.max(0, Math.min(255, (int) (fade * 200.0F)))).getRGB();
            Fonts.TAHOMA_8.drawString(ago, textX, bottom - 5.0F, com.adjustclient.render.ColorUtil.blend(new java.awt.Color(red, true), new java.awt.Color(textColor, true), recent).getRGB());
            Fonts.TAHOMA_8.drawString("First login", textX, bottom - 5.0F - 4.5F, textColor);
        }
        if (channel != null) {
            Fonts.TAHOMA_8.drawString(channel, textX, bottom - 5.0F - (ago != null ? 9.0F : 0.0F), textColor);
        }
        com.adjustclient.util.anticheat.player.PlayerKillStats kills =
                com.adjustclient.util.anticheat.player.PlayerStatsCache.INSTANCE.get(shown.player.info.getGameProfile().getName());
        if (kills != null) {
            String killText = kills.kills + " kill" + (kills.kills == 1 ? "" : "s");
            Fonts.TAHOMA_8.drawString(killText, left + cardWidth - Fonts.TAHOMA_8.getStringWidth(killText) - 2.0F, bottom - 9.0F, textColor);
            String deathText = kills.deaths + " death" + (kills.deaths == 1 ? "" : "s");
            Fonts.TAHOMA_8.drawString(deathText, left + cardWidth - Fonts.TAHOMA_8.getStringWidth(deathText) - 2.0F, bottom - 5.0F, textColor);
        }
        com.adjustclient.hypixel.HypixelGameState gameState = Adjust.gameStateTracker.state;
        if (ping == 1 && gameState != null && Boolean.FALSE.equals(gameState.lobby) || ping < 0) {
            Fonts.TAHOMA_8.drawString("Ping hidden", textX, lineY, textColor);
        } else {
            Fonts.TAHOMA_8.drawString(ping + "ms", textX, lineY, textColor);
        }
        // vanilla ping bars + head: after NvgContext.flushFrame():
        NvgContext.flushFrame();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        drawHead(shown, left + 3.0F, top + 3.0F, healthFlash, fade);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, fade * 0.6F);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        GuiPlayerTabOverlay.drawPing(ping, 0, (left + cardWidth - 3.0F) * 2.0F, (top + 4.0F) * 2.0F, 0.0F);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        RenderUtil.popScissor();                              // pop scissor
        if (!(hovered != null && shown == hovered) && card.visible.value == 0.0F) {
            this.hoverCard = null;
        }
    }

    static void drawHead(PlayerEntry entry, float x, float y, float hurt, float alpha) {
        NetworkPlayerInfo info = entry.player.info;
        net.minecraft.util.ResourceLocation skin = info.getLocationSkin();
        float t = MathHelper.clamp_float(hurt, 0.0F, 1.0F);
        Color white = Color.WHITE;
        float u = 1.0F - t;
        int r = (int) ((double) t * 237.0 + (double) white.getRed() * u);
        int g = (int) ((double) white.getGreen() * u + (double) t * 60.0);
        int b = (int) ((double) white.getBlue() * u + (double) t * 60.0);
        int a = (int) ((double) t * 255.0 + (double) white.getAlpha() * u);
        Color tint = new Color(r, g, b, a);
        int alpha255 = (int) (alpha * 255.0F);
        tint = new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), Math.max(0, Math.min(255, alpha255)));
        GlStateManager.color(tint.getRed() / 255.0F, tint.getGreen() / 255.0F, tint.getBlue() / 255.0F, tint.getAlpha() / 255.0F);   // cached colour check + glColor4f
        RenderUtil.drawHead(skin, x, y);                                   // RenderUtil.drawHead(ResourceLocation, x, y)
    }
}
