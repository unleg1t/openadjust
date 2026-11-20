package com.adjustclient.modules.impl.player;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.*;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.UtilityModule;
import com.adjustclient.modules.util.CpsSettings;
import com.adjustclient.core.rendering.ColorUtil;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.settings.*;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.utilities.BlockUtil;
import com.adjustclient.utilities.RandomUtil;
import com.adjustclient.utilities.RotationUtil;
import com.adjustclient.utilities.WorldUtil;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.server.ServerType;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.*;
import java.awt.Color;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.ChatColor;
import com.adjustclient.utilities.game.player.InventoryStateTracker;

public final class SeasonalSimulatorModule extends UtilityModule {
    public final ExpandSetting collectAura;
    public final BooleanSetting collectEnabled;
    public final CpsSettings cps;
    public final BooleanSetting swingClientSide;
    public BlockOverlaySettings overlay;
    private final List<OverlayBlock> overlayBlocks = new ArrayList<>();
    private final Map<BlockPos, Timer> candyTimers = new HashMap<>();
    public static SeasonalSimulatorModule INSTANCE;
    private static final Pattern CANDY_COUNT;

    static { CANDY_COUNT = Pattern.compile("\\((\\d+)\\s[^)]*\\)"); }

    public SeasonalSimulatorModule() {
        super("Seasonal Simulator", Category.PLAYER);
        INSTANCE = this;
        collectAura = new ExpandSetting(this, "Collect Aura");
        collectEnabled = new BooleanSetting(collectAura, "Enabled", true, "Yes", "No");
        collectAura.enabledPredicate = new CollectAuraCondition(this);
        cps = new CpsSettings(collectAura, 6, 10);
        swingClientSide = new BooleanSetting(collectAura, "Swing (clientside)", true);
        overlay = new BlockOverlaySettings(this, new Color(22, 31, 255, 55),
                () -> { if (!overlay.isEnabled()) overlayBlocks.clear(); });
    }

    @EventHandler(priority = 44) public void onUpdate(IngameActionEvent event) {
        if (event.suppressActions || !isEnabled()) return;
        var server = ServerTracker.INSTANCE.serverState;
        if (server == null || server.antiCheatModel == null
                || server.antiCheatModel.serverType != ServerType.HYPIXEL
                || InventoryStateTracker.isInServersideGui()) return;
        if (collectEnabled.isEnabled() && isCollectionGame() && Adjust.cpsCounter.canClick(cps)) collectCandy();
    }

    @EventHandler public void onMotion(MotionEvent event) {
        if (!isEnabled() || !event.pre) return;
        var server = ServerTracker.INSTANCE.serverState;
        if (server == null || server.antiCheatModel == null
                || server.antiCheatModel.serverType != ServerType.HYPIXEL) {
            setEnabled(false, true); return;
        }
        overlayBlocks.clear();
        if (!overlay.isEnabled()) return;
        for (TileEntity tile : mc.theWorld.loadedTileEntityList) {
            if (!(tile instanceof TileEntitySkull) || shouldIgnoreSkull((TileEntitySkull) tile)) continue;
            BlockPos pos = tile.getPos();
            AxisAlignedBB box = BlockUtil.getCollisionBox(pos);
            if (box == null) continue;
            boolean collected = isRecentlyCollected(pos);
            Color full = overlay.fullColor.getColor();
            Color outline = overlay.outlineColor.getColor();
            if (collected) {
                full = new Color(255 - full.getRed(), 255 - full.getGreen(), 255 - full.getBlue(), full.getAlpha());
                outline = new Color(255 - outline.getRed(), 255 - outline.getGreen(), 255 - outline.getBlue(), outline.getAlpha());
            }
            if (overlay.fullDistanceColor.isEnabled() || overlay.outlineDistanceColor.isEnabled()) {
                float fraction = 1.0F - (float) (Math.sqrt(mc.thePlayer.getDistanceSq(box.minX, box.minY, box.minZ)) / 42.0);
                fraction = Math.max(0.0F, Math.min(1.0F, fraction));
                Color distance = new Color(collected ? -12763751 : Color.HSBtoRGB(fraction * 0.33333334F, 0.6F, 0.6F), true);
                if (overlay.fullDistanceColor.isEnabled()) full = ColorUtil.withAlpha(distance, full.getAlpha());
                if (overlay.outlineDistanceColor.isEnabled()) outline = ColorUtil.withAlpha(distance, outline.getAlpha());
            }
            overlayBlocks.add(new OverlayBlock(box, full.getRGB(), outline.getRGB()));
        }
    }

