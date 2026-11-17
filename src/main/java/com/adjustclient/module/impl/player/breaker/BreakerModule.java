package com.adjustclient.module.impl.player.breaker;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.event.*;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.*;
import com.adjustclient.util.*;
import java.awt.Color;
import java.util.function.BooleanSupplier;
import com.adjustclient.module.impl.combat.killaura.KillAuraModule;
import com.adjustclient.module.impl.combat.builder.BuilderModule;

public final class BreakerModule extends Module implements MinecraftInstance {
    public static BreakerModule INSTANCE;
    public final BreakerTargetFinder targetFinder;
    public final BreakHandler handler;
    public final BreakToolSelector toolSelector;
    public final BlockBreakTracker tracker;
    public final Cooldown cooldown;
    public final Timer priorityTimer;
    public final ModeSetting<BreakerBlock> block;
    public final RenderSettings render;
    public final ExpandSetting preventInput;
    public final BooleanSetting preventInputEnabled;
    public final ModeSetting<Priority> priority;
    public final KeybindSetting switchPriorityBind;
    public final NumberSetting breakPriorityTime;
    public final ExpandSetting spoof;
    public final BooleanSetting rotations;
    public final BooleanSetting heldItem;
    public final NumberSetting maxBreakTime;
    public final BooleanSetting breakSurrounding;
    public final BooleanSetting preventWandering;
    public final BooleanSetting hideSwings;
    public boolean breakPriority;

    public BreakerModule() {
        super("Breaker", Category.PLAYER);
        INSTANCE = this;
        targetFinder = new BreakerTargetFinder(this);
        handler = new BreakHandler(this);
        toolSelector = new BreakToolSelector(this);
        tracker = new BlockBreakTracker(this);
        cooldown = new Cooldown(0L);
        priorityTimer = new Timer();
        priorityTimer.lastMs = 0L;
        block = new ModeSetting<>(this, "Block", BreakerBlock.BED, BreakerBlock.values());
        render = new RenderSettings(this, "Render", new Color(0x78435ad1, true));
        preventInput = new ExpandSetting(this, "Prevent Input");
        preventInputEnabled = new BooleanSetting(preventInput, "Enabled", true, "Yes", "No");
        preventInput.enabledPredicate = () -> preventInputEnabled.isEnabled();
        preventInputEnabled.addListener(() -> {
            if (com.adjustclient.ui.hud.toggled.ToggledList.INSTANCE != null)
                com.adjustclient.ui.hud.toggled.ToggledList.INSTANCE.dirty = true;
        });
        priority = new ModeSetting<>(preventInput, "Priority", Priority.COMBAT, Priority.values());
        Bind bind = Adjust.INSTANCE.bindManager.register(
                "module.breaker.switchprioritybind", mode -> { }, "Breaker Switch Priority");
        switchPriorityBind = new KeybindSetting(preventInput, "Switch priority bind", bind);
        switchPriorityBind.setDescription("This will invert the priority mode when the bind is held down.");
        breakPriorityTime = new NumberSetting(preventInput, "Break priority time", 1.0F, 0.1F, 5.0F, 0.1F, new PriorityModeCondition());
        breakPriorityTime.setDescription("If breaking the block takes less than the specified amount of time, automatically prioritize break over combat.");
        spoof = new ExpandSetting(this, "Spoof");
        rotations = new BooleanSetting(spoof, "Rotations", false);
        heldItem = new BooleanSetting(spoof, "Held item", false);
        maxBreakTime = new NumberSetting(this, "Max. break time", 5.0F, 1.0F, 10.0F, 0.5F);
        maxBreakTime.setDescription("If a block is predicted to exceed the maximum break time, it will not be targeted for breaking.");
        breakSurrounding = new BooleanSetting(this, "Break surrounding", true);
        breakSurrounding.setDescription("Breaks the fastest surrounding block from the target to bypass anti-cheat checks.");
        preventWandering = new BooleanSetting(this, "Prevent wandering", false);
        preventWandering.setDescription("Prevents you from walking out of breaking range, similar to safe walking.");
        hideSwings = new BooleanSetting(this, "Hide swings", false);
        hideSwings.setDescription("Hides swings clientside (visual effect only).");
        breakPriorityTime.formatter(NumberSetting.SECONDS);
        maxBreakTime.formatter(NumberSetting.SECONDS);
    }

