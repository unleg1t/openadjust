package com.adjustclient.modules.impl.render.hud.eventtimer;

import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.core.events.Render2DEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BooleanSupplier;
import com.adjustclient.Adjust;
import com.adjustclient.utilities.game.movable.Movable;
import com.adjustclient.utilities.game.movable.MovableAnchor;
import com.adjustclient.utilities.hypixel.HypixelGameState;
import com.adjustclient.utilities.hypixel.api.client.EventTimerFetcher;
import com.adjustclient.utilities.hypixel.api.model.events.EventTimerEntry;
import com.adjustclient.utilities.hypixel.game.SkyWarsGameState;
import com.adjustclient.utilities.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.utilities.hypixel.game.bedwars.BedWarsTimerProtocol;
import com.adjustclient.utilities.hypixel.game.duels.DuelsGameState;
import com.adjustclient.utilities.hypixel.game.zombies.ZombiesGameState;
import com.adjustclient.core.rendering.font.TextRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.ChatColor;

public final class EventTimers {

    static final Map<String, ItemStack> PIT_ICONS = new HashMap<>();
    static final Map<String, ItemStack> OTHER_ICONS = new HashMap<>();

    static {
        PIT_ICONS.put("KOTH", new ItemStack(Item.getItemFromBlock(Blocks.beacon)));
        PIT_ICONS.put("KOTL", new ItemStack(Item.getItemFromBlock(Blocks.ladder)));
        PIT_ICONS.put("Spire", new ItemStack(Items.magma_cream));
        PIT_ICONS.put("Rage Pit", new ItemStack(Items.baked_potato));
        PIT_ICONS.put("Dragon Egg", new ItemStack(Item.getItemFromBlock(Blocks.dragon_egg)));
        PIT_ICONS.put("Care Package", new ItemStack(Item.getItemFromBlock(Blocks.chest)));
        PIT_ICONS.put("Team Deathmatch", new ItemStack(Items.diamond_sword));
        PIT_ICONS.put("Quick Maths", new ItemStack(Items.writable_book));
        PIT_ICONS.put("Auction", new ItemStack(Item.getItemFromBlock(Blocks.gold_block)));
        PIT_ICONS.put("2x Rewards", new ItemStack(Items.emerald));
        PIT_ICONS.put("Giant Cake", new ItemStack(Items.cake));
        PIT_ICONS.put("Blockhead", new ItemStack(Item.getItemFromBlock(Blocks.brick_block)));
        PIT_ICONS.put("Raffle", new ItemStack(Items.name_tag));
        PIT_ICONS.put("Squads", new ItemStack(Items.banner));
        PIT_ICONS.put("Beast", new ItemStack(Items.diamond_chestplate));
        PIT_ICONS.put("Robbery", new ItemStack(Items.gold_nugget));
        PIT_ICONS.put("All bounty", new ItemStack(Items.gold_ingot));
        PIT_ICONS.put("Pizza", new ItemStack(Items.bread));
        OTHER_ICONS.put("Start", new ItemStack(Item.getItemFromBlock(Blocks.glass)));
        OTHER_ICONS.put("Refill", new ItemStack(Item.getItemFromBlock(Blocks.chest)));
        OTHER_ICONS.put("Doom", new ItemStack(Item.getItemFromBlock(Blocks.dragon_egg)));
        OTHER_ICONS.put("Game End", new ItemStack(Item.getItemFromBlock(Blocks.bedrock)));
    }

    public BooleanSetting enabled;
    public NumberSetting visible;
    public Movable movable;
    public Timer lastShown;
    public ExpandSetting group;
    public TimerEntry[] entries;
    public List<EventTimerEntry> apiEvents;
    public Timer lastFetch;
    public float height;

    /** Row height of the list. */
    private static final float ROW_HEIGHT = 18.0F;

