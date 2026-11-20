package com.adjustclient.utilities.script.engine;

import com.adjustclient.utilities.script.engine.event.*;
import java.util.List;

public final class ScriptEventTypes {
    public static final List<ScriptEventType<?>> TYPES;

    static {
        TYPES = List.of(ClientTickEventType.INSTANCE, GameLoopEventType.INSTANCE,
                IngameMouseEventType.INSTANCE, ItemSlowdownEventType.INSTANCE,
                KeyboardInputEventType.INSTANCE, LeftMouseEventType.INSTANCE,
                LowLevelPacketReceiveEventType.INSTANCE, LowLevelPacketSendEventType.INSTANCE,
                MotionEventType.INSTANCE, MouseInputEventType.INSTANCE, MoveEventType.INSTANCE,
                MoveFlyingEventType.INSTANCE, PacketEventType.INSTANCE,
                PlayerControllerUpdateEventType.INSTANCE, PlayerCreateEventType.INSTANCE,
                PostPacketReceiveEventType.INSTANCE, Render3DEventType.INSTANCE,
                RenderScreenEventType.INSTANCE, TeleportEventType.INSTANCE,
                UpdateEventType.INSTANCE, UserInputVerificationEventType.INSTANCE);
    }
}
