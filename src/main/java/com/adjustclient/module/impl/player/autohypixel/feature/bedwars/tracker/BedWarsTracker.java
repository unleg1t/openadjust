package com.adjustclient.module.impl.player.autohypixel.feature.bedwars.tracker;

import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.game.movable.*;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.module.impl.player.autohypixel.AutoHypixelModule;
import com.adjustclient.module.impl.player.autohypixel.feature.AutoHypixelFeature;
import com.adjustclient.module.impl.player.autohypixel.feature.bedwars.tracker.item.*;
import com.adjustclient.module.impl.render.hud.playerlist.stats.BedWarsTeam;
import com.adjustclient.setting.*;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.render.font.TextComponent;
import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ChatUtil;
import com.adjustclient.util.anticheat.player.TabPlayer;
import com.adjustclient.util.anticheat.entity.TrackedFireball;
import java.util.*;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.init.Items;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ChatColor;

public final class BedWarsTracker extends AutoHypixelFeature {
    public static BedWarsTracker INSTANCE;
    final BedWarsTrackerHud hud;
    public final BooleanSetting enabled;
    final ModeSetting<NotificationMode> notificationMode;
    final BooleanSetting ping;
    final BooleanSetting armor;
    final BooleanSetting weapons;
    final BooleanSetting tools;
    final BooleanSetting utility;
    final BooleanSetting rotationItems;
    final BooleanSetting sharpenedSwords;
    final BooleanSetting reinforcedArmor;
    final Map<String, TrackedPlayer> players = new HashMap<>();
    final Map<BedWarsTeam, TeamUpgradeState> teams = new HashMap<>();

    public BedWarsTracker(AutoHypixelModule owner, ExpandSetting parent) {
        super(owner);
        INSTANCE = this;
        ExpandSetting group = new ExpandSetting(parent, "Bed Wars Tracker");
        enabled = new BooleanSetting(group, "Enabled", true, "Yes", "No");
        group.enabledPredicate = () -> enabled.isEnabled();
        ExpandSetting notifications = new ExpandSetting(group, "Notifications");
        notificationMode = new ModeSetting<>(notifications, "Mode", NotificationMode.NOTIFICATION, NotificationMode.values());
        ping = new BooleanSetting(notifications, "Ping", false);
        armor = new BooleanSetting(notifications, "Armor", true);
        weapons = new BooleanSetting(notifications, "Weapons", true);
        tools = new BooleanSetting(notifications, "Tools", true);
        utility = new BooleanSetting(notifications, "Utility", true);
        rotationItems = new BooleanSetting(notifications, "Rotation Items", true);
        ExpandSetting upgrades = new ExpandSetting(notifications, "Team Upgrades");
        sharpenedSwords = new BooleanSetting(upgrades, "Sharpened Swords", true);
        reinforcedArmor = new BooleanSetting(upgrades, "Reinforced Armor", true);
        Movable movable = Adjust.movableManager.registerMovable("bedWarsTracker", new com.adjustclient.util.Vec2f(2.0F, 400.0F));
        BooleanSetting render = new BooleanSetting(group, "Render", false);
        hud = new BedWarsTrackerHud(this, movable, render);
        movable.canDrag = hud::isVisible;
    }

    @Override public boolean isFeatureEnabled() { return enabled.isEnabled(); }

    public static boolean inGame() {
        var state = Adjust.gameStateTracker.state;
        return state.game == HypixelGame.BED_WARS && state.startTimer != null && !state.ended;
    }

    public TrackedPlayer getPlayer(String name) { return players.get(name.toLowerCase()); }

