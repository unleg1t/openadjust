package com.adjustclient.util.sunshine;
import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.sunshine.check.SunshineCheck;
import com.adjustclient.util.sunshine.check.movement.*;
import com.adjustclient.util.sunshine.check.player.*;
import com.adjustclient.util.sunshine.check.packet.*;
import com.adjustclient.util.sunshine.check.interact.*;
import com.adjustclient.util.sunshine.check.raytrace.*;
import com.adjustclient.util.sunshine.listener.*;
import com.adjustclient.util.sunshine.tracker.*;
import com.adjustclient.util.sunshine.intave.IntaveViolations;
import net.minecraft.network.Packet;
import java.util.*;
import net.minecraft.util.ChatColor;
public final class SunshineAnticheat {
    public final PlayerSnapshot listeners;
    public final TimedPacketTracker packetTracker;
    public final List<SunshineCheck> checks;
    public final PositionTracker positionTracker;
    public final NoopPacketTracker noopTracker;
    public final SunshineState state;
    public final SunshineCheckEntry entry;
    public final IntaveViolations intave;
    public SunshineAnticheat() {
        listeners=new PlayerSnapshot();
        packetTracker=new TimedPacketTracker(this);
        listeners.outgoing.listeners.add(packetTracker);
        checks=List.of(new JumpMotionCheck(),new RaytraceCheck(),new BlockPlaceCheck(),new UseCheck(),new ChatInputCheck(),
                new TrackedRepliesCheck(),new GuiActionCheck(),new GroundCheck(),new RotationCheck(),new GroundJumpCheck(),
                new OmniSprintJumpCheck(),new OmniSprintCheck(),new NukerFrequencyCheck(),new InvalidPlayerPacketCheck(),new EntityActionCheck());
        positionTracker=new PositionTracker(this);
        listeners.outgoing.listeners.add(positionTracker);
        noopTracker=new NoopPacketTracker(this);
        listeners.outgoing.listeners.add(noopTracker);
        state=new SunshineState(this); entry=new SunshineCheckEntry(this); intave=new IntaveViolations();
        checks.forEach(check -> {
            check.anticheat=this;
            for (MotionContext<?> context:listeners.contexts) {
                if (context.listenerType.isInstance(check)) {
                    @SuppressWarnings("unchecked") Set<CheckListener> set=(Set<CheckListener>)(Set<?>)context.listeners;
                    set.add((CheckListener)check);
                }
            }
        });
    }
    public void onOutgoingPacket(Packet<?> packet) {
        listeners.outgoing.listeners.forEach(listener -> listener.onOutgoingPacket(packet));
    }
    public static ChatMessage createMessage() {
        ChatMessage message=new ChatMessage();
        message.color=ChatColor.DARK_GRAY; message.bold=true; message.append("[");
        message.color=ChatColor.RESET; message.bold=true; message.append("Sunshine");
        message.color=ChatColor.DARK_GRAY; message.bold=true; message.append("] ");
        return message;
    }
    public void onIncomingPacket(Packet<?> packet) {
        listeners.incoming.listeners.forEach(listener -> listener.onIncomingPacket(packet));
    }
}
