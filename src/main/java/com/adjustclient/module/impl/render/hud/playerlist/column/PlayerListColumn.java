package com.adjustclient.module.impl.render.hud.playerlist.column;

import com.adjustclient.module.impl.render.hud.playerlist.stats.PlayerEntry;

import java.util.ArrayList;
import java.util.List;

public abstract class PlayerListColumn {
    public float x;

    public static final List<PlayerListColumn> COLUMNS = new ArrayList<>();

    public static PlayersColumn PLAYERS;
    public static DistanceColumn DISTANCE;
    public static HpColumn HP;
    public static GroupColumn GROUP;
    public static KdColumn KD;
    public static KillsColumn KILLS;
    public static WsColumn WS;
    public static GoldColumn GOLD;
    public static BountyColumn BOUNTY;
    public static EnchantsColumn ENCHANTS;
    public static TkdColumn TKD;
    public static FkdColumn FKD;
    public static WlColumn WL;
    public static PingColumn PING;
    public static OnlineTimeColumn ONLINE_TIME;
    public static SeenColumn SEEN;
    public static TagsColumn TAGS;
    public static ClientColumn CLIENT;

    /** Header text of the column (also the name of its on/off setting). */
    public abstract String getName();

    /** Pixel width of the column cell for this entry. */
    public abstract float getWidth(PlayerEntry entry);

    /**
     * Draws the cell. {@code deferred} collects render tasks that have to run after the text pass (head icons, the Adjust logo).
     */
    public abstract void render(List<Runnable> deferred, PlayerEntry entry, float x, float y);

    /** True if the column's setting is on by default (false for Gold and Seen). */
    public boolean isEnabledByDefault() {
        return true;
    }

    /** True if the user can switch the column off (false for the Players column). */
    public boolean isToggleable() {
        return true;
    }

    public static void registerColumns() {
        PlayersColumn players = new PlayersColumn();
        COLUMNS.add(players);
        PLAYERS = players;
        DistanceColumn distance = new DistanceColumn();
        COLUMNS.add(distance);
        DISTANCE = distance;
        HpColumn hp = new HpColumn();
        COLUMNS.add(hp);
        HP = hp;
        GroupColumn group = new GroupColumn();
        COLUMNS.add(group);
        GROUP = group;
        KdColumn kd = new KdColumn();
        COLUMNS.add(kd);
        KD = kd;
        KillsColumn kills = new KillsColumn();
        COLUMNS.add(kills);
        KILLS = kills;
        WsColumn ws = new WsColumn();
        COLUMNS.add(ws);
        WS = ws;
        GoldColumn gold = new GoldColumn();
        COLUMNS.add(gold);
        GOLD = gold;
        BountyColumn bounty = new BountyColumn();
        COLUMNS.add(bounty);
        BOUNTY = bounty;
        EnchantsColumn enchants = new EnchantsColumn();
        COLUMNS.add(enchants);
        ENCHANTS = enchants;
        TkdColumn tkd = new TkdColumn();
        COLUMNS.add(tkd);
        TKD = tkd;
        FkdColumn fkd = new FkdColumn();
        COLUMNS.add(fkd);
        FKD = fkd;
        WlColumn wl = new WlColumn();
        COLUMNS.add(wl);
        WL = wl;
        PingColumn ping = new PingColumn();
        COLUMNS.add(ping);
        PING = ping;
        OnlineTimeColumn onlineTime = new OnlineTimeColumn();
        COLUMNS.add(onlineTime);
        ONLINE_TIME = onlineTime;
        SeenColumn seen = new SeenColumn();
        COLUMNS.add(seen);
        SEEN = seen;
        TagsColumn tags = new TagsColumn();
        COLUMNS.add(tags);
        TAGS = tags;
        ClientColumn client = new ClientColumn();
        COLUMNS.add(client);
        CLIENT = client;
    }
}