    @EventHandler public void onTick(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isActive() || !inGame()) return;
        refreshRoster();
        players.values().forEach(player -> {
            if (player.fireball != null && player.fireball.timer.hasElapsed(120000L)) player.fireball = null;
            List<ItemStack> items = new ArrayList<>();
            items.add(player.sword.stack);
            items.add(player.bow);
            items.add(player.shears);
            if (player.pickaxe != null) items.add(player.pickaxe.stack);
            if (player.axe != null) items.add(player.axe.stack);
            items.add(player.armor.stack);
            player.renderItems = items.toArray(new ItemStack[0]);
        });
        hud.rebuild();
    }

    @EventHandler public void onRender(Render2DEvent event) { if (isActive() && hud.render.isEnabled()) hud.draw(event); }

    @EventHandler public void onProjectileSpawn(ProjectileShooterEvent event) {
        if (!isActive()) return;
        Entity shooter = event.shooter;
        String key = shooter.getName().toLowerCase();
        if (!players.containsKey(key) || shooter.getTeam() != null) return;
        TrackedPlayer player = Objects.requireNonNull(players.get(key));
        if (event.projectile instanceof TrackedFireball) {
            player.fireball = null;
            player.recentItems[0] = null;
        }
    }

    @EventHandler public void onWorldChange(WorldResetEvent event) {
        players.clear();
        teams.clear();
        hud.rows.clear();
    }

    void refreshRoster() {
        BedWarsGameState state = (BedWarsGameState) Adjust.gameStateTracker.state;
        if (state.playerTeams == null) return;
        for (Map.Entry<String, BedWarsTeam> entry : state.playerTeams.entrySet()) {
            if (players.containsKey(entry.getKey())) continue;
            TabPlayer tab = null;
            for (TabPlayer candidate : com.adjustclient.util.anticheat.ServerTracker.INSTANCE.playerTracker.players.values()) {
                if (candidate.info.getGameProfile().getName().equalsIgnoreCase(entry.getKey())) {
                    tab = candidate;
                    break;
                }
            }
            if (tab == null) return;
            BedWarsTeam team = entry.getValue();
            if (!teams.containsKey(team)) teams.put(team, new TeamUpgradeState());
            TrackedPlayer tracked = null;
            for (EntityPlayer entity : mc.theWorld.playerEntities) {
                if (!entity.getGameProfile().getName().equalsIgnoreCase(entry.getKey())) continue;
                tracked = new TrackedPlayer(tab, team);
                observeHeldItem(tracked, entity.getHeldItem());
                ItemStack boots = entity.inventory.armorInventory[1];
                if (boots != null && boots.getItem() instanceof ItemArmor)
                    observeArmor(tracked, boots, (ItemArmor) boots.getItem());
                break;
            }
            if (tracked == null) tracked = new TrackedPlayer(tab, team);
            players.put(entry.getKey(), tracked);
        }
    }

    @EventHandler public void onPlayerKill(PlayerKillEvent event) {
        if (!isActive()) return;
        String key = event.victim;
        if (event.finalKill) { players.remove(key); return; }
        if (!players.containsKey(key)) return;
        TrackedPlayer player = players.get(key);
        ItemStack sword = new ItemStack(Items.wooden_sword);
        sword.setTagCompound(player.sword.stack.getTagCompound());
        player.sword = new TrackedEquipment<>(ToolTier.WOOD, sword);
        if (player.pickaxe != null && player.pickaxe.tier != ToolTier.WOOD) {
            ToolTier tier = ToolTier.values()[player.pickaxe.tier.ordinal() - 1];
            Item item = switch (tier) {
                case WOOD -> Items.wooden_pickaxe;
                case STONE -> Items.stone_pickaxe;
                case IRON -> Items.iron_pickaxe;
                case GOLD -> Items.golden_pickaxe;
                case DIAMOND -> Items.diamond_pickaxe;
            };
            ItemStack stack = new ItemStack(item);
            stack.setTagCompound(player.pickaxe.stack.getTagCompound());
            player.pickaxe = new TrackedEquipment<>(tier, stack);
        }
        if (player.axe != null && player.axe.tier != ToolTier.WOOD) {
            ToolTier tier = ToolTier.values()[player.axe.tier.ordinal() - 1];
            Item item = switch (tier) {
                case WOOD -> Items.wooden_axe;
                case STONE -> Items.stone_axe;
                case IRON -> Items.iron_axe;
                case GOLD -> Items.golden_axe;
                case DIAMOND -> Items.diamond_axe;
            };
            ItemStack stack = new ItemStack(item);
            stack.setTagCompound(player.axe.stack.getTagCompound());
            player.axe = new TrackedEquipment<>(tier, stack);
        }
        player.bow = null;
        player.fireball = null;
        player.recentItems[0] = null;
    }

    @EventHandler public void onPacket(com.adjustclient.event.PacketAppliedEvent event) {
        if (!isActive() || !(event.packet instanceof S04PacketEntityEquipment)) return;
        S04PacketEntityEquipment packet = (S04PacketEntityEquipment) event.packet;
        if (packet.getEquipmentSlot() != 0 && packet.getEquipmentSlot() != 2) return;
        Entity entity = mc.theWorld.getEntityByID(packet.getEntityID());
        if (!(entity instanceof EntityPlayer)) return;
        String key = ((EntityPlayer) entity).getGameProfile().getName().toLowerCase();
        if (!players.containsKey(key)) return;
        TrackedPlayer tracked = players.get(key);
        ItemStack stack = packet.getItemStack();
        if (packet.getEquipmentSlot() == 0) observeHeldItem(tracked, stack);
        else if (stack != null && stack.getItem() instanceof ItemArmor)
            observeArmor(tracked, stack, (ItemArmor) stack.getItem());
    }

    void notifyHeldItem(TrackedPlayer player, BedWarsItem item) {
        if ((item.category == BedWarsItem.Category.ROTATION && rotationItems.isEnabled())
                || (item.category == BedWarsItem.Category.UTILITY && utility.isEnabled()))
            notifyPlayer(player, " is holding ", item);
    }

    void observeHeldItem(TrackedPlayer player, ItemStack stack) {
        if (stack == null) return;
        if (player.fireball != null) player.fireball.superseded = true;
        BedWarsItem trackedItem = BedWarsItem.fromStack(stack);
        boolean equipment = trackedItem != null && trackedItem.category == BedWarsItem.Category.GEAR;
        if (trackedItem != null && trackedItem != BedWarsItem.FIREBALL) notifyHeldItem(player, trackedItem);
        Item item = stack.getItem();
        if (item instanceof ItemSword) {
            ToolTier tier = ToolTier.fromMaterial(((ItemSword) item).getToolMaterial());
            if (equipment && player.sword.tier.ordinal() < tier.ordinal() && weapons.isEnabled())
                notifyPlayer(player, " has " + (tier == ToolTier.IRON ? "an " : "a "), trackedItem);
            if (stack.isItemEnchanted() && teams.containsKey(player.team)) {
                TeamUpgradeState upgrades = teams.get(player.team);
                if (!upgrades.sharpenedSwords) {
                    if (sharpenedSwords.isEnabled()) notifyTeam(player.team, BedWarsItem.SHARPENED_SWORDS);
                    upgrades.sharpenedSwords = true;
                }
            }
            player.sword = new TrackedEquipment<>(tier, stack);
        } else if (item instanceof ItemPickaxe) {
            ToolTier tier = ToolTier.fromMaterial(((ItemPickaxe) item).getToolMaterial());
            if (equipment && (player.pickaxe == null || player.pickaxe.tier.ordinal() < tier.ordinal()) && tools.isEnabled())
                notifyPlayer(player, " has " + (tier == ToolTier.IRON ? "an " : "a "), trackedItem);
            player.pickaxe = new TrackedEquipment<>(tier, stack);
        } else if (item instanceof ItemAxe) {
            ToolTier tier = ToolTier.fromMaterial(((ItemAxe) item).getToolMaterial());
            if (equipment && (player.axe == null || player.axe.tier.ordinal() < tier.ordinal()) && tools.isEnabled())
                notifyPlayer(player, " has " + (tier == ToolTier.IRON ? "an " : "a "), trackedItem);
            player.axe = new TrackedEquipment<>(tier, stack);
        } else if (item instanceof ItemShears) {
            if (equipment && player.shears == null && tools.isEnabled()) notifyPlayer(player, " has ", trackedItem);
            player.shears = stack;
        } else if (item instanceof ItemBow) {
            if (equipment && (player.bow == null || BedWarsItem.fromStack(player.bow) != trackedItem) && weapons.isEnabled())
                notifyPlayer(player, " has a ", trackedItem);
            player.bow = stack;
        } else if (item instanceof ItemFireball) {
            if ((player.fireball == null || player.fireball.timer.hasElapsed(5001L)) && utility.isEnabled())
                notifyPlayer(player, " is holding a ", BedWarsItem.FIREBALL);
            player.rememberFireball(stack);
        }
    }

    void notifyTeam(BedWarsTeam team, BedWarsItem item) {
        if (team == null) return;
        notify(team, team.name + " Team", " has ", item);
    }

    void notifyPlayer(TrackedPlayer player, String verb, BedWarsItem item) {
        notify(player.team, player.player.info.getGameProfile().getName(), verb, item);
    }

    void observeArmor(TrackedPlayer player, ItemStack stack, ItemArmor item) {
        ArmorTier tier = ArmorTier.fromMaterial(item.getArmorMaterial());
        BedWarsItem observed = BedWarsItem.fromStack(stack);
        if (observed != null && observed.category == BedWarsItem.Category.GEAR
                && player.armor.tier != tier && armor.isEnabled()) notifyPlayer(player, " has ", observed);
        if (stack.isItemEnchanted() && teams.containsKey(player.team)) {
            TeamUpgradeState upgrades = teams.get(player.team);
            if (!upgrades.reinforcedArmor) {
                if (reinforcedArmor.isEnabled()) notifyTeam(player.team, BedWarsItem.REINFORCED_ARMOR);
                upgrades.reinforcedArmor = true;
            }
        }
        player.armor = new TrackedEquipment<>(tier, stack);
    }

    void notify(BedWarsTeam team, String subject, String verb, BedWarsItem item) {
        BedWarsGameState state = (BedWarsGameState) Adjust.gameStateTracker.state;
        if (state.playerTeams == null) return;
        BedWarsTeam self = state.playerTeams.get(mc.getSession().getUsername().toLowerCase());
        if (self == null || team == null || self == team) return;
        if (ping.isEnabled()) mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("note.pling")));
        if (notificationMode.getValue() == NotificationMode.CHAT) {
            ChatMessage message = new ChatMessage();
            message.append("Bed Wars ");
            message.color = team.chatColor;
            message.append(subject);
            message.color = ChatColor.GRAY;
            message.append(verb);
            message.color = item.color;
            message.append(item.displayName);
            message.color = ChatColor.GRAY;
            message.append(".");
            ChatUtil.addChatMessage(message.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
        } else {
            List<TextComponent> parts = new ArrayList<>();
            parts.add(new TextComponent(subject, mc.fontRendererObj.getColorCode(team.chatColor.getFormattingCode()) | 0xFF000000));
            parts.add(new TextComponent(verb, 0xFFBEBCBD));
            parts.add(new TextComponent(item.displayName, mc.fontRendererObj.getColorCode(item.color.getFormattingCode()) | 0xFF000000));
            parts.add(new TextComponent(".", 0xFFBEBCBD));
            NotificationBuilder builder = new NotificationBuilder();
            builder.title = "Bed Wars Tracker";
            builder.parts = parts;
            builder.key = subject + "." + verb + "." + item.displayName;
            builder.lifetimeSteps = 140;
            com.adjustclient.module.impl.render.hud.HUDModule.INSTANCE_ALT.notifications.add(builder.build());
        }
    }

    enum NotificationMode implements ModeOption {
        NOTIFICATION("Notification"), CHAT("Chat");
        private final String name;
        NotificationMode(String name) { this.name = name; }
        @Override public String getName() { return name; }
    }
}
