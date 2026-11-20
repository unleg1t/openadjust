package com.adjustclient.modules.impl.movement.scaffold;

import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.Listener;
import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.core.events.CancellablePlayerUpdateEvent;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import java.awt.Color;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.WorldSettings;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.core.rendering.font.Fonts;

public final class BlockCountHud implements Listener {

    public static BlockCountHud instance;

    private final ScaffoldModule module;
    private final BooleanSetting hud;
    private final BooleanSetting prediction;
    private PredictionState predictionState;
    private final Animation animation;
    private BlockCountDisplay display;

    public BlockCountHud(ScaffoldModule module, ExpandSetting render) {
        this.animation = new Animation(Easing.EASE_OUT_EXPO, 200);
        this.module = module;
        this.hud = new BooleanSetting(render, "Block count HUD", true, "On", "Off");
        this.hud.setDisplayName("HUD");
        this.prediction = new BooleanSetting(render, "Block count prediction", false, "On", "Off",
                () -> !this.isHudEnabled());
        this.prediction.setDescription("Displays how many blocks are required to reach your predicted destination (experimental).");
        instance = this;
    }

    /** "Block count HUD" is on (and not hidden). */
    private boolean isHudEnabled() {
        return this.hud.isEnabled();
    }

    @EventHandler public void onWorldReset(com.adjustclient.core.events.WorldResetEvent event) {
        this.display = null;
    }

    @EventHandler public void onTick(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled()) return;
        if (this.hud.isEnabled()) {
            if (this.module.isEnabled() && this.module.autoEnableTimer == null) {
                int blocks;
                if (Minecraft.getMinecraft().playerController.getCurrentGameType() == WorldSettings.GameType.CREATIVE) {
                    blocks = Integer.MAX_VALUE;
                } else {
                    blocks = ServerTracker.INSTANCE.hotbarBlockTracker.getTotalBlocks();
                }
                this.display = new BlockCountDisplay(blocks);
                // forget the bridge prediction when it was not updated for 6 seconds
                if (this.predictionState != null && this.predictionState.timer.hasElapsed(6000L)) {
                    this.predictionState = null;
                }
                return;
            }
        }
        if (this.display == null) {
            return;
        }
        if (!this.display.hiding) {
            this.display.hiding = true;
            return;
        }
        if (this.animation.isFinished()) {
            this.display = null;
        }
    }

    @EventHandler public void onRender2D(Render2DEvent event) {
        BlockCountDisplay display = this.display;
        if (display == null) {
            return;
        }
        this.animation.update(display.hiding ? 0.0F : 1.0F);
        float totalWidth = display.totalWidth;
        ScaledResolution resolution = event.scaledResolution;
        int screenWidth = resolution.getScaledWidth();
        int screenHeight = resolution.getScaledHeight();
        int alpha = (int) (this.animation.getValue() * 255.0F);
        alpha = MathHelper.clamp_int(alpha, 0, 255);
        if (alpha < 25) {
            return;
        }
        float y = screenHeight * 0.5F + 12.0F;
        float x = screenWidth * 0.5F - totalWidth * 0.5F;

        Color theme = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        int countColor = theme.getRGB() & 0xFFFFFF | alpha << 24;
        Fonts.TAHOMA_10.drawString(display.countText, x, y, countColor);

        new Color(233, 235, 234, alpha);
        int textColor = alpha << 24 | 0xE9EBEA;
        String suffix = " block" + (display.count == 1 ? "" : "s");
        if (this.prediction.isEnabled()) {
            String approx = "";
            if (this.predictionState != null && this.predictionState.destination != null) {
                Vec3 position = Minecraft.getMinecraft().thePlayer.getPositionVector();
                BlockPos destination = this.predictionState.destination;
                double dx = (double) destination.getX() - position.xCoord;
                double dy = (double) destination.getY() - position.yCoord;
                double dz = (double) destination.getZ() - position.zCoord;
                int needed = (int) MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz);
                approx = ", approx ~" + needed + " needed";
            }
            suffix = suffix + approx;
        }
        Fonts.TAHOMA_10.drawString(suffix, x + display.countWidth, y, textColor);
    }

    public void onPlace(PlacementTarget event) {
        // restart the average when the yaw changed by more than 15 degrees
        boolean reset = true;
        if (this.predictionState != null) {
            double diff = MathHelper.wrapAngleTo180_double(this.predictionState.yawAverage - event.yaw);
            reset = Math.abs(diff) > 15.0;
        }
        if (reset) {
            this.predictionState = new PredictionState();
        }
        PredictionState state = this.predictionState;
        state.yawSum = state.yawSum + event.yaw;
        state.samples++;
        state.yawAverage = state.yawSum / (double) state.samples;
        state.samples2++;
        if (state.samples2 >= 2 && state.destination == null) {
            state.destination = this.predictDestination(event, state.yawAverage);
        }
        this.predictionState.timer.reset();
    }

    /**
     * Walks from the placement position along {@code yaw} (up to 254 blocks, starting 7 blocks away) and returns the
     * first position (y-1, y, y+1 at each step) that contains a block with a collision box of non-zero height, or null.
     * The returned position is the reused {@link BlockPos.MutableBlockPos}.
     */
    private BlockPos predictDestination(PlacementTarget event, double yaw) {
        Vec3i origin = event.pos;
        double radians = yaw * 0.0174532925199433;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        for (int i = 7; i < 255; i++) {
            int dx = (int) (-sin * (double) i);
            int dz = (int) ((double) i * cos);
            for (int dy = -1; dy <= 1; dy++) {
                pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz); // MutableBlockPos.func_181079_c
                IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(pos);
                Block block = state.getBlock();
                AxisAlignedBB box = block.getCollisionBoundingBox(Minecraft.getMinecraft().theWorld, pos, state);
                if (box != null && box.maxY - box.minY != 0.0) {
                    return pos;
                }
            }
        }
        return null;
    }
}
