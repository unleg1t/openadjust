package com.adjustclient.utilities.hypixel.game.zombies;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.HypixelGameStartedEvent;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.hypixel.HypixelGameState;
import com.adjustclient.utilities.hypixel.game.LocrawResponse;
import com.adjustclient.utilities.hypixel.game.TeamGame;
import com.adjustclient.utilities.hypixel.game.zombies.entity.*;
import com.adjustclient.utilities.hypixel.game.zombies.entity.prediction.*;
import com.adjustclient.utilities.hypixel.game.zombies.round.*;
import com.adjustclient.utilities.Timer;
import it.unimi.dsi.fastutil.ints.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.ChatColor;
import com.adjustclient.utilities.anticheat.ServerTracker;

public final class ZombiesGameState extends HypixelGameState implements TeamGame {
    public final Int2ObjectOpenHashMap<TrackedEntityRef> entities = new Int2ObjectOpenHashMap<>();
    public ZombiesDifficulty map;
    public ZombiesMap difficulty = ZombiesMap.NORMAL;
    public final Timer roundTimer = new Timer(0);
    public EntityArmorStand luckyChest;
    public int[] instaKillRounds, maxAmmoRounds, doubleGoldRounds;
    public final IntOpenHashSet seenEntities = new IntOpenHashSet();
    public int round;
    public Object field84;
    public static final int[][] INSTA_KILL_ROUNDS;
    public static final int[][] MAX_AMMO_ROUNDS;
    public static final int[][] DOUBLE_GOLD_ROUNDS;
    static {
        INSTA_KILL_ROUNDS = new int[][]{{2,5,8,11,14,17,20,23},{3,6,9,12,15,18,21,24}};
        MAX_AMMO_ROUNDS = new int[][]{{2,5,8,12,16,21,26,31,36,41,46,51,61,66,71,76,81,86,91,96},
            {3,6,9,13,17,22,27,32,37,42,47,52,62,67,72,77,82,87,92,97,102}};
        DOUBLE_GOLD_ROUNDS = new int[][]{{5,15,45,55,65,75,85,95,105},
            {6,16,26,36,46,66,76,86,96},{7,17,27,37,47,67,77,87,97}};
    }
    public ZombiesGameState() { super(HypixelGame.ZOMBIES); }
    public List<PredictedTarget> getPowerUps() {
        return entities.values().stream()
            .map(e -> e instanceof PredictedTarget p ? p : null)
            .filter(value -> value != null)
            .toList();
    }
    public List<ZombiesEntity> getTargets() {
        return entities.values().stream()
            .map(e -> e instanceof ZombiesEntity p ? p : null)
            .filter(Objects::nonNull)
            .toList();
    }
    public ZombiesRoundTiming[] getRoundTimings() {
        if (map == null) return null;
        return switch (map) {
            case DEAD_END -> ZombiesRoundData.TABLE_0.get(round);
            case BAD_BLOOD -> ZombiesRoundData.TABLE_1.get(round);
            case ALIEN_ARCADIUM -> ZombiesRoundData.TABLE_2.get(round);
            case PRISON -> ZombiesRoundData.TABLE_3.get(round);
        };
    }
    public static void onSpecialBoss() {
    }
    public void onPowerUp(ZombiesPowerUp powerUp) {
        int[][] schedules = switch (powerUp) {
            case INSTA_KILL -> INSTA_KILL_ROUNDS;
            case MAX_AMMO -> MAX_AMMO_ROUNDS;
            case SHOPPING_SPREE -> DOUBLE_GOLD_ROUNDS;
            default -> null;
        };
        if (schedules != null) for (int[] schedule : schedules) {
            if (Arrays.stream(schedule).anyMatch(value -> value == round)) {
                switch (powerUp) {
                    case INSTA_KILL -> instaKillRounds = schedule;
                    case MAX_AMMO -> maxAmmoRounds = schedule;
                    case SHOPPING_SPREE -> doubleGoldRounds = schedule;
                    default -> { }
                }
                break;
            }
        }
    }
    public void acceptLocation(LocrawResponse response) {
        if (response.map == null) return;
        for (ZombiesDifficulty value : ZombiesDifficulty.values())
            if (((String)value.field16).equalsIgnoreCase(response.map)) { map = value; break; }
    }
    public boolean isLuckyChest(Entity entity) {
        return entity.hasCustomName() && ChatColor.getTextWithoutFormattingCodes(entity.getCustomNameTag()).contains("Lucky Chest");
    }
    public void onScoreboardLine(String team, String line) {
        if (Pattern.matches("§c§l.+ \\d{1,3}", line)) {
            try {
                String[] words = line.split("\\s+");
                int next = Integer.parseInt(words[words.length - 1].trim());
                if (next != round) { round = next; roundTimer.reset(); }
                if (startTimer == null) {
                    startTimer = new Timer();
                    super.onStarted();
                }
            } catch (NumberFormatException ignored) { }
        }
        if ("team_5".equals(team) && line.contains(": ") && !line.contains("/")) {
            if (line.contains("§c")) difficulty = ZombiesMap.HARD;
            else if (line.contains("§4")) difficulty = ZombiesMap.RIP;
            else if (line.contains("§a")) difficulty = ZombiesMap.NORMAL;
        }
    }
    @Override public void onPacket(Packet<?> packet, boolean before) {
        super.onPacket(packet, before);
        if (before) return;
        if (packet instanceof S3EPacketTeams teams) {
            if (teams.getAction() == 0 || teams.getAction() == 2)
                onScoreboardLine(teams.getName(), (String.valueOf(teams.getPrefix()) + String.valueOf(teams.getSuffix())).trim());
        } else if (packet instanceof S0EPacketSpawnObject spawn) {
            if (spawn.getType() == 2) entities.put(spawn.getEntityID(), new ZombiesEntityTracker(this, spawn.getEntityID()));
        } else if (packet instanceof S0FPacketSpawnMob spawn) {
            switch (spawn.getEntityType()) {
                case 27,47,50,51,53,54,55,56,57,60,61,62,66,67,95,99 ->
                    entities.put(spawn.getEntityID(), new PredictedTarget(this, spawn.getEntityID(), spawn.getEntityType()));
                default -> { }
            }
        } else if (packet instanceof S13PacketDestroyEntities destroy) {
            for (int id : destroy.getEntityIDs()) entities.remove(id);
        } else if (packet instanceof S14PacketEntity move) {
            Entity entity = move.getEntity(mc.theWorld);
            if (entity != null && entities.get(entity.getEntityId()) instanceof PredictedTarget target) target.predictor.onRelativeMove(move);
        } else if (packet instanceof S12PacketEntityVelocity velocity) {
            if (entities.get(velocity.getEntityID()) instanceof PredictedTarget target) target.predictor.onVelocity(velocity);
        } else if (packet instanceof S18PacketEntityTeleport teleport) {
            if (entities.get(teleport.getEntityId()) instanceof PredictedTarget target) target.predictor.onTeleport(teleport);
        } else if (packet instanceof S19PacketEntityHeadLook head) {
            Entity entity = head.getEntity(mc.theWorld);
            if (entity != null && entities.get(entity.getEntityId()) instanceof PredictedTarget target)
                target.predictor.headYaw = head.getYaw() * 360.0F / 256.0F;
        }
    }
    public boolean isNewEntity(int id) { return seenEntities.add(id); }
    @Override public ZombiesGameState locate() {
        CompletableFuture<LocrawResponse> future;
        if (locraw != null) future = CompletableFuture.completedFuture(locraw);
        else if (locrawFuture != null) future = locrawFuture;
        else {
            if (ServerTracker.INSTANCE.serverState == null
                    || ServerTracker.INSTANCE.serverState.antiCheatModel == null
                    || ServerTracker.INSTANCE.serverState.antiCheatModel.serverType != com.adjustclient.utilities.server.ServerType.HYPIXEL)
                throw new IllegalStateException("Unable to locate because we are not on Hypixel!");
            Adjust.chatMessageSender.sendMessage("/locraw", false);
            future = locrawFuture = new CompletableFuture<>();
        }
        future.thenAccept(response -> {
            acceptLocation(response);
        });
        if ((game.flags & 0x40) != 0) super.locate(false);
        return this;
    }
    public void update() {
        if (mc.theWorld == null) return;
        if (spectator) super.onGameStart();
        try {
            var iterator = entities.int2ObjectEntrySet().iterator();
            while (iterator.hasNext()) {
                TrackedEntityRef entity = iterator.next().getValue();
                if (entity.isExpired()) iterator.remove();
                else entity.update();
            }
        } catch (ConcurrentModificationException | NoSuchElementException | IndexOutOfBoundsException ignored) { }
        luckyChest = null;
        for (Entity entity : mc.theWorld.loadedEntityList)
            if (entity instanceof EntityArmorStand armorStand && isLuckyChest(armorStand)) { luckyChest = armorStand; break; }
    }

    @SuppressWarnings("unused")
    private void vendorOnScoreboardLineTelemetry() {
        if (Adjust.telemetryListener != null) {
            Adjust.telemetryListener.onGameStart(new HypixelGameStartedEvent(this));
        }
    }
}
