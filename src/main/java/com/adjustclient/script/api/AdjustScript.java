package com.adjustclient.script.api;

import com.adjustclient.script.api.Client;
import com.adjustclient.script.api.EnumFlags;
import com.adjustclient.script.api.Minecraft;
import com.adjustclient.script.api.MouseInputType;
import com.adjustclient.script.api.Packet;
import com.adjustclient.script.api.Script;
import com.adjustclient.script.api.Settings;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

public abstract class AdjustScript {
    protected final Client client = new Client();
    protected final Minecraft mc = new Minecraft();
    protected final Script script = new Script();
    protected final Settings settings = new Settings();

    protected AdjustScript() {
        AdjustScript.bindObjects(this);
    }

    private static native void bindObjects(AdjustScript var0);

    public static final class UserInputVerificationEvent
    extends Event {
        public native boolean isAllowBlockBreakInGui();

        public native void setAllowBlockBreakInGui(boolean var1);

        public native boolean isFlag();

        public native void setFlag(boolean var1);

        UserInputVerificationEvent() {
        }
    }

    public static final class UpdateEvent
    extends CancellableEvent {
        UpdateEvent() {
        }
    }

    public static final class TeleportEvent
    extends CancellableEvent {
        public double x;
        public double y;
        public double z;
        public float rotationYaw;
        public float rotationPitch;

        public native Set<EnumFlags> getFlags();

        public native boolean isPre();

        public native boolean isPost();

        private native double $script$read$x();

        private native void $script$write$x(double var1);

        private native double $script$read$y();

        private native void $script$write$y(double var1);

        private native double $script$read$z();

        private native void $script$write$z(double var1);

        private native float $script$read$rotationYaw();

        private native void $script$write$rotationYaw(float var1);

        private native float $script$read$rotationPitch();

        private native void $script$write$rotationPitch(float var1);

        TeleportEvent() {
        }
    }

    public static final class RenderScreenEvent
    extends Event {
        public native float getPartialTicks();

        public native float getMouseX();

        public native float getMouseY();

        public native int getScaledWidth();

        public native int getScaledHeight();

        public native double getScaleFactor();

        RenderScreenEvent() {
        }
    }

    public static final class Render3DEvent
    extends Event {
        public native float getPartialTicks();

        Render3DEvent() {
        }
    }

    public static final class PostPacketReceiveEvent
    extends Event {
        public final Packet packet = null;

        private native Packet $script$read$packet();

        PostPacketReceiveEvent() {
        }
    }

    public static final class PlayerCreateEvent
    extends Event {
        PlayerCreateEvent() {
        }
    }

    public static final class PlayerControllerUpdateEvent
    extends Event {
        public final boolean pre;

        public native boolean isPre();

        public native boolean isPost();

        private native boolean $script$read$pre();

        PlayerControllerUpdateEvent() {
            this.pre = false;
        }
    }

    public static final class PacketEvent
    extends CancellableEvent {
        public Packet packet;

        public native boolean isOutgoing();

        public native boolean isIncoming();

        private native Packet $script$read$packet();

        private native void $script$write$packet(Packet var1);

        PacketEvent() {
        }
    }

    public static final class MoveFlyingEvent
    extends CancellableEvent {
        public final float strafe;
        public final float forward;
        public float friction;
        public float rotationYaw;
        public final boolean pre;
        public final boolean post;

        public native boolean isPre();

        public native boolean isPost();

        private native float $script$read$strafe();

        private native float $script$read$forward();

        private native float $script$read$friction();

        private native void $script$write$friction(float var1);

        private native float $script$read$rotationYaw();

        private native void $script$write$rotationYaw(float var1);

        private native boolean $script$read$pre();

        private native boolean $script$read$post();

        MoveFlyingEvent() {
            this.strafe = 0.0f;
            this.forward = 0.0f;
            this.pre = false;
            this.post = false;
        }
    }

