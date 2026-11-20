package com.adjustclient.modules.impl.movement.speed;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.CancellablePlayerUpdateEvent;
import com.adjustclient.core.events.DirectionalPacketEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.IncomingVelocityEvent;
import com.adjustclient.core.events.MotionEvent;
import com.adjustclient.core.events.MoveEvent;
import com.adjustclient.core.events.MoveFlyingEvent;
import com.adjustclient.core.events.MoveHeadingEvent;
import com.adjustclient.core.events.MovementInputEvent;
import com.adjustclient.core.events.ViewBobbingEvent;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.modules.impl.movement.step.StepModule;
import com.adjustclient.core.settings.ModeSetting;
import com.adjustclient.utilities.MovementUtil;
import com.adjustclient.utilities.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public final class SpeedModule extends Module {
    public static SpeedModule INSTANCE;
    public final ModeSetting<SpeedMode<?>> mode;
    public SpeedState state;
    private Object field40;
    private int ticks;

    public SpeedModule() {
        super("Speed", Category.MOVEMENT);
        INSTANCE = this;
        notifyToggle = false;
        VanillaSpeedMode vanilla = new VanillaSpeedMode(this);
        NcpSpeedMode ncp = new NcpSpeedMode(this);
        RewoundSpeedMode rewound = new RewoundSpeedMode(this);
        mode = new ModeSetting<>(this, "Mode", vanilla, vanilla, ncp, rewound);
        mode.addListener(() -> resetState());
        settings.remove(mode);
        settings.add(0, mode);
    }

    public void resetState() {
        if (!isEnabled()) return;
        initialize(mode.getValue());
    }

    @EventHandler
    public void onUpdate(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        SpeedMode<?> selected = mode.getValue();
        if (selected instanceof NcpSpeedMode && ((NcpSpeedMode) selected).keepMovement) toggle(false);
        if (!mode.getValue().feature.isAllowed()) toggle(false);
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!isEnabled()) return;
        if (event.pre && state != null && state.entity != null) {
            mode.getValue();
        }
    }

    @EventHandler
    public void onHeading(MoveHeadingEvent event) {
        if (!isEnabled() || state == null || state.entity == null) return;
        mode.getValue();
    }

    @EventHandler
    public void onMoveFlying(MoveFlyingEvent event) {
        if (!isEnabled()) return;
        Entity entity = state.entity;
        if (entity == null) return;
        double motionX = entity.motionX;
        double motionZ = entity.motionZ;
        if (!event.pre) {
            SpeedMode<?> selected = mode.getValue();
            if (selected instanceof VanillaSpeedMode) ((VanillaSpeedMode) selected).updateMovement((VanillaSpeedState) state);
            else if (selected instanceof NcpSpeedMode) ((NcpSpeedMode) selected).update((NcpSpeedState) state);
            else if (selected instanceof RewoundSpeedMode) ((RewoundSpeedMode) selected).update((RewoundSpeedState) state);
        }
        if (motionX != entity.motionX || motionZ != entity.motionZ) event.cancelled = true;
    }

    @EventHandler
    public void onMovementInput(MovementInputEvent event) {
        if (!isEnabled()) return;
        if (event.jump && mc.cameraOverride == null && !mc.thePlayer.capabilities.isFlying
                && (!mc.thePlayer.capabilities.allowFlying || mc.thePlayer.onGround)
                && !MovementUtil.isInLiquid()) event.jump = false;
        if (state != null && mode.getValue() instanceof NcpSpeedMode && state.entity.ridingEntity == null)
            event.sneak = false;
    }

    @EventHandler
    public void onViewBobbing(ViewBobbingEvent event) {
        if (!isEnabled() || state == null) return;
        SpeedMode<?> selected = mode.getValue();
        if (selected instanceof RewoundSpeedMode) {
            ((RewoundSpeedMode) selected).onViewBobbing();
        }
    }

    @Override
    public boolean canToggle(boolean enabling) {
        return !enabling || mode.getValue().feature.isAllowed();
    }

    @EventHandler
    public void onMoveFlyingPost(MoveFlyingEvent event) {
        if (!isEnabled() || state == null || !(mode.getValue() instanceof NcpSpeedMode)) return;
        if (event.pre && !StepModule.INSTANCE.isStepping())
            event.specialInputScaling = true;
    }

    @EventHandler(priority = 100)
    public void onVelocityPre(IncomingVelocityEvent event) {
        if (event.isCancelled() || !isEnabled() || state == null
                || !(event.packet instanceof S12PacketEntityVelocity)) return;
        S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.packet;
        SpeedMode<?> selected = mode.getValue();
        if (selected instanceof NcpSpeedMode) {
            if (packet.getEntityID() == state.entity.getEntityId()) {
                ((NcpSpeedMode) selected).onVelocity((NcpSpeedState) state, event, packet);
            }
        } else if (selected instanceof RewoundSpeedMode) {
            if (packet.getEntityID() == state.entity.getEntityId()) {
                ((RewoundSpeedState) state).damageTimer = new Timer();
            }
        }
    }

    @EventHandler
    public void onVelocity(IncomingVelocityEvent event) {
        if (event.isCancelled() || !isEnabled() || state == null || !(mode.getValue() instanceof NcpSpeedMode)
                || !(event.packet instanceof S12PacketEntityVelocity)) return;
        S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.packet;
        if (packet.getEntityID() == state.entity.getEntityId()) {
            if (packet.getMotionY() < 1) event.suppressApplication = true;
            else ((NcpSpeedState) state).field38 = true;
        }
    }

    @Override
    public void toggle(boolean enabling) {
        if (enabling && mc.thePlayer == null) enabling = false;
        if (!enabling && isEnabled() && mc.thePlayer != null && mode.getValue() instanceof NcpSpeedMode
                && !((NcpSpeedMode) mode.getValue()).canRun((NcpSpeedState) state)) return;
        if (isEnabled() == enabling) return;
        if (enabling) {
            try {
                if (!canToggle(true)) return;
            } catch (Throwable error) {
                reportToggleError(error, true);
            }
        }
        flags = (byte) (enabling ? flags | 2 : flags & ~2);
        try {
            if (enabling) onEnable();
            else onDisable();
        } catch (Throwable error) {
            reportToggleError(error, enabling);
        }
        if (notifyToggle) Adjust.INSTANCE.fileSystem.markDirty();
    }

    @Override
    public void onEnable() {
        resetState();
    }

    @Override
    public void onDisable() {
        clearState();
    }

    @Override
    public String getSuffix() {
        return mode.getValue().getName();
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        if (!isEnabled() || state == null || state.entity == null) return;
        mode.getValue();
    }

    @EventHandler
    public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isEnabled() || state == null) return;
        if (!event.outgoing && event.packet instanceof S01PacketJoinGame) toggle(false);
        else mode.getValue();
    }

    @Override
    public void setEnabled(boolean enabling, boolean notify) {
        if (!enabling && isEnabled() && mc.thePlayer != null && mode.getValue() instanceof NcpSpeedMode
                && !((NcpSpeedMode) mode.getValue()).checkMovement((NcpSpeedState) state)) return;
        if (isEnabled() == enabling) return;
        if (enabling) {
            try {
                if (!canToggle(true)) return;
            } catch (Throwable error) {
                reportToggleError(error, true);
            }
        }
        flags = (byte) (enabling ? flags | 2 : flags & ~2);
        try {
            if (enabling) initialize(mode.getValue());
            else clearState();
        } catch (Throwable error) {
            reportToggleError(error, enabling);
        }
        if (notifyToggle) Adjust.INSTANCE.fileSystem.markDirty();
    }

    public void initialize(SpeedMode<?> selected) {
        state = selected.createState(mc.thePlayer);
        ticks = 0;
        if (selected instanceof NcpSpeedMode) {
            NcpSpeedMode ncp = (NcpSpeedMode) selected;
            NcpSpeedState ncpState = (NcpSpeedState) state;
            ncp.reset(ncpState);
            ncp.keepMovement = false;
            ncpState.jumpTicks = ncp.jumpTicks.getInt();
        } else if (selected instanceof VanillaSpeedMode) {
            ((VanillaSpeedMode) selected).initialize((VanillaSpeedState) state);
        } else if (selected instanceof RewoundSpeedMode) {
            ((RewoundSpeedMode) selected).reset((RewoundSpeedState) state);
        }
    }

    public void clearState() {
        if (state == null) return;
        SpeedMode<?> selected = mode.getValue();
        if (selected instanceof NcpSpeedMode) ((NcpSpeedMode) selected).reset((NcpSpeedState) state);
        else if (selected instanceof VanillaSpeedMode) ((VanillaSpeedMode) selected).limitSpeed((VanillaSpeedState) state);
        else if (selected instanceof RewoundSpeedMode) ((RewoundSpeedMode) selected).reset((RewoundSpeedState) state);
        state = null;
    }
}
