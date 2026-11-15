package com.adjustclient.module.impl.player;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MovementInputEvent;
import com.adjustclient.event.Render3DEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.UtilityModule;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.setting.*;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.util.*;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.awt.Color;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public final class AntiAFKModule extends UtilityModule implements MinecraftInstance {
    public static AntiAFKModule INSTANCE;
    public final ExpandSetting requireInactivity;
    public final BooleanSetting inactivityEnabled;
    public final NumberSetting inactivityRequirement;
    private Vec3 target;
    private final Timer inactivityTimer;
    private int movementTicks;
    private int sneakTicks;
    private int stuckTicks;
    private int waitTicks;
    private int escapeStrafe;
    private int escapeForward;
    private boolean escaping;
    private boolean moveTargetAfterEscape;

    public AntiAFKModule() {
        super("Anti AFK", Category.PLAYER);
        INSTANCE = this;
        requireInactivity = new ExpandSetting(this, "Require Inactivity");
        inactivityEnabled = new BooleanSetting(requireInactivity, "Enabled", false);
        requireInactivity.enabledPredicate = () -> inactivityEnabled.isEnabled() && !inactivityEnabled.isHidden();
        inactivityRequirement = new NumberSetting(requireInactivity, "Requirement", 10, 1, 120, 1);
        inactivityTimer = new Timer();
        escaping = true;
    }

    @EventHandler public void onMovementInput(MovementInputEvent event) {
        if (!isEnabled()) return;
        if (event.forward || event.back || event.left || event.right || event.jump || event.sneak) inactivityTimer.reset();
        if (inactivityEnabled.isEnabled() && !inactivityEnabled.isHidden()
                && !inactivityTimer.hasElapsed(inactivityRequirement.getValue().longValue() * 1000L)) {
            movementTicks = sneakTicks = stuckTicks = waitTicks = 0;
            target = null;
            escaping = true;
            moveTargetAfterEscape = false;
            chooseEscapeDirection();
            return;
        }
        event.forward = event.back = event.left = event.right = event.jump = event.sneak = false;
        Vec3 position = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        if (target == null) target = position;
        float targetYaw = RotationUtil.getYawBetween(target, position).x;
        double dx = target.xCoord - position.xCoord;
        double dz = target.zCoord - position.zCoord;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance <= 0.3 && movementTicks >= 4 && mc.thePlayer.onGround) {
            escaping = true;
            movementTicks = 0;
            waitTicks = RandomUtil.nextInt(6) + 25;
            escapeForward = 1;
            escapeStrafe = 1;
        } else if (distance >= 3.0) {
            escaping = false;
            if (moveTargetAfterEscape) {
                target = position;
                moveTargetAfterEscape = false;
            }
        }
        if (waitTicks >= 1) { waitTicks--; return; }
        movementTicks++;
        if (mc.thePlayer.isCollidedHorizontally) event.jump = true;
        if (escaping) {
            event.back = escapeForward < 0;
            event.forward = escapeForward > 0;
            event.left = escapeStrafe > 0;
            event.right = escapeStrafe < 0;
        } else {
            float bestForward = 0.0F, bestStrafe = 0.0F;
            Float bestDifference = null;
            for (float forward = -1.0F; forward <= 1.0F; forward++) {
                for (float strafe = -1.0F; strafe <= 1.0F; strafe++) {
                    if (forward == 0.0F && strafe == 0.0F) continue;
                    float direction = MovementUtil.getDirection(mc.thePlayer.rotationYaw, forward, strafe);
                    float difference = Math.abs(MathHelper.wrapAngleTo180_float(targetYaw - direction));
                    if (bestDifference == null || bestDifference > difference) {
                        bestDifference = difference;
                        bestForward = forward;
                        bestStrafe = strafe;
                    }
                }
            }
            event.back = bestForward < 0.0F;
            event.forward = bestForward > 0.0F;
            event.left = bestStrafe > 0.0F;
            event.right = bestStrafe < 0.0F;
            if (Math.random() > 0.98) sneakTicks = RandomUtil.nextInt(2) + 3;
            if (sneakTicks >= 1) { event.sneak = true; sneakTicks--; }
        }
        if (MovementUtil.getSpeed() <= 0.016 || movementTicks >= 60) {
            if (++stuckTicks >= 8) {
                movementTicks = sneakTicks = stuckTicks = waitTicks = 0;
                target = null;
                escaping = true;
                moveTargetAfterEscape = false;
                chooseEscapeDirection();
                NotificationBuilder notification = new NotificationBuilder();
                notification.title = getName();
                notification.parts = new java.util.ArrayList<>();
                notification.actions = new java.util.ArrayList<>();
                notification.addText("You seem to have gotten stuck; re-calibrating position.", (int) 4290690237L).show();
                moveTargetAfterEscape = true;
            }
        } else stuckTicks = 0;
    }

    @EventHandler public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || target == null) return;
        RenderUtil.begin3d();
        Color base = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        GlStateManager.color(base.getRed() / 255.0F, base.getGreen() / 255.0F, base.getBlue() / 255.0F, 100 / 255.0F);
        var renderer = mc.getRenderManager();
        double x = target.xCoord - renderer.viewerPosX;
        double y = target.yCoord - (renderer.viewerPosY - 0.1);
        double z = target.zCoord - renderer.viewerPosZ;
        RenderUtil.drawFilledBox(new AxisAlignedBB(x - 0.300000011920929, y, z - 0.300000011920929,
                x + 0.300000011920929, y + 0.6000000238418579, z + 0.300000011920929));
        RenderUtil.end3d();
    }

    @Override public boolean canToggle(boolean enabling) { return !enabling || mc.thePlayer != null; }

    private boolean staysNearOrigin(int forward, int strafe, AtomicReference<Simulation> holder) {
        Simulation simulation = holder.get();
        if (simulation == null) {
            simulation = new Simulation(mc.thePlayer);
            holder.set(simulation);
        }
        simulation.simulateTick(mc.thePlayer, true);
        var player = simulation.player;
        player.movementInput.moveForward = forward;
        player.movementInput.moveStrafe = strafe;
        player.sprintKeyDown = true;
        player.movementInput.sneak = false;
        player.motionX = player.motionZ = 0.0;
        double x = player.posX, z = player.posZ;
        for (int tick = 0; tick < 16; tick++) {
            player.tick();
            if (player.isCollidedHorizontally && player.onGround) player.jump();
            double dx = x - player.posX, dz = z - player.posZ;
            if (Math.sqrt(dx * dx + dz * dz) >= 1.0) return false;
        }
        return true;
    }

    @Override public void onEnable() {
        movementTicks = sneakTicks = stuckTicks = waitTicks = 0;
        target = null;
        escaping = true;
        moveTargetAfterEscape = false;
        chooseEscapeDirection();
        inactivityTimer.reset();
    }

    private void chooseEscapeDirection() {
        AtomicReference<Simulation> holder = new AtomicReference<>();
        IntOpenHashSet tried = new IntOpenHashSet();
        while (tried.size() < 6) {
            escapeForward = RandomUtil.nextInt(3) - 1;
            escapeStrafe = RandomUtil.nextInt(3) - 1;
            if (escapeForward == 0 && escapeStrafe == 0) continue;
            int key = (escapeStrafe << 16) | (escapeForward << 8);
            if (tried.contains(key)) continue;
            tried.add(key);
            if (!staysNearOrigin(escapeForward, escapeStrafe, holder)) return;
        }
        NotificationBuilder notification = new NotificationBuilder();
        notification.title = getName();
                notification.parts = new java.util.ArrayList<>();
                notification.actions = new java.util.ArrayList<>();
        notification.addText("You have gotten stuck really badly and there are no possible moves from this position, sorry.", (int) 4290690237L).show();
        setEnabled(false, true);
    }
}