    public Priority getPriority() {
        if (breakPriority) return Priority.BREAK;
        Priority selected = priority.getValue();
        return switchPriorityBind.getValue().isDown() ? (selected == Priority.BREAK ? Priority.COMBAT : Priority.BREAK) : selected;
    }

    public boolean tickBreaking() {
        if (Adjust.mouseClickHelper.left.disabled || BuilderModule.INSTANCE.blockIn.active) {
            handler.target = null;
            handler.samplingFactor = RandomUtil.nextFloat(0.05F, 0.06F);
            return false;
        }
        if (preventInputEnabled.isEnabled()) {
            if (breakPriority || getPriority() == Priority.BREAK) setBreakPriority(true);
            else if (KillAuraModule.INSTANCE.isEnabled() && KillAuraModule.INSTANCE.targetSelector.rotationTarget != null) {
                handler.target = null;
                handler.samplingFactor = RandomUtil.nextFloat(0.05F, 0.06F);
                return false;
            }
        }
        if (handler.target != null && handler.tick()) return true;
        if (handler.target == null && targetFinder.canSearch() && cooldown.isExpired()) {
            BlockReachCheck target = targetFinder.findTarget();
            if (target != null) {
                if (breakPriorityTime.getValue().floatValue() * 1000.0F >= (float) target.breakTime) setBreakPriority(true);
                handler.target = target;
                handler.samplingFactor = RandomUtil.nextFloat(0.05F, 0.06F);
                return handler.tick();
            }
        }
        setBreakPriority(false);
        return false;
    }

    public boolean hasTarget() {
        return handler.target != null;
    }

    public boolean isBreakingOnRewound() {
        var server = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
        return server != null && server.antiCheatModel != null
                && server.antiCheatModel.serverType == com.adjustclient.server.ServerType.REWOUND
                && handler.target != null && mc.playerController.getIsHittingBlock();
    }

    public void setBreakPriority(boolean value) {
        if (breakPriority == value) return;
        breakPriority = value;
        if (com.adjustclient.ui.hud.toggled.ToggledList.INSTANCE != null)
            com.adjustclient.ui.hud.toggled.ToggledList.INSTANCE.dirty = true;
    }

    public static int getProgressColor(int alpha) {
        float progress = net.minecraft.util.MathHelper.clamp_float(mc.playerController.curBlockDamageMP, 0.0F, 1.0F);
        Color color = Color.getHSBColor(progress * 0.33333334F, 0.7F, 0.7F);
        return com.adjustclient.render.ColorUtil.withAlpha(color, Math.max(0, Math.min(255, alpha))).getRGB();
    }

    @EventHandler(priority = 61) public void onInput(IngameActionEvent event) {
        if (!isEnabled()) return;
        if (breakPriority) Adjust.mouseClickHelper.right.disabled = true;
        if (preventInputEnabled.isEnabled()) return;
        if (Adjust.mouseClickHelper.left.disabled || BuilderModule.INSTANCE.blockIn.active) {
            handler.target = null;
            handler.samplingFactor = RandomUtil.nextFloat(0.05F, 0.06F);
            return;
        }
        if (tickBreaking()) event.actionStage = false;
    }

