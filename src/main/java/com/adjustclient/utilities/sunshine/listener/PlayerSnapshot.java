package com.adjustclient.utilities.sunshine.listener;
import java.util.List;
public class PlayerSnapshot {
    public final MotionContext<MovementListener> movement = new MotionContext<>(MovementListener.class);
    public final MotionContext<OutgoingPacketListener> outgoing = new MotionContext<>(OutgoingPacketListener.class);
    public final MotionContext<IncomingPacketListener> incoming = new MotionContext<>(IncomingPacketListener.class);
    public final MotionContext<RaytraceListener> raytrace = new MotionContext<>(RaytraceListener.class);
    public final MotionContext<MotionListener> jump = new MotionContext<>(MotionListener.class);
    public final MotionContext<SprintListener> rotation = new MotionContext<>(SprintListener.class);
    public final List<MotionContext<?>> contexts = List.of(movement, outgoing, incoming, raytrace, jump, rotation);
    public Object field32;
}
