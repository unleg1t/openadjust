package com.adjustclient.util.anticheat;

import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.util.*;
import com.adjustclient.util.Timer;
import com.adjustclient.util.rotation.RotationHolder;
import com.adjustclient.util.anticheat.entity.*;
import com.adjustclient.util.anticheat.entity.EntityTracker;
import com.adjustclient.util.anticheat.player.PlayerTracker;
import com.adjustclient.util.anticheat.blocker.*;
import com.adjustclient.util.anticheat.model.*;
import com.adjustclient.module.impl.movement.scaffold.HotbarBlockTracker;
import com.adjustclient.service.credits.CreditsRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.world.WorldSettings;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.*;
import java.util.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import com.adjustclient.module.impl.movement.inventorymove.InventoryMoveModule;
import com.adjustclient.event.ScreenEventPhase;

public final class ServerTracker implements Listener, MinecraftInstance, RotationHolder {
    public static ServerTracker INSTANCE = new ServerTracker();
    public static Adjust clientInstance;
    public EntityTracker entityTracker;
    public PlayerTracker playerTracker;
    public C13PacketPlayerAbilities lastInput;
    public LatestLogWriter logWriter = new LatestLogWriter(null, "Adjust");
    public BlockerState serverState;
    public Object field24;
    public Object field28;
    public IChatComponent header;
    public IChatComponent footer;
    public Object field40;
    public Map<BlockPos, Timer> bedTimers = new LinkedHashMap<>();
    public Map<BlockPos, Set<BlockPos>> bedBlocks = new HashMap<>();
    public List<com.adjustclient.util.PlacementData> placements = new ArrayList<>();
    public com.adjustclient.module.impl.player.inventory.click.SlotClickHighlighter field56 = new com.adjustclient.module.impl.player.inventory.click.SlotClickHighlighter();
    public HotbarBlockTracker hotbarBlockTracker = new HotbarBlockTracker();
    public Vec3 position;
    public Vec3 previousPosition;
    public Vec2f rotation;
    public it.unimi.dsi.fastutil.floats.FloatArrayList rotationChanges = new it.unimi.dsi.fastutil.floats.FloatArrayList();
    public com.adjustclient.util.BoundedLongList field80 = new com.adjustclient.util.BoundedLongList(10);
    public Long lastSwing;
    public Timer velocityTimer = new Timer();
    public Timer interactionTimer = new Timer();
    public Timer placementTimer = new Timer();
    public Timer pearlTimer = new Timer();
    public Timer worldTimer = new Timer();
    public Timer bedTimer = new Timer();
    public Timer packetTimer = new Timer();
    public Timer field116 = new Timer();
    public Timer field120 = new Timer();
    public Timer loopTimer = new Timer();
    public Timer lagTimer = new Timer();
    public Timer swingTimer = new Timer();
    public Timer field136 = new Timer();
    public Vec2f placementRotation;
    public Entity attackedEntity;
    public EntityLivingBase attackedLivingEntity;
    public Object field152;
    public float tps = 20.0F;
    public long lastTimeUpdate;
    public float velocity;
    public float health;
    public float healthChange;
    public int ticks;
    public int ticksSinceVelocity = Integer.MAX_VALUE;
    public int ticksSinceTabUpdate;
    public int ticksSinceAttack = Integer.MAX_VALUE;
    public boolean field196;
    public boolean field197;
    public boolean field198;
    public boolean field199;
    public boolean swung;
    public boolean mouseOverOverridden;
    public boolean usedItemSinceMovement;
    public boolean dugSinceMovement;
    public boolean field204;
    public boolean digging;
    public Object field208;