    @EventHandler public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;
        BlockReachCheck target = handler.target;
        if (target == null || !target.pos.equals(event.pos)) return;
        handler.target = null;
        handler.samplingFactor = RandomUtil.nextFloat(0.05F, 0.06F);
        tracker.submit(target);
        if (target.block instanceof net.minecraft.block.BlockBed) {
            var state = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
            cooldown.timer.reset();
            cooldown.durationMs = (state == null ? 0 : state.packetWatcher.getAverageDelay()) + 300L;
            priorityTimer.reset();
        }
    }

    @EventHandler(priority = -15) public void onPlayerUpdate(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        handler.updateRotation();
    }

    @EventHandler(priority = 1) public void onBreakProgress(BlockBreakProgressEvent event) {
        if (!isEnabled() || handler.target == null || !handler.target.pos.equals(event.pos)) return;
        net.minecraft.util.BlockPos pos = event.pos;
        float hardness = BlockBreakUtil.getRelativeHardness(mc.theWorld.getBlockState(pos).getBlock(), pos, handler.getTool());
        event.damage = mc.playerController.blockBreakingTicks * hardness;
    }

    @EventHandler public void onPreventInput(StepAttemptEvent event) {
        if (!isEnabled()) return;
        if (priorityTimer.lastMs + 65L > System.currentTimeMillis()) { event.cancelled = true; return; }
        if (handler.target != null && event.to.yCoord - handler.target.targetPos.getY() >= 2.2)
            event.cancelled = true;
    }

    @EventHandler public void onProgressHud(Render2DEvent event) {
        if (!isEnabled() || !render.hudEnabled.isEnabled() || handler.target == null || mc.playerController.currentBlock.getY() == -1) return;
        net.minecraft.util.BlockPos pos = mc.playerController.currentBlock;
        float progress = mc.playerController.blockBreakingTicks
                * BlockBreakUtil.getRelativeHardness(mc.theWorld.getBlockState(pos).getBlock(), pos, handler.getTool());
        Adjust.progressBarRenderer.bars.add(new com.adjustclient.game.render.progress.ProgressBar(
                net.minecraft.util.MathHelper.clamp_float(progress, 0.0F, 1.0F)));
    }

    @EventHandler public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || !render.isEnabled() || handler.target == null) return;
        var manager = mc.getRenderManager();
        double x = manager.viewerPosX, y = manager.viewerPosY, z = manager.viewerPosZ;
        if (render.range.enabled.isEnabled()) {
            com.adjustclient.render.RenderUtil.j();
            org.lwjgl.opengl.GL11.glLineWidth(3.0F);
            org.lwjgl.opengl.GL11.glBegin(3);
            var player = com.adjustclient.render.RenderUtil.interpolatedPosition(mc.thePlayer, event.partialTicks);
            for (var point : handler.target.rangePoints) {
                double proximity = 1.0 - Math.min(point.squareDistanceTo(player) / 16.0, 1.0);
                Color color = com.adjustclient.render.ColorUtil.withAlpha(render.range.color.getColor(), (int) (proximity * 120.0 + 30.0));
                if (proximity > 0.3) color = com.adjustclient.render.ColorUtil.blend(color.brighter(), color, proximity / 2.0);
                com.adjustclient.render.RenderUtil.glColor(color.getRGB());
                org.lwjgl.opengl.GL11.glVertex3d(point.xCoord - x, point.yCoord - y, point.zCoord - z);
            }
            org.lwjgl.opengl.GL11.glEnd();
            com.adjustclient.render.RenderUtil.k();
        }
        if (!render.full.enabled.isEnabled() && !render.outline.enabled.isEnabled()) return;
        var box = BlockUtil.getSelectedBox(handler.target.pos).offset(-x, -y, -z);
        com.adjustclient.render.RenderUtil.begin3d();
        Integer progressColor = null;
        if (render.full.enabled.isEnabled()) {
            Color color = render.full.color.getColor();
            int argb = color.getRGB();
            if (render.full.progressColor.isEnabled()) { argb = getProgressColor(color.getAlpha()); progressColor = argb; }
            com.adjustclient.render.RenderUtil.glColor(argb);
            com.adjustclient.render.RenderUtil.drawFilledBox(box);
        }
        if (render.outline.enabled.isEnabled()) {
            Color color = render.outline.color.getColor();
            int argb = color.getRGB();
            if (render.outline.progressColor.isEnabled()) argb = progressColor == null ? getProgressColor(color.getAlpha()) : progressColor;
            com.adjustclient.render.RenderUtil.glColor(argb);
            com.adjustclient.render.RenderUtil.drawOutlinedBox(box);
        }
        com.adjustclient.render.RenderUtil.end3d();
    }

    @Override public void onDisable() {
        handler.target = null;
        handler.samplingFactor = RandomUtil.nextFloat(0.05F, 0.06F);
        synchronized (tracker.pending) { tracker.pending.clear(); }
        tracker.tracked.clear();
        setBreakPriority(false);
    }

    public String getSuffix() {
        if (!preventInputEnabled.isEnabled()) return null;
        return breakPriority || priority.getValue() == Priority.BREAK ? "Break" : "Combat";
    }

    @EventHandler public void onWorldLoad(WorldResetEvent event) {
        if (!isEnabled()) return;
        handler.target = null;
        handler.samplingFactor = RandomUtil.nextFloat(0.05F, 0.06F);
        synchronized (tracker.pending) { tracker.pending.clear(); }
        tracker.tracked.clear();
    }

    @EventHandler public void onBlockCollision(CollisionBoxesEvent event) {
        if (!isEnabled() || handler.target == null || !preventWandering.isEnabled()) return;
        var center = handler.target.center;
        double dx = mc.thePlayer.posX - center.xCoord, dz = mc.thePlayer.posZ - center.zCoord;
        double reach = mc.playerController.getBlockReachDistance() + 0.3;
        if (net.minecraft.util.MathHelper.sqrt_double(dx * dx + dz * dz) > reach) return;
        event.collisionBoxes.addAll(handler.target.rangeBoxes);
    }

    @EventHandler public void onPacketReceive(com.adjustclient.event.PacketAppliedEvent event) {
        if (!isEnabled()) return;
        if (event.packet instanceof net.minecraft.network.play.server.S23PacketBlockChange)
            tracker.onBlockUpdate((net.minecraft.network.play.server.S23PacketBlockChange) event.packet);
    }

    public enum Priority implements ModeOption {
        COMBAT("Combat"), BREAK("Break");
        private final String name;
        Priority(String name) { this.name = name; }
        public String getName() { return name; }
    }

    class PriorityModeCondition implements BooleanSupplier {
        public boolean getAsBoolean() { return priority.getValue() == Priority.BREAK; }
    }

    static final class RenderGroup {
        public final BooleanSetting enabled;
        public final BooleanSetting progressColor;
        public final ColorSetting color;
        RenderGroup(ExpandSetting parent, boolean enabled, boolean progressColor, Color color, boolean alpha) {
            this.enabled = new BooleanSetting(parent, "Enabled", enabled, "Yes", "No");
            parent.enabledPredicate = () -> this.enabled.isEnabled();
            this.progressColor = progressColor ? new BooleanSetting(parent, "Progress color", true) : null;
            this.color = new ColorSetting(parent, "Color", color, alpha, false, ColorSetting.Mode.PLAIN, null);
        }
    }

    static final class RenderSettings {
        public final RenderGroup full;
        public final RenderGroup outline;
        public final RenderGroup range;
        public final BooleanSetting hudEnabled;
        RenderSettings(SettingContainer parent, String name, Color color) {
            ExpandSetting group = new ExpandSetting(parent, name);
            group.enabledPredicate = () -> isEnabled();
            Color outlineColor = new Color((int) (color.getRed() * 0.7), (int) (color.getGreen() * 0.7),
                    (int) (color.getBlue() * 0.7), color.getAlpha() + 35);
            full = new RenderGroup(new ExpandSetting(group, "Full"), true, true, color, true);
            outline = new RenderGroup(new ExpandSetting(group, "Outline"), false, true,
                    outlineColor, true);
            range = new RenderGroup(new ExpandSetting(group, "Range"), true, false, color, false);
            ExpandSetting hud = new ExpandSetting(group, "HUD");
            hudEnabled = new BooleanSetting(hud, "Enabled", true, "Yes", "No");
            hud.enabledPredicate = () -> hudEnabled.isEnabled();
            hudEnabled.setDisplayName("hudEnabled");
        }
        public boolean isEnabled() { return full.enabled.isEnabled() || outline.enabled.isEnabled() || range.enabled.isEnabled(); }
    }
}
