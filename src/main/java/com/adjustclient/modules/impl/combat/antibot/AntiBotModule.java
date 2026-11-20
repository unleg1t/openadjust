package com.adjustclient.modules.impl.combat.antibot;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.DirectionalPacketEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.utilities.hypixel.game.hideandseek.HideAndSeekGameState;
import com.adjustclient.utilities.hypixel.game.hideandseek.HideAndSeekMode;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.managers.servertracker.FriendServerTracker;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.model.WatchdogModel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.scoreboard.ScorePlayerTeam;

public final class AntiBotModule extends Module implements MinecraftInstance {
    public static AntiBotModule INSTANCE;
    public static final Map<Integer, BotMovementTracker> TRACKERS =
            Collections.synchronizedMap(new HashMap<>());
    private static final List<String> LOBBY_PREFIXES = List.of("§a", "§b", "§7", "§6");
    boolean cleanupPending;

    public AntiBotModule() {
        super("Anti Bot", Category.COMBAT);
        INSTANCE = this;
    }

    @Override public void onDisable() {
        TRACKERS.clear();
    }

    @EventHandler
    public void onServerConnect(com.adjustclient.core.events.ServerConnectionAttemptEvent event) {
        cleanupPending = true;
    }

    @EventHandler
    public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isEnabled() || mc.theWorld == null || mc.thePlayer == null || event.outgoing) return;
        if (cleanupPending) {
            Iterator<Map.Entry<Integer, BotMovementTracker>> iterator = TRACKERS.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, BotMovementTracker> entry = iterator.next();
                if (mc.theWorld.getEntityByID(entry.getKey()) != null) {
                    BotMovementTracker tracker = entry.getValue();
                    if (tracker.age < 32762) tracker.age++;
                }
            }
            cleanupPending = false;
        }
        Packet<?> packet = event.packet;
        if (packet instanceof S01PacketJoinGame) {
            TRACKERS.clear();
            return;
        }
        if (packet instanceof S14PacketEntity) {
            S14PacketEntity movement = (S14PacketEntity) packet;
            Entity entity = movement.getEntity(mc.theWorld);
            if (entity == null || entity.isUser()) return;
            BotMovementTracker tracker = TRACKERS.get(entity.getEntityId());
            if (tracker == null) {
                if (!(entity instanceof EntityPlayer) || entity.isUser()) return;
                tracker = new BotMovementTracker(entity);
                TRACKERS.put(tracker.entityId, tracker);
                if (!entity.getDataWatcher().getAllWatched().isEmpty()) tracker.metadataSeen = true;
            }
            if (tracker.verdict != 0) return;
            if (!(packet instanceof S14PacketEntity.S17PacketEntityLookMove)) tracker.nonLookMoveSeen = true;
            int weight = 1;
            if (packet instanceof S14PacketEntity.S15PacketEntityRelMove
                    && movement.func_149062_c() == 0 && movement.func_149061_d() == 0
                    && movement.func_149064_e() == 0 && elapsed(tracker.movementTimer, 1000L)) {
                tracker.emptyMoveStreak++;
                weight = 4;
            } else {
                tracker.emptyMoveStreak = 0;
            }
            if (elapsed(tracker.movementTimer, 40L)) {
                tracker.movementTimer.reset();
                tracker.teleports -= weight;
                tracker.relativeMoves += weight;
            }
            tracker.updatePosition(entity);
            return;
        }
        if (packet instanceof S18PacketEntityTeleport) {
            int id = ((S18PacketEntityTeleport) packet).getEntityId();
            Entity entity = mc.theWorld.getEntityByID(id);
            if (entity == null || entity.isUser()) return;
            // Negative IDs from the special server are accepted even for non-player entities.
            if (!(id < 0 && serverType() == ServerType.HYPIXEL) && !(entity instanceof EntityPlayer)) return;
            BotMovementTracker tracker = TRACKERS.get(id);
            if (tracker == null) {
                tracker = new BotMovementTracker(entity);
                TRACKERS.put(tracker.entityId, tracker);
            }
            if (tracker.verdict == 0) {
                if (elapsed(tracker.teleportTimer, 40L)) {
                    tracker.teleports++;
                    tracker.teleportTimer.reset();
                }
                tracker.updatePosition(entity);
            }
            return;
        }
        int id;
        boolean strongEvidence = false;
        if (packet instanceof S1CPacketEntityMetadata) {
            id = ((S1CPacketEntityMetadata) packet).getEntityId();
        } else if (packet instanceof S1DPacketEntityEffect) {
            id = ((S1DPacketEntityEffect) packet).getEntityId();
            strongEvidence = true;
        } else if (packet instanceof S1EPacketRemoveEntityEffect) {
            id = ((S1EPacketRemoveEntityEffect) packet).getEntityId();
            strongEvidence = true;
        } else if (packet instanceof S19PacketEntityStatus) {
            Entity statusEntity = ((S19PacketEntityStatus) packet).getEntity(mc.theWorld);
            if (statusEntity == null) return;
            id = statusEntity.getEntityId();
            strongEvidence = true;
        } else if (packet instanceof S0BPacketAnimation) {
            id = ((S0BPacketAnimation) packet).getEntityID();
        } else {
            return;
        }
        BotMovementTracker tracker = TRACKERS.get(id);
        Entity entity = mc.theWorld.getEntityByID(id);
        if (tracker == null || entity == null) return;
        if (!(packet instanceof S0BPacketAnimation) || entity.ticksExisted >= 25) tracker.metadataSeen = true;
        if (strongEvidence) tracker.nonLookMoveSeen = true;
    }

    private static boolean elapsed(com.adjustclient.utilities.Timer timer, long delay) {
        return timer.lastMs + delay <= System.currentTimeMillis();
    }

    private static ServerType serverType() {
        var blocker = ServerTracker.INSTANCE.serverState;
        return blocker == null || blocker.antiCheatModel == null ? null
                : blocker.antiCheatModel.serverType;
    }

    static boolean isPitPrefix(String prefix) {
        if (!prefix.endsWith(" ")) return false;
        if (prefix.equals("§6§lHIGH§a ")) return true;
        int formattingCodes = 0;
        for (char character : prefix.toCharArray()) {
            if (character == '§' && ++formattingCodes >= 4) return prefix.matches("§[a-zA-Z0-9](.*)");
        }
        return false;
    }

    static boolean isSpecialEntity(Entity entity) {
        if (!(entity instanceof EntityLivingBase)) {
            if (entity.getClass() != EntityItem.class) return false;
            ItemStack stack = ((EntityItem) entity).getEntityItem();
            if (stack.getItem() instanceof ItemSkull && serverType() == ServerType.HYPIXEL) {
                if (stack.getTagCompound() != null
                        && stack.getTagCompound().getCompoundTag("SkullOwner").hasKey("hypixelPopulated")) return true;
                if (stack.getDisplayName().equals("§aEgg")) return true;
            }
            return hasUuidName(stack.getDisplayName());
        }
        if (entity.getClass() == EntityBat.class) return true;
        var game = Adjust.gameStateTracker.state;
        HypixelGame type = game.game;
        if (entity.getClass() == EntityGiantZombie.class) {
            if (type == HypixelGame.BED_WARS || type == HypixelGame.SKYWARS
                    || (type.flags & 2) != 0) return true;
            ItemStack held = ((EntityLivingBase) entity).getHeldItem();
            if (held != null && hasUuidName(held.getDisplayName())) return true;
        } else if (entity.getClass() == EntityRabbit.class && type == HypixelGame.THE_HYPIXEL_PIT) {
            return entity.getName().contains("§l");
        }
        if (entity.getClass() != EntityGiantZombie.class && entity.getClass() != EntityRabbit.class
                && game.lobby != null && game.lobby.booleanValue()
                && !(entity instanceof EntityPlayer)) return true;
        if (type != HypixelGame.BED_WARS) return false;
        if (entity.getClass() != EntityIronGolem.class && entity.getClass() != EntitySilverfish.class
                && !(entity instanceof EntityPlayer)) return true;
        return ((EntityLivingBase) entity).getHealth() == 1.0F;
    }

    static boolean checkTeam(Entity entity, BotMovementTracker tracker) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            var game = Adjust.gameStateTracker.state;
            if (serverType() == ServerType.HYPIXEL && (game.game.flags & 2) != 0) {
                if (player.getGameProfile().getName().endsWith("§r")) return true;
            } else if (player.worldObj != null) {
                ScorePlayerTeam team = player.worldObj.getScoreboard().getPlayersTeam(player.getGameProfile().getName());
                if (team != null && checkTeam(team, tracker, player.getGameProfile().getName())) return true;
            }
        }
        tracker.field46--;
        return false;
    }

    static boolean checkTeam(ScorePlayerTeam team, BotMovementTracker tracker, String playerName) {
        ServerType server = serverType();
        String name = team.getTeamName();
        String prefix = team.getColorPrefix();
        if (server != ServerType.HYPIXEL) {
            if (server == ServerType.BLOCKSMC && name.equals("Z_NPC")) tracker.verdict = 2;
            if (server == ServerType.MINEPLEX && name.equals("§HiddenNPC")) tracker.verdict = 2;
            return false;
        }
        var game = Adjust.gameStateTracker.state;
        if (game.game == HypixelGame.HOUSING
                || game.lobby != null && game.lobby.booleanValue()) {
            if (prefix.startsWith("§")) {
                if (FriendServerTracker.isNpcName(team)) {
                    tracker.verdict = 2;
                    return false;
                }
                return prefix.equals("§7") || prefix.equals("§a[VIP] ") || prefix.equals("§a[VIP§6+§a] ")
                        || prefix.startsWith("§b[MVP") || prefix.startsWith("§6[MVP") || prefix.contains("YOUTUBE")
                        || prefix.equals("§2[GM] ") || prefix.equals("§c[ADMIN] ") || prefix.equals("§c[§6ዞ§c] ");
            }
        }
        if (game.lobby == null || game.lobby.booleanValue()
                || game.game == null) return false;
        switch (game.game) {
            case THE_HYPIXEL_PIT:
                return isPitPrefix(prefix) && name.equals(team.getRegisteredName());
            case BED_WARS:
                if (game.startTimer != null) {
                    return team.getColorSuffix().isEmpty() && team.getRegisteredName().equals(name)
                            && prefix.contains("§") && name.matches("^[A-Z][a-zA-Z]{0,7}\\d{1,3}$");
                }
                return LOBBY_PREFIXES.contains(prefix) && team.getColorSuffix().isEmpty()
                        && team.getRegisteredName().equals(name) && name.matches("^\\d{3}$");
            case MURDER_MYSTERY:
                return name.equals(playerName);
            case SKYWARS:
                // pattern: ^\d{3}pre§[a-zA-Z0-9]$
                return game.startTimer == null && name.matches("^\\d{3}pre§[a-zA-Z0-9]$");
            case DUELS:
                return game.startTimer != null
                        ? name.startsWith("w_") && name.contains("_team_") : name.equals("§7§k");
            default:
                return false;
        }
    }

    public static boolean isBot(int entityId) {
        if (!INSTANCE.isEnabled() || mc.theWorld == null) return false;
        var blocker = ServerTracker.INSTANCE.serverState;
        var server = blocker == null ? null : blocker.antiCheatModel;
        if (server != null && !server.allowsCapabilityD()) return false;
        Entity entity = mc.theWorld.getEntityByID(entityId);
        if (entity != null && entity.isUser()) return false;
        BotMovementTracker tracker = TRACKERS.get(entityId);
        boolean player = entity instanceof EntityPlayer;
        if (tracker == null) {
            if (entity == null) return false;
            if (server instanceof WatchdogModel && isSpecialEntity(entity)) {
                tracker = new BotMovementTracker(entity);
                tracker.verdict = 3;
                TRACKERS.put(tracker.entityId, tracker);
                return true;
            }
            if (!player) return false;
            tracker = new BotMovementTracker(entity);
            TRACKERS.put(tracker.entityId, tracker);
        }
        if (tracker.age < 101 && tracker.field46 != 1 && tracker.field46 >= -14 && entity != null
                && checkTeam(entity, tracker)) {
            tracker.field46 = 1;
            tracker.metadataSeen = true;
            tracker.nonLookMoveSeen = true;
            tracker.relativeMoves += 7;
            tracker.teleports = -7;
        }
        if (server instanceof WatchdogModel) {
            var game = Adjust.gameStateTracker.state;
            if (game instanceof HideAndSeekGameState && game.hasStartedFor(1000L)
                    && ((HideAndSeekGameState) game).mode == HideAndSeekMode.PARTY_POOPER && player) {
                var team = ((EntityPlayer) entity).getTeam();
                if (team != null && team.getRegisteredName().equals("SpecialNPCs")) {
                    tracker.verdict = 4;
                    return true;
                }
            } else if (game instanceof BedWarsGameState && player && ((EntityPlayer) entity).getTeam() == null) {
                return true;
            }
        }
        if (!tracker.metadataSeen || tracker.verdict != 0 || !tracker.nonLookMoveSeen
                || tracker.relativeMoves < 6 || tracker.teleports >= -5) return true;
        if (!player || tracker.squaredMovement >= 1.0 || tracker.age < 41 || tracker.teleports < -39) return false;
        return Adjust.gameStateTracker.state.game == HypixelGame.BED_WARS
                && ((EntityPlayer) entity).getTeam() == null;
    }

    static boolean hasUuidName(String name) {
        if (name.length() < 32) return false;
        try {
            UUID.fromString(name.substring(2));
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