    public static final class MoveEvent
    extends CancellableEvent {
        public double motionX;
        public double motionY;
        public double motionZ;

        private native double $script$read$motionX();

        private native void $script$write$motionX(double var1);

        private native double $script$read$motionY();

        private native void $script$write$motionY(double var1);

        private native double $script$read$motionZ();

        private native void $script$write$motionZ(double var1);

        MoveEvent() {
        }
    }

    public static final class MouseInputEvent
    extends CancellableEvent {
        public native int getButton();

        public native boolean isPressed();

        public native MouseInputType getType();

        public native int getWheelDelta();

        public native boolean isInScreen();

        MouseInputEvent() {
        }
    }

    public static final class MotionEvent
    extends CancellableEvent {
        public double x;
        public double y;
        public double z;
        public float yaw;
        public float pitch;
        public boolean onGround;
        public boolean sprinting;
        public boolean sneaking;
        public final boolean pre;
        public final boolean post;

        public native boolean isPre();

        public native boolean isPost();

        private native double $script$read$x();

        private native void $script$write$x(double var1);

        private native double $script$read$y();

        private native void $script$write$y(double var1);

        private native double $script$read$z();

        private native void $script$write$z(double var1);

        private native float $script$read$yaw();

        private native void $script$write$yaw(float var1);

        private native float $script$read$pitch();

        private native void $script$write$pitch(float var1);

        private native boolean $script$read$onGround();

        private native void $script$write$onGround(boolean var1);

        private native boolean $script$read$sprinting();

        private native void $script$write$sprinting(boolean var1);

        private native boolean $script$read$sneaking();

        private native void $script$write$sneaking(boolean var1);

        private native boolean $script$read$pre();

        private native boolean $script$read$post();

        MotionEvent() {
            this.pre = false;
            this.post = false;
        }
    }

    public static final class LowLevelPacketSendEvent
    extends Event {
        public final Packet packet = null;

        private native Packet $script$read$packet();

        LowLevelPacketSendEvent() {
        }
    }

    public static final class LowLevelPacketReceiveEvent
    extends CancellableEvent {
        public final Packet packet = null;

        private native Packet $script$read$packet();

        LowLevelPacketReceiveEvent() {
        }
    }

    public static final class LeftMouseEvent
    extends CancellableEvent {
        LeftMouseEvent() {
        }
    }

    public static final class KeyboardInputEvent
    extends CancellableEvent {
        public native int getKeycode();

        public native char getKeyChar();

        public native boolean isPressed();

        public native boolean isInScreen();

        KeyboardInputEvent() {
        }
    }

    public static final class ItemSlowdownEvent
    extends Event {
        public boolean usingItem;
        public boolean allowUsingSprint;
        public boolean allowSneakingSprint;
        public float slowdownMultiplier;

        private native boolean $script$read$usingItem();

        private native void $script$write$usingItem(boolean var1);

        private native boolean $script$read$allowUsingSprint();

        private native void $script$write$allowUsingSprint(boolean var1);

        private native boolean $script$read$allowSneakingSprint();

        private native void $script$write$allowSneakingSprint(boolean var1);

        private native float $script$read$slowdownMultiplier();

        private native void $script$write$slowdownMultiplier(float var1);

        ItemSlowdownEvent() {
        }
    }

    public static final class IngameMouseEvent
    extends Event {
        public native boolean isDisabled();

        public native void setDisabled(boolean var1);

        public native boolean isBlockClick();

        public native void setBlockClick(boolean var1);

        IngameMouseEvent() {
        }
    }

    public static final class GameLoopEvent
    extends Event {
        public native boolean isPreScheduled();

        public native boolean isPostScheduled();

        GameLoopEvent() {
        }
    }

    public static final class ClientTickEvent
    extends CancellableEvent {
        ClientTickEvent() {
        }
    }

    @Retention(value=RetentionPolicy.CLASS)
    @Target(value={ElementType.METHOD})
    public static @interface Priority {
        public byte value();
    }

    public static class CancellableEvent
    extends Event {
        protected CancellableEvent() {
        }

        public final native void setCancelled();

        public final native void setCancelled(boolean var1);

        public final native boolean isCancelled();
    }

    public static class Event {
        protected Event() {
        }
    }
}

