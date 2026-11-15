package com.adjustclient.util.sunshine.tracker;
import com.adjustclient.util.sunshine.TimedSample;
import net.minecraft.network.Packet;
import java.util.*;
import java.util.function.Predicate;
public final class HistoryLookup {
    public final List<Predicate<Packet<?>>> predicates;
    public Object field8;
    public HistoryLookup(List<Predicate<Packet<?>>> predicates) { this.predicates = predicates; }
    public HistoryLookup(Class<? extends Packet<?>> type) {
        predicates = List.of(packet -> type.isInstance(packet));
    }
    public boolean matches(LinkedList<TimedSample> history) {
        if (predicates.size() > history.size()) return false;
        for (int i = predicates.size() - 1, n = 1; i >= 0; i--, n++)
            if (!predicates.get(i).test(history.get(history.size() - n).value)) return false;
        return true;
    }
}