    @EventHandler public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || overlayBlocks.isEmpty()) return;
        RenderUtil.begin3d();
        double x = mc.getRenderManager().viewerPosX;
        double y = mc.getRenderManager().viewerPosY;
        double z = mc.getRenderManager().viewerPosZ;
        for (OverlayBlock block : overlayBlocks) {
            AxisAlignedBB box = block.bounds.offset(-x, -y, -z);
            if (overlay.fullEnabled.isEnabled() && overlay.fullColor.getColor().getAlpha() != 0) {
                RenderUtil.glColor(block.fullColor);
                RenderUtil.drawFilledBox(box);
            }
            if (overlay.outlineEnabled.isEnabled() && overlay.fullColor.getColor().getAlpha() != 0) {
                RenderUtil.glColor(block.outlineColor);
                RenderUtil.drawOutlinedBox(box);
            }
        }
        RenderUtil.end3d();
    }

    public boolean shouldIgnoreSkull(TileEntitySkull skull) {
        if (skull.getSkullType() != 3 || isRecentlyCollected(skull.getPos())) return true;
        var profile = skull.getPlayerProfile();
        if (profile == null || profile.getProperties() == null || !"\u00a7item".equals(profile.getName())) return true;
        return profile.getProperties().isEmpty();
    }

    @Override public boolean canToggle(boolean enabling) {
        if (!enabling || mc.thePlayer == null) return true;
        var server = ServerTracker.INSTANCE.serverState;
        return server != null && server.antiCheatModel != null
                && server.antiCheatModel.serverType == ServerType.HYPIXEL;
    }

    public boolean isRecentlyCollected(BlockPos position) {
        if (!candyTimers.containsKey(position)) return false;
        Timer timer = candyTimers.get(position);
        var server = ServerTracker.INSTANCE.serverState;
        int latency = 0;
        if (server != null && server.packetWatcher != null) {
            var samples = server.packetWatcher;
            if (samples.delayCount != 0)
                latency = samples.totalDelay / samples.delayCount;
        }
        return !timer.hasElapsed(140L + latency);
    }

    @Override public void onDisable() { overlayBlocks.clear(); }

    @Override public String getDescription() {
        return "Contains various cheats for the Seasonal Simulator mini-games on Hypixel.";
    }

    public void collectCandy() {
        for (TileEntity tile : mc.theWorld.loadedTileEntityList) {
            if (!(tile instanceof TileEntitySkull) || shouldIgnoreSkull((TileEntitySkull) tile)) continue;
            BlockPos pos = tile.getPos();
            if (!(Math.sqrt(mc.thePlayer.getDistanceSq(pos.getX(), pos.getY(), pos.getZ())) <= 6.0)) continue;
            AxisAlignedBB box = BlockUtil.getCollisionBox(pos);
            if (box == null) continue;
            float factor = RandomUtil.nextFloat(0.99F, 1.0F);
            var candidates = RotationUtil.getSortedHitVecs(mc.thePlayer, box, EnumFacing.UP, factor, factor, 3.0F);
            var hit = RotationUtil.findHitRotation(candidates, mc.theWorld.getBlockState(pos).getBlock(), pos);
            if (hit == null) continue;
            WorldUtil.setMouseOver(hit.getRight());
            Adjust.mouseClickHelper.right.clicks = 1;
            Adjust.mouseClickHelper.right.down = true;
            Adjust.mouseClickHelper.right.holdTicks = 0;
            if (swingClientSide.isEnabled()) mc.thePlayer.swingItem();
            Adjust.cpsCounter.recordClick();
            candyTimers.put(pos, new Timer());
            return;
        }
    }

    @SuppressWarnings("unused")
    private static void vendorReportBypass() {
        com.adjustclient.managers.util.ServiceUtil.reportFeatureUse(
                com.adjustclient.managers.telemetry.BypassFeature.HALLOWEEN_SIMULATOR);
    }

    @EventHandler public void onPacketReceive(com.adjustclient.core.events.DirectionalPacketEvent event) {
        if (event.isCancelled() || !isEnabled() || event.outgoing || !(event.packet instanceof S02PacketChat)) return;
        S02PacketChat packet = (S02PacketChat) event.packet;
        if (packet.getType() == 1 || packet.getType() == 2 || !isCollectionGame()) return;
        IChatComponent message = packet.getChatComponent();
        if (message.getSiblings().size() != 2 || !message.getUnformattedTextForChat().isEmpty()) return;
        IChatComponent first = message.getSiblings().get(0);
        if (first.getChatStyle().getColor() != ChatColor.GREEN || !first.getUnformattedText().endsWith(" ")) return;
        IChatComponent second = message.getSiblings().get(1);
        String text = second.getUnformattedText();
        if (second.getChatStyle().getColor() != ChatColor.GRAY || !text.startsWith("(") || !text.endsWith(")")) return;
        Matcher matcher = CANDY_COUNT.matcher(text);
        if (matcher.find()) {
            NotificationBuilder notification = new NotificationBuilder();
            notification.title = "Seasonal Simulator";
            notification.parts = new ArrayList<>();
            notification.actions = new ArrayList<>();
            notification.addText("Candy collected, found ", 0xffbebcbd).append(matcher.group(1))
                    .addText(" candies so far.", 0xffbebcbd).show();
        }
    }

    public static boolean isCollectionGame() {
        var state = Adjust.gameStateTracker.state;
        if (state.lobby == null || state.lobby || state.startTimer == null || state.ended) return false;
        return state.game.isAnyOf(HypixelGame.HALLOWEEN_SIMULATOR, HypixelGame.GRINCH_SIMULATOR,
                HypixelGame.EASTER_SIMULATOR, HypixelGame.SCUBA_SIMULATOR);
    }

    public static final class BlockOverlaySettings {
        public final BooleanSetting fullEnabled;
        public final BooleanSetting fullDistanceColor;
        public final ColorSetting fullColor;
        public final BooleanSetting outlineEnabled;
        public final BooleanSetting outlineDistanceColor;
        public final ColorSetting outlineColor;

        public BlockOverlaySettings(SettingContainer parent, Color color, Runnable changed) {
            ExpandSetting group = new ExpandSetting(parent, "Block Overlay");
            group.enabledPredicate =
                    () -> isEnabled();
            ExpandSetting full = new ExpandSetting(group, "Full");
            fullEnabled = new BooleanSetting(full, "Enabled", true, "Yes", "No");
            fullDistanceColor = new BooleanSetting(full, "Distance color", true);
            fullColor = new ColorSetting(full, "Color", color,
                    () -> fullDistanceColor.isEnabled());
            ExpandSetting outline = new ExpandSetting(group, "Outline");
            full.enabledPredicate =
                    () -> fullEnabled.isEnabled() && fullColor.getColor().getAlpha() != 0;
            outlineEnabled = new BooleanSetting(outline, "Enabled", true, "Yes", "No");
            outline.enabledPredicate =
                    () -> outlineEnabled.isEnabled() && fullColor.getColor().getAlpha() != 0;
            outlineDistanceColor = new BooleanSetting(outline, "Distance color", true);
            outlineColor = new ColorSetting(outline, "Color",
                    new Color((int) (color.getRed() * 0.7), (int) (color.getGreen() * 0.7),
                            (int) (color.getBlue() * 0.7), color.getAlpha() + 35),
                    () -> outlineDistanceColor.isEnabled());
            fullEnabled.addListener(changed);
            outlineEnabled.addListener(changed);
        }

        public boolean isEnabled() {
            return fullEnabled.isEnabled() && fullColor.getColor().getAlpha() != 0
                    || outlineEnabled.isEnabled() && fullColor.getColor().getAlpha() != 0;
        }
    }

    static class CollectAuraCondition implements java.util.function.BooleanSupplier {
        final SeasonalSimulatorModule owner;
        CollectAuraCondition(SeasonalSimulatorModule owner) { this.owner = owner; }
        public boolean getAsBoolean() { return owner.collectEnabled.isEnabled(); }
    }

    static final class OverlayBlock {
        public final AxisAlignedBB bounds;
        public final int fullColor;
        public final int outlineColor;
        OverlayBlock(AxisAlignedBB bounds, int fullColor, int outlineColor) {
            this.bounds = bounds;
            this.fullColor = fullColor;
            this.outlineColor = outlineColor;
        }
    }
}