    @EventHandler public void onBlockPlace(ItemPlacementAttemptEvent event) {
        if (event.stack != null && hotbarBlockTracker != null) hotbarBlockTracker.onBlockPlaced();
    }
    @EventHandler public void onTick(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled()) return;
        updateBeds();
        Adjust.movementPredictor.validator.validateMotion();
        if (entityTracker != null) entityTracker.update();
        if (INSTANCE.serverState != null
                && INSTANCE.serverState.antiCheatModel != null
                && INSTANCE.serverState.antiCheatModel.serverType == com.adjustclient.server.ServerType.HYPIXEL
                && Adjust.gameStateTracker.state.game == com.adjustclient.hypixel.HypixelGame.BED_WARS) {
            if (playerTracker != null) {
                if (Adjust.gameStateTracker.state.startTimer == null) {
                    synchronized (playerTracker.lock) {
                        playerTracker.active = true;
                    }
                } else playerTracker.reconcile();
            }
        }
        if (playerTracker != null) playerTracker.expirePending();
        if (ticksSinceAttack != Integer.MAX_VALUE) ++ticksSinceAttack;
        if (mc.thePlayer == null) return;
        float currentHealth = mc.thePlayer.getDataWatcher().getWatchableObjectFloat(6);
        healthChange = currentHealth - health;
        health = currentHealth;
        placements.removeIf(
                placement -> ((Timer) placement.field4).hasElapsed(60000L));
    }
    @EventHandler public void onChunkUpdate(BlockBreakEvent event) {
        while (bedTimers.size() >= 64) bedTimers.remove(bedTimers.keySet().iterator().next());
        bedTimers.computeIfAbsent(event.pos,
                pos -> new Timer()).reset();
    }
    @EventHandler public void onHypixelGameEvent(com.adjustclient.event.BlockPlacedEvent event) {
        if (Adjust.gameStateTracker.state instanceof com.adjustclient.hypixel.game.bedwars.BedWarsGameState state)
            state.onBlockState(event);
    }
    @EventHandler public void onGameLoop(PreClientTickEvent event) {
        swung = false;
        if (serverState != null) {
            PacketWatcher watcher = serverState.packetWatcher;
            if (++watcher.field40 % 12 == 0) {
                if (watcher.pendingBlock != null && watcher.placementTimer.hasElapsed(3000L)) watcher.pendingBlock = null;
                if (watcher.waitingTab && watcher.tabTimer.hasElapsed(3000L)) watcher.waitingTab = false;
                if (watcher.pendingAction != null && watcher.actionTimer.hasElapsed(3000L)) watcher.pendingAction = null;
            }
            serverState.featureBlocker.flushNotifications();
        }
        if (loopTimer.hasElapsed(500L)) lagTimer.reset();
        loopTimer.reset();
        mouseOverOverridden = false;
    }
    @EventHandler public void onInput(GuiOpenedEvent event) {
        if (event.screen != null) return;
        if (!mc.isCallingFromMinecraftThread()) {
            mc.addScheduledTask(
                    () -> updateKeyBindings(null));
        } else {
            try { updateKeyBindings(null); } catch (Exception ignored) { }
        }
    }
    @EventHandler public void onWorldLoad(ServerJoinCompleteEvent event) {
        mc.getTextureManager().skinTextureCache.clean();
        worldTimer.reset();
        lastInput = null;
    }
    @EventHandler public void onPacketReceive(ServerConnectionAttemptEvent event) {
        var model = AntiCheatDetector.detectModel(event.host, event.port, event.serverIdentity);
        BlockerState state = new BlockerState();
        state.antiCheatModel = model;
        state.field12 = event.host;
        state.field16 = event.address;
        state.field48 = event.port;
        if (model != null) model.setServerData(state);
        serverState = state;
        entityTracker = new EntityTracker();
        playerTracker = new PlayerTracker();
        playerTracker.active = true;
        field24 = event.host;
        field28 = event.serverIdentity;
        for (com.adjustclient.module.Module module : Adjust.INSTANCE.moduleManager.view)
            module.flags &= ~8;
    }
    @EventHandler public void onServerJoin(DisconnectTeardownEvent event) {
        if (event.origin != ScreenEventPhase.USER
                && serverState != null && serverState.antiCheatModel != null)
            serverState.antiCheatModel.onActivate(event);
        serverState = null;
        entityTracker = null;
        mc.timer.timerSpeed = 1.0F;
    }
    @EventHandler public void onVelocity(IncomingVelocityEvent event) {
        if (event.hasVelocity()) field116.reset();
    }
    @EventHandler public void onPacketSend(LowLevelPacketSendEvent event) {
        field136.reset();
        Packet<?> packet = event.packet;
        if (serverState != null) serverState.packetWatcher.onPacket(packet, true, false);
        if (packet instanceof C03PacketPlayer movement) {
            onMotion(movement);
            if (ticksSinceVelocity != Integer.MAX_VALUE) ++ticksSinceVelocity;
            field199 = false;
        } else if (packet instanceof C02PacketUseEntity use) {
            if (entityTracker != null) {
                TrackedEntity<?> tracked = entityTracker.get(use.entityId, TrackedEntity.class);
                if (tracked instanceof TrackedLivingEntity living && use.getAction() == C02PacketUseEntity.Action.ATTACK)
                    living.combatStats.updateAttack();
            }
        } else if (packet instanceof C0APacketAnimation) {
            if (field199 && !digging) com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.WARN, true,
                    "Swung multiple times in one tick, lagging?", new Object[0]);
            long now = System.currentTimeMillis();
            if (lastSwing != null) {
                if (now - lastSwing >= 5000) field80.clear();
                else field80.add(now - lastSwing);
            }
            lastSwing = now;
            field199 = true;
        } else if (packet instanceof C13PacketPlayerAbilities input) {
            if (lastInput == null || input.isAllowFlying()
                    && lastInput.isFlying() != input.isFlying()) field120.reset();
            lastInput = input;
        }
        Adjust.placementVerifier.onPacket(packet, false);
    }
    @EventHandler public void onBedRemoved(BlockRenderStateEvent event) {
        IBlockState state = event.state;
        if (state.getBlock() == net.minecraft.init.Blocks.bed
                && state.getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD) {
            synchronized (bedBlocks) {
                bedBlocks.computeIfAbsent(new BlockPos(event.position),
                        pos -> new HashSet<>());
            }
        }
    }
    public void onMotion(C03PacketPlayer packet) {
        if (serverState != null && serverState.antiCheatModel != null) {
            if (serverState.antiCheatModel.serverType == com.adjustclient.server.ServerType.HYPIXEL
                    && Adjust.gameStateTracker != null
                    && Adjust.gameStateTracker.state instanceof com.adjustclient.hypixel.game.pit.PitGameState pitState) {
                pitState.onPlayerMove(packet);
            } else if (serverState.antiCheatModel.serverType == com.adjustclient.server.ServerType.REWOUND
                    && Adjust.rewoundGameTracker != null) {
                Adjust.rewoundGameTracker.state.onPlayerMove(packet);
            }
        }
        if (packet.isOnGround()) velocity = 0;
        if (packet.isMoving()) {
            previousPosition = position;
            if (position != null && !packet.isOnGround()) {
                double dy = packet.getPositionY() - position.yCoord;
                if (dy >= 0) velocity = 0;
                else velocity -= dy;
            }
            position = new Vec3(packet.getPositionX(), packet.getPositionY(), packet.getPositionZ());
            ++ticksSinceTabUpdate;
        } else ticksSinceTabUpdate = 0;
        if (packet.getRotating()) {
            if (rotation != null) {
                rotationChanges.add(0, Math.abs(MathHelper.wrapAngleTo180_float(rotation.x - packet.getYaw())));
                while (rotationChanges.size() >= 11) rotationChanges.removeFloat(rotationChanges.size() - 1);
            }
            rotation = new Vec2f(packet.getYaw(), packet.getPitch());
        }
        field196 = packet.isOnGround();
        var validator = Adjust.movementPredictor.validator;
        if (packet.isMoving()) {
            if (validator.field8 == null)
                validator.field8 = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            Vec3 before = (Vec3) validator.field8;
            Vec3 after = new Vec3(packet.getPositionX(), packet.getPositionY(), packet.getPositionZ());
            var simulation = new com.adjustclient.util.prediction.MotionSimulator();
            simulation.update(before, after);
            validator.field8 = after;
            var predictions = validator.predictor;
            predictions.history.addFirst(simulation);
            if (predictions.history.size() >= 21) predictions.history.removeLast();
            if (predictions.jumpTicks > 0) --predictions.jumpTicks;
            if (packet.forkField39) return;
            if (!before.equals(after)) ++predictions.historyTicks;
        }
    }
    public Set<BlockPos> onBlockStateUpdate(BlockPos pos) {
        synchronized (bedBlocks) {
            if (mc.theWorld == null || !mc.theWorld.isBlockLoaded(pos, true)) return Collections.emptySet();
            IBlockState state = mc.theWorld.getBlockState(pos);
            if (state.getBlock() != net.minecraft.init.Blocks.bed) return Collections.emptySet();
            BlockPos head = state.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT
                    ? pos.offset(state.getValue(BlockBed.FACING)) : pos;
            BlockPos foot = head.offset(state.getValue(BlockBed.FACING).getOpposite());
            if (!mc.theWorld.isBlockLoaded(head, true) || !mc.theWorld.isBlockLoaded(foot, true)
                    || mc.theWorld.getBlockState(head).getBlock() != net.minecraft.init.Blocks.bed
                    || mc.theWorld.getBlockState(foot).getBlock() != net.minecraft.init.Blocks.bed)
                return Collections.emptySet();
            Set<BlockPos> tracked = bedBlocks.get(head);
            if (tracked == null) return Collections.emptySet();
            Set<BlockPos> result = new HashSet<>();
            for (BlockPos block : tracked) {
                if (mc.theWorld.isBlockLoaded(block, true)
                        && !mc.theWorld.getBlockState(block).getBlock().isFullBlock()) result.add(block);
            }
            return result;
        }
    }
    public void recordPlacement(net.minecraft.entity.player.EntityPlayer player, BlockPos pos, EnumFacing facing, Object item, float x, float y, float z) {
        Vec3 hit = new Vec3((float) pos.getX() + x, (float) pos.getY() + y, (float) pos.getZ() + z);
        PlacementData data = new PlacementData(pos, facing, hit, item, player);
        placements.add(data);
    }
    public boolean hasSwingCooldownElapsed() {
        return swingTimer.hasElapsed(mc.playerController == null || mc.playerController.getCurrentGameType() == WorldSettings.GameType.CREATIVE ? 350L : 90L);
    }
    public void updateBeds() {
        synchronized (bedBlocks) {
            Iterator<Map.Entry<BlockPos, Set<BlockPos>>> entries = bedBlocks.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<BlockPos, Set<BlockPos>> entry = entries.next();
                BlockPos head = entry.getKey();
                if (!mc.theWorld.isBlockLoaded(head, true)) continue;
                IBlockState state = mc.theWorld.getBlockState(head);
                if (state.getBlock() != net.minecraft.init.Blocks.bed
                        || !state.getProperties().containsKey(BlockBed.PART)
                        || state.getValue(BlockBed.PART) != BlockBed.EnumPartType.HEAD) {
                    entries.remove();
                    continue;
                }
                BlockPos foot = head.offset(state.getValue(BlockBed.FACING).getOpposite());
                if (!mc.theWorld.isBlockLoaded(foot, true)) continue;
                if (mc.theWorld.getBlockState(foot).getBlock() != net.minecraft.init.Blocks.bed) {
                    entries.remove();
                    continue;
                }
                for (EnumFacing face : EnumFacing.values()) {
                    BlockPos adjacent = head.offset(face);
                    if (face == EnumFacing.DOWN || adjacent.equals(head) || adjacent.equals(foot)) continue;
                    if (mc.theWorld.isBlockLoaded(adjacent, true)
                            && mc.theWorld.getBlockState(adjacent).getBlock().isFullBlock()) entry.getValue().add(adjacent);
                }
                for (EnumFacing face : EnumFacing.values()) {
                    BlockPos adjacent = foot.offset(face);
                    if (face == EnumFacing.DOWN || adjacent.equals(head) || adjacent.equals(foot)) continue;
                    if (mc.theWorld.isBlockLoaded(adjacent, true)
                            && mc.theWorld.getBlockState(adjacent).getBlock().isFullBlock()) entry.getValue().add(adjacent);
                }
            }
        }
    }
    public void updateKeyBindings(Object ignored) {
        if (mc.currentScreen != null) return;
        for (KeyBinding binding : InventoryMoveModule.MOVEMENT_AND_JUMP_KEYS) {
            int key = binding.getKeyCode();
            if (key == 0 || binding.isKeyDown()) continue;
            boolean pressed = key >= -100 && key < 256
                    && (key < 0 ? Mouse.isButtonDown(key + 100) : Keyboard.isKeyDown(key));
            if (!pressed) binding.pressTime = 0;
            binding.pressed = pressed;
        }
    }
    @EventHandler public void onRender2D(Render2DEvent event) {
        if (CreditsRenderer.INSTANCE != null) {
            CreditsRenderer.INSTANCE.render(event);
        }
    }
    @EventHandler public void onGameEnd(HypixelGameEndedEvent event) {
        if (CreditsRenderer.INSTANCE != null && event.state.lobby != null && !event.state.lobby) {
            CreditsRenderer.INSTANCE.gamesPlayed++;
        }
    }
    @EventHandler public void onTabListUpdate(PlayerKillEvent event) {
        if (CreditsRenderer.INSTANCE != null && event.isLocalPlayer()) {
            CreditsRenderer.INSTANCE.kills++;
            if (RandomUtil.RANDOM.nextDouble() > 0.67) {
                CreditsRenderer.INSTANCE.bonusKills++;
            }
        }
    }
    @EventHandler public void onWorldReset(com.adjustclient.event.WorldResetEvent event) { clearWorldState(); }
    public void clearWorldState() {
        if (entityTracker != null) entityTracker.entities = new Int2ObjectOpenHashMap<>();
        ticks = 0;
        bedTimers.clear();
        synchronized (bedBlocks) { bedBlocks.clear(); }
        velocity = 0.0F;
        bedTimer.reset();
        placementRotation = null;
        placements.clear();
    }
    @EventHandler public void onChat(ChatLineEvent event) {
        if (event.pre || playerTracker == null) return;
        if (event.chat != Adjust.chatManager.minecraftChannel.history) return;
        if (!(event.component instanceof ChatComponentText text) || !text.forkField16) return;
        if (serverState == null || serverState.antiCheatModel == null
                || serverState.antiCheatModel.serverType != com.adjustclient.server.ServerType.HYPIXEL) return;
        var game = Adjust.gameStateTracker.state;
        if (game.lobby == null || game.lobby
                || game.startTimer != null || game.game != com.adjustclient.hypixel.HypixelGame.BED_WARS
                || event.timestamp < game.locationTimer.lastMs) return;
        String sender = com.adjustclient.module.impl.render.streamer.StreamerModule.getChatSenderName(text);
        if (sender != null) {
            net.minecraft.util.IChatComponent displayName = com.adjustclient.module.impl.render.streamer.StreamerModule.extractSenderComponent(text, sender);
            playerTracker.trackName(sender, displayName);
        }
    }
    @EventHandler(priority = -20) public void onPacketAppliedPost(com.adjustclient.event.PacketAppliedEvent event) {
        if (playerTracker != null && entityTracker != null) {
            playerTracker.onPacket(event.packet);
        }
    }
    @EventHandler public void onPacketApplied(com.adjustclient.event.LowLevelPacketReceiveEvent event) {
        Packet<?> packet = event.packet;
        if (serverState != null) serverState.packetWatcher.onPacket(packet, false, false);
        if (packet instanceof S08PacketPlayerPosLook correction) {
            velocityTimer.reset();
            Adjust.movementPredictor.validator.onCorrection(correction);
        } else if (packet instanceof S01PacketJoinGame) {
            mc.timer.timerSpeed = 1.0F;
            lastSwing = null;
            field80.clear();
            attackedLivingEntity = null;
            attackedEntity = null;
            usedItemSinceMovement = false;
            if (clientInstance != null
                    && clientInstance.accountManager != null
                    && clientInstance.accountManager.isSessionAccountSelected()
                    && serverState != null
                    && serverState.antiCheatModel instanceof WatchdogModel watchdog) {
                var account = clientInstance.accountManager.selectedAccount;
                account.bans.setExpiry(watchdog.serverType, 0L, true);
                account.syncBans();
            }
            Adjust.movementPredictor.history.clear();
            Adjust.placementVerifier.reset();
            ticksSinceVelocity = Integer.MAX_VALUE;
            tps = 20.0F;
        } else if (packet instanceof S03PacketTimeUpdate) {
            long now = System.currentTimeMillis();
            float rate = 20.0F / ((float) (now - lastTimeUpdate) / 50.0F / 20.0F);
            tps = RandomUtil.round(MathHelper.clamp_float(rate, 0, 20), 2);
            lastTimeUpdate = now;
        } else if (packet instanceof S27PacketExplosion explosion) {
            if (explosion.func_149149_c() != 0 || explosion.func_149144_d() != 0 || explosion.func_149147_e() != 0)
                ticksSinceVelocity = 0;
        } else if (packet instanceof S12PacketEntityVelocity v && mc.thePlayer != null && v.getEntityID() == mc.thePlayer.getEntityId()) {
            ticksSinceVelocity = 0;
        }
        if (serverState != null && serverState.antiCheatModel != null)
            serverState.antiCheatModel.onPacket(packet);
        var latency = Adjust.latencyTracker;
        if (latency.transactions != null) {
            if (packet instanceof C00PacketKeepAlive keepAlive) {
                int id = keepAlive.getKey();
                if (id < 0 && id == (short) id) latency.transactions.acknowledge((short) id);
            }
            latency.sample.onPacket(latency.transactions, packet);
        }
    }
    @EventHandler public void onPacketObserved(DirectionalPacketEvent event) {
        if (event.isCancelled()) return;
        Packet<?> packet = event.packet;
        if (event.outgoing) {
            Adjust.placementVerifier.onPacket(packet, true);
            if (packet instanceof C08PacketPlayerBlockPlacement placement) {
                if (placement.getPlacedBlockDirection() != 255) {
                    placementTimer.reset();
                    if (mc.thePlayer != null) placementRotation = new Vec2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
                }
                if (mc.thePlayer != null && mc.thePlayer.inventory.getCurrentItem() != null
                        && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemEnderPearl) pearlTimer.reset();
                usedItemSinceMovement = true;
            } else if (packet instanceof C07PacketPlayerDigging dig) {
                if (dig.getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK
                        || dig.getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) digging = false;
                else if (dig.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) digging = true;
                dugSinceMovement = true;
            } else if (packet instanceof C02PacketUseEntity use) {
                if (use.getAction() == C02PacketUseEntity.Action.ATTACK) {
                    Entity attacked = use.getEntityFromWorld(mc.theWorld);
                    attackedEntity = attacked;
                    if (attacked instanceof com.adjustclient.util.SimulationPlayer) {
                        event.setCancelled(true);
                        return;
                    }
                    if (attacked instanceof EntityLivingBase living) attackedLivingEntity = living;
                    ticksSinceAttack = 0;
                }
            } else if (packet instanceof C09PacketHeldItemChange) {
                field204 = true;
            } else if (packet instanceof C03PacketPlayer) {
                attackedEntity = null;
                usedItemSinceMovement = false;
                dugSinceMovement = false;
                field204 = false;
            }
            return;
        }
        try {
            if (serverState != null) serverState.packetWatcher.onPacket(packet, false, true);
        } catch (RuntimeException ignored) { }
        if (!(packet instanceof net.minecraft.network.status.server.S00PacketServerInfo)) packetTimer.reset();
        try {
            if (packet instanceof S02PacketChat chat) {
                if (chat.getType() != 2) {
                    IChatComponent component = chat.getChatComponent();
                    if (logWriter != null) logWriter.write("[CHAT] " + component.getUnformattedText());
                    if (component instanceof ChatComponentText text && text.getChatComponentText_TextValue().isEmpty()
                            && text.getChatStyle().getColor() == null && text.getSiblings().size() == 1) {
                        IChatComponent child = text.getSiblings().get(0);
                        ChatStyle style = child.getChatStyle();
                        int length = child.getUnformattedText().length();
                        if (child instanceof ChatComponentText && child.getSiblings().isEmpty() && length >= 8 && length <= 130
                                && style != null && style.getColor() == ChatColor.RED
                                && style.getChatClickEvent() == null && style.getChatHoverEvent() == null && !style.getObfuscated()
                                && !placementTimer.hasElapsed(com.adjustclient.service.util.ServiceUtil.addServerDelay(300)))
                            interactionTimer.reset();
                    }
                }
            } else if (packet instanceof S2FPacketSetSlot window) {
                if (mc.thePlayer != null && mc.thePlayer.openContainer != null
                        && window.func_149175_c() == mc.thePlayer.openContainer.windowId
                        && hotbarBlockTracker != null)
                    hotbarBlockTracker.onSetSlot(window);
            } else if (packet instanceof S01PacketJoinGame) {
                if (serverState != null && !serverState.field52) {
                    serverState.field24.reset();
                    serverState.field52 = true;
                }
                Adjust.latencyTracker.reset();
            } else if (packet instanceof S47PacketPlayerListHeaderFooter list) {
                header = list.getHeader().createCopy();
                footer = list.getFooter().createCopy();
            } else if (entityTracker != null) {
                if (packet instanceof S19PacketEntityStatus status && status.getOpCode() == 2) {
                    TrackedLivingEntity living = entityTracker.get(status.entityId, TrackedLivingEntity.class);
                    if (living != null) living.onStatus(status);
                } else if (packet instanceof S0DPacketCollectItem collect) {
                    TrackedItem item = entityTracker.get(collect.getCollectedItemEntityID(), TrackedItem.class);
                    if (item != null) {
                        item.field40 = entityTracker.get(collect.getEntityID(), TrackedLivingEntity.class);
                        item.collected = true;
                        item.retentionMillis = 8000;
                        item.removed = true;
                    }
                } else if (packet instanceof S1CPacketEntityMetadata metadata && metadata.func_149376_c() != null) {
                    TrackedLocalPlayer tracked = entityTracker.get(metadata.getEntityId(), TrackedLocalPlayer.class);
                    if (tracked != null) for (net.minecraft.entity.DataWatcher.WatchableObject value : metadata.func_149376_c()) {
                        if (value.getDataValueId() == 4) {
                            if (tracked.entity instanceof net.minecraft.entity.passive.EntityWolf
                                    || tracked.entity instanceof net.minecraft.entity.passive.EntityHorse)
                                tracked.lookup((String) value.getObject());
                            break;
                        }
                    }
                }
            }
        } catch (RuntimeException failure) { failure.printStackTrace(); }
        Adjust.latencyTracker.onReceive(packet);
    }

    @SuppressWarnings("unused")
    private void vendorCheckAntiTamper() {
        if (serverState == null || serverState.field24 == null) return;
        if (!serverState.field52) {
            if (!serverState.field24.hasElapsed(10000L)) return;
            serverState.field24.reset();
            if (mc.thePlayer != null && mc.thePlayer.sendQueue != null && mc.thePlayer.sendQueue.netManager != null) {
                mc.thePlayer.sendQueue.netManager.closeChannel(new ChatComponentText("Synchronization error"));
            }
            return;
        }
        if (!serverState.field24.hasElapsed(7500L)) return;
        serverState.field24 = null;
        if (serverState.antiCheatModel == null || serverState.antiCheatModel.isAllChecksPassed()) return;
        StringBuilder failedIds = new StringBuilder();
        for (AntiCheatCheck check : serverState.antiCheatModel.checks) {
            if (!check.passed) failedIds.append(check.id);
        }
        if (mc.thePlayer != null && mc.thePlayer.sendQueue != null && mc.thePlayer.sendQueue.netManager != null) {
            mc.thePlayer.sendQueue.netManager.closeChannel(
                    new ChatComponentText("Service error.\nThe server you are connecting to is not authentic."));
        }
        String details = failedIds + " " + com.adjustclient.service.util.ServiceUtil.formatAddress(serverState);
        new com.adjustclient.backend.api.request.client.FailCheckRequest((byte) 77, details)
                .toPacket().sendAsync(null);
    }
}