    public EventTimers() {
        HUDModule hud = HUDModule.INSTANCE;
        BooleanSupplier header = () -> this.enabled.isEnabled();
        this.group = new ExpandSetting(hud, "Event Timers");
        this.group.enabledPredicate = header;
        this.enabled = new BooleanSetting(this.group, "Enabled", false);   // JSON default false
        this.enabled.addListener(() -> {
            if (this.enabled.isEnabled()) {
                if (Minecraft.getMinecraft().theWorld == null) {
                    this.lastShown = null;
                    return;
                }
                this.lastShown = new Timer();                            // Timer(now)
                this.tick();
            } else {
                this.lastShown = null;
            }
        });
        this.visible = new NumberSetting(this.group, "Visible", 3, 1, 7, 1);
        this.movable = Adjust.movableManager.registerMovable("eventTimers", new com.adjustclient.utilities.Vec2f(2.0F, 200.0F));
        this.movable.canDrag = () -> this.enabled.isEnabled() && HUDModule.INSTANCE_ALT.isEnabled()
                && this.entries != null && Minecraft.getMinecraft().thePlayer != null;
    }

    public void tick() {
        if (this.lastShown != null && this.lastShown.lastMs + 1500L <= System.currentTimeMillis()) {
            this.lastShown = null;                                       // the window fades out 1.5 s after the last tick
        } else if (this.lastShown != null) {
            // still fresh: keep
        }
        HypixelGameState state = Adjust.gameStateTracker == null ? null : Adjust.gameStateTracker.state;
        if (state == null || Adjust.gameStateTracker.state.lobby == null || state.lobby) {
            this.showUnavailable();
            return;
        }
        int rows = 0;
        if (state instanceof BedWarsGameState) {
            List<BedWarsTimerProtocol.TimerMessage> events = ((BedWarsGameState) state).timerProtocol.getTimers();
            if (events.isEmpty()) {
                this.showUnavailable();
                return;
            }
            rows = Math.min(events.size(), this.visible.getInt());
            this.entries = new TimerEntry[rows];
            this.height = rows * ROW_HEIGHT;
            this.lastShown = null;
            for (int i = 0; i < rows; i++) {
                BedWarsTimerProtocol.TimerMessage event = events.get(i);
                long remaining = 50L * event.time - (System.currentTimeMillis() - event.updatedAt);
                TimerEntry entry = new TimerEntry();
                entry.name = event.name;
                entry.icon = event.item;
                entry.time = formatTime(remaining);
                this.entries[i] = entry;
            }
            this.height = this.entries.length * ROW_HEIGHT;
        } else if (state instanceof SkyWarsGameState) {
            com.adjustclient.utilities.game.server.GameCountdown countdown = ((SkyWarsGameState) state).countdown;
            if (countdown == null) {
                this.showUnavailable();
                return;
            }
            String curName = countdown.name();
            int curTime = countdown.countdown();
            ItemStack icon = OTHER_ICONS.getOrDefault(curName, new ItemStack(Items.sign));
            TimerEntry entry = new TimerEntry();
            entry.name = curName;
            entry.icon = icon;
            entry.time = formatTime(curTime);
            this.entries = new TimerEntry[] {entry};
            this.height = ROW_HEIGHT;
            this.lastShown = null;
        } else if (state instanceof DuelsGameState) {
            com.adjustclient.utilities.game.server.GameCountdown countdown = ((DuelsGameState) state).countdown;
            if (countdown == null) {
                this.showUnavailable();
                return;
            }
            TimerEntry entry = new TimerEntry();
            entry.name = countdown.name();
            entry.icon = new ItemStack(Items.iron_sword);
            entry.time = formatTime(countdown.countdown());
            this.entries = new TimerEntry[] {entry};
            this.height = ROW_HEIGHT;
            this.lastShown = null;
        } else if (state instanceof ZombiesGameState) {
            ZombiesGameState zombies = (ZombiesGameState) state;
            if (zombies.roundTimer == null || zombies.difficulty == null) {
                this.showUnavailable();
                return;
            }
            long elapsed = System.currentTimeMillis() - zombies.roundTimer.lastMs;
            int visibleRows = this.visible.getInt();
            List<TimerEntry> list = new ArrayList<>();
            com.adjustclient.utilities.hypixel.game.zombies.round.ZombiesRoundTiming[] generators = zombies.getRoundTimings();
            if (generators != null) {
                int wave = 1;
                for (int i = 0; i < generators.length && list.size() < visibleRows; i++) {
                    com.adjustclient.utilities.hypixel.game.zombies.round.ZombiesRoundTiming generator = generators[i];
                    if (generator == null) continue;
                    long remaining = 50L * generator.ticks() - elapsed;
                    ItemStack icon;
                    String extra;
                    String time;
                    if (remaining >= 1) {
                        time = formatTime(remaining);
                    } else {
                        time = ChatColor.GREEN + "Spawned!";
                    }
                    com.adjustclient.utilities.hypixel.game.zombies.round.ZombiesBoss boss;
                    switch (zombies.difficulty) {
                        case NORMAL: boss = generator.normalTag(); break;
                        case HARD: boss = generator.hardTag(); break;
                        case RIP: boss = generator.ripTag(); break;
                        default: throw new MatchException(null, null);
                    }
                    if (boss == com.adjustclient.utilities.hypixel.game.zombies.round.ZombiesBoss.NONE) {
                        icon = new ItemStack(Items.skull, 1, 2);
                        extra = null;
                    } else {
                        if (boss == null || boss.iconSupplier == null) continue;
                        icon = boss.iconSupplier.get();
                        extra = boss.displayName;
                    }
                    TimerEntry entry = new TimerEntry();
                    entry.name = "Wave " + wave++;
                    entry.icon = icon;
                    entry.time = time;
                    entry.extra = extra;
                    list.add(entry);
                }
            }
            if (list.isEmpty()) {
                this.showUnavailable();
                return;
            }
            this.entries = list.toArray(new TimerEntry[0]);
            this.height = this.entries.length * ROW_HEIGHT;
            this.lastShown = null;
        } else if (state instanceof com.adjustclient.utilities.hypixel.game.pit.PitGameState) {
            if (this.lastFetch == null || this.lastFetch.lastMs + 1_200_000L <= System.currentTimeMillis()) {
                CompletableFuture.supplyAsync(Adjust.hypixelApiManager.eventTimerFetcher.fetchTask(), ForkJoinPool.commonPool())
                        .thenAccept(this::acceptEvents);
                this.lastFetch = new Timer();                            // Timer(now)
            }
            if (this.apiEvents == null) {
                this.showUnavailable();
                return;
            }
            long now = System.currentTimeMillis();
            this.apiEvents.removeIf(e -> e.time <= now);
            int n = Math.min(this.apiEvents.size(), this.visible.getInt());
            this.entries = new TimerEntry[n];
            this.height = n * ROW_HEIGHT;
            this.lastShown = null;
            for (int i = 0; i < n; i++) {
                EventTimerEntry event = this.apiEvents.get(i);
                TimerEntry entry = new TimerEntry();
                entry.name = event.event;
                entry.icon = PIT_ICONS.getOrDefault(event.event, new ItemStack(Item.getItemFromBlock(Blocks.barrier)));
                entry.time = formatTime(event.time - now);
                entry.extra = event.name;
                this.entries[i] = entry;
            }
            this.height = n * ROW_HEIGHT;
        } else {
            this.showUnavailable();
        }
    }

