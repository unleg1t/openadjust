package com.adjustclient.module.impl.combat.builder.feature.beddefense;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class BedDefensePreset {
    public final UUID id;
    public final String builtinId;
    public final String name;
    public final List<BedDefensePlacement> placements;
    public final boolean enabled;

    public BedDefensePreset(UUID id, String builtinId, String name, boolean enabled,
                            List<BedDefensePlacement> placements) {
        if (name == null || name.isBlank() || name.length() > 32) throw new IllegalArgumentException("Invalid preset name");
        if (placements.isEmpty() || placements.size() > 256)
            throw new IllegalArgumentException("Invalid preset placement count");
        this.id = Objects.requireNonNull(id);
        this.builtinId = builtinId;
        this.name = name;
        this.enabled = enabled;
        this.placements = List.copyOf(placements);
    }

    @Override public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof BedDefensePreset other)) return false;
        return enabled == other.enabled
                && id.equals(other.id) && Objects.equals(builtinId, other.builtinId)
                && name.equals(other.name)
                && placements.equals(other.placements);
    }

    @Override public int hashCode() {
        return Objects.hash(id, builtinId, name,
                enabled, placements);
    }

    public static BedDefensePreset create(String id, String displayName, List<BedDefensePlacement> placements) {
        return new BedDefensePreset(UUID.nameUUIDFromBytes(("adjust:bed-defense:" + id)
                .getBytes(StandardCharsets.UTF_8)), id, displayName, true, placements);
    }
}