    private void acceptEvents(List<EventTimerEntry> events) {
        this.apiEvents = events;
    }

    /** Installs the single "No timers available" row with the reason as its second line. */
    private void showUnavailable() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null
                ? (mc.currentScreen instanceof net.minecraft.client.gui.GuiDownloadTerrain && this.lastShown == null)
                : this.lastShown == null) {
            this.entries = null;
            return;
        }
        String reason = null;
        HypixelGameState state = Adjust.gameStateTracker.state;
        // lobby == FALSE and the game maps to a supported case of the switch map (1..4: Pit, Duels, SkyWars, Bed Wars)
        if (state.lobby != null && !state.lobby) {
            int mapped = EventTimersSwitchMap.GAME[state.game.ordinal()];
            if (mapped >= 1 && mapped <= 4) {
                reason = state.game == HypixelGame.THE_HYPIXEL_PIT ? "Please wait..." : "Wait for the game to start.";
            }
        }
        if (reason == null) {
            reason = "Unsupported?";
            com.adjustclient.utilities.anticheat.blocker.BlockerState serverState = com.adjustclient.utilities.anticheat.ServerTracker.INSTANCE.serverState;
            if (serverState != null && serverState.antiCheatModel != null
                    && serverState.antiCheatModel.serverType == com.adjustclient.utilities.server.ServerType.HYPIXEL) {
                Boolean lobby = Adjust.gameStateTracker.state.lobby;
                if (lobby == null || lobby) {
                    reason = "You aren't in a game!";
                }
            }
        }
        TimerEntry entry = new TimerEntry();
        entry.name = "No timers available";
        entry.icon = new ItemStack(Items.sign);
        entry.time = reason;
        this.entries = new TimerEntry[] {entry};
        this.height = ROW_HEIGHT;
    }

    /** Render2D: draws the window ("Event Timers" header + rows) at the movable position; fades out 1 s after the last tick. */
    public void render(Render2DEvent event) {
        if (this.entries == null) {
            return;
        }
        float fade = 1.0F;
        int bgR = 222, bgG = 140;
        if (this.lastShown != null) {
            long since = System.currentTimeMillis() - this.lastShown.lastMs;
            if (since >= 1000L) {
                float t = (float) (since - 1000L) / 500.0F;               // fade over 500 ms after the first second
                fade = 1.0F - t;
                if (fade < 0.05F) {
                    return;
                }
                bgR = (int) (fade * 222.0F);
                bgG = (int) (fade * 140.0F);
            }
        }
        // movable: width 91, height 9, y = list height + 9
        this.movable.height = this.height + 9.0F;
        this.movable.width = 91.0F;
        this.movable.headerHeight = 9.0F;
        com.adjustclient.utilities.Vec2f position = this.movable.updatePosition();
        float x = position.x;
        float y = position.y;
        this.movable.drawOverlay(event.scaledResolution);
        com.adjustclient.modules.impl.render.hud.widget.HudWidget.drawPanel(bgG, x, y, 91.0F, this.height);
        // header: "Event Timers" centred, Color(240, 240, 240, alpha)
        float centre = x + 45.5F;
        float titleY = y + 4.0F;
        com.adjustclient.core.rendering.font.Fonts.TAHOMA_10.drawString("Event Timers",
                centre - com.adjustclient.core.rendering.font.Fonts.TAHOMA_10.getStringWidth("Event Timers") * 0.5F, titleY, (bgR << 24) | 0xF0F0F0);
        float rowY = y + 9.0F + 1.5F;
        for (TimerEntry entry : this.entries) {
            entry.render(x, rowY, fade, event.postTasks);
            rowY += 18.0F;
        }
    }

    private static String formatTime(long ms) {
        if (ms < 1) {
            return "0s";
        }
        if (ms < 60_000L) {
            return (ms / 1000L) + "s";
        }
        if (ms < 3_600_000L) {
            return (ms / 60_000L) + "m " + ((ms - 60_000L * (ms / 60_000L)) / 1000L) + "s";
        }
        if (ms < 86_400_000L) {
            return (ms / 3_600_000L) + "h " + ((ms - 3_600_000L * (ms / 3_600_000L)) / 60_000L) + "m "
                    + ((ms - 60_000L * (ms / 60_000L)) / 1000L) + "s";
        }
        return (ms / 86_400_000L) + "d " + ((ms - 86_400_000L * (ms / 86_400_000L)) / 3_600_000L) + "h "
                + ((ms - 3_600_000L * (ms / 3_600_000L)) / 60_000L) + "m " + ((ms - 60_000L * (ms / 60_000L)) / 1000L) + "s";
    }
}
