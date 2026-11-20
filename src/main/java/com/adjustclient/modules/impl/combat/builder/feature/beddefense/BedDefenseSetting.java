package com.adjustclient.modules.impl.combat.builder.feature.beddefense;

import com.adjustclient.core.settings.Setting;
import com.adjustclient.core.settings.SettingContainer;
import com.adjustclient.utilities.ByteUtil;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public final class BedDefenseSetting extends Setting<List<BedDefensePreset>> {
    public Consumer<String> recordingCallback;

    public BedDefenseSetting(SettingContainer parent, String name, List<BedDefensePreset> defaults) {
        super(parent, name, List.copyOf(defaults), null);
    }
    public BedDefenseSetting(SettingContainer parent) { this(parent, "Defenses", BedDefensePresets.PRESETS); }

    public boolean duplicate(BedDefensePreset preset) {
        if (value.size() >= 64 || preset == null || preset.builtinId != null) return false;
        String name = uniqueName(preset.name);
        List<BedDefensePreset> copy = new ArrayList<>(value);
        copy.add(new BedDefensePreset(UUID.randomUUID(), null, name, false, preset.placements));
        setValue(copy);
        return true;
    }

    public boolean isNameAvailable(String name, UUID excludedId) {
        if (name == null || name.isBlank() || name.length() > 32) return false;
        for (BedDefensePreset preset : value) {
            if (!preset.id.equals(excludedId)
                    && preset.name.equalsIgnoreCase(name.trim())) return false;
        }
        return true;
    }

    public boolean addRecordedPreset(String name, List<BedDefensePlacement> placements) {
        if (value.size() >= 64 || !isNameAvailable(name, null) || placements.isEmpty()) return false;
        List<BedDefensePreset> copy = new ArrayList<>(value);
        copy.add(new BedDefensePreset(UUID.randomUUID(), null, name.trim(), true, placements));
        super.setValue(List.copyOf(copy));
        return true;
    }

    public void toggle(UUID id) {
        List<BedDefensePreset> copy = new ArrayList<>(value);
        for (int i = 0; i < copy.size(); i++) {
            BedDefensePreset preset = copy.get(i);
            if (preset.id.equals(id)) {
                copy.set(i, new BedDefensePreset(preset.id, preset.builtinId,
                        preset.name, !preset.enabled,
                        preset.placements));
                setValue(copy);
                return;
            }
        }
    }

    @Override public void setValue(List<BedDefensePreset> presets) {
        List<BedDefensePreset> normalized = List.copyOf(presets);
        if (value == normalized) return;
        previousValue = value;
        List<BedDefensePreset> old = value;
        value = normalized;
        if (com.adjustclient.core.gui.hud.toggled.ToggledList.INSTANCE != null)
            com.adjustclient.core.gui.hud.toggled.ToggledList.INSTANCE.dirty = true;
        if (com.adjustclient.utilities.vfs.VirtualFileSystem.INSTANCE != null) {
            com.adjustclient.core.events.SettingChangeEvent event = new com.adjustclient.core.events.SettingChangeEvent(this, normalized);
            com.adjustclient.utilities.vfs.VirtualFileSystem.INSTANCE.onSettingChange(event);
        }
        if (listeners != null) {
            for (var listener : listeners) {
                try { listener.onChange(old, normalized); }
                catch (Throwable exception) {
                    exception.printStackTrace();
                    com.adjustclient.core.logging.Logger.log(com.adjustclient.core.logging.LogLevel.ERROR, true,
                            "Error during property update of '{}': {}", name, exception);
                }
            }
        }
        previousValue = null;
    }

    @Override public void reset() { setValue(BedDefensePresets.PRESETS); }

    public void requestRecording(String name) {
        if (recordingCallback != null && isNameAvailable(name, null))
            recordingCallback.accept(name.trim());
    }

    public String uniqueName(String name) {
        String base = name.trim();
        if (isNameAvailable(base, null)) return base;
        for (int suffix = 2; ; suffix++) {
            String ending = " (" + suffix + ")";
            String candidate = base.substring(0, Math.min(base.length(), 32 - ending.length())) + ending;
            if (isNameAvailable(candidate, null)) return candidate;
        }
    }

    @Override public boolean isChanged() { return true; }

    public static boolean matchesId(UUID id, BedDefensePreset preset) { return preset.id.equals(id); }

    @Override public void read(DataInputStream input) throws IOException {
        int version = input.readUnsignedByte();
        if (version != 1) throw new IOException("Unsupported bed defense preset version: " + version);
        int count = ByteUtil.readVarInt(input);
        if (count < 0 || count > 64) throw new IOException("Invalid bed defense preset count: " + count);
        List<BedDefensePreset> result = new ArrayList<>(Math.max(0, count));
        Set<String> builtinIds = new HashSet<>();
        Set<UUID> ids = new HashSet<>();
        for (int i = 0; i < count; i++) {
            BedDefensePreset preset;
            if (input.readUnsignedByte() != 0) {
                String id = input.readUTF();
                boolean enabled = input.readBoolean();
                BedDefensePreset builtin = null;
                for (BedDefensePreset candidate : BedDefensePresets.PRESETS) {
                    if (candidate.builtinId.equals(id)) { builtin = candidate; break; }
                }
                if (builtin == null || !builtinIds.add(id)) continue;
                preset = new BedDefensePreset(builtin.id, builtin.builtinId,
                        builtin.name, enabled, builtin.placements);
            } else {
                UUID id = new UUID(input.readLong(), input.readLong());
                String name = input.readUTF();
                boolean enabled = input.readBoolean();
                int placementCount = ByteUtil.readVarInt(input);
                if (placementCount < 1 || placementCount > 256)
                    throw new IOException("Invalid placement count: " + placementCount);
                List<BedDefensePlacement> placements = new ArrayList<>(placementCount);
                Set<Long> positions = new HashSet<>();
                for (int j = 0; j < placementCount; j++) {
                    int x = input.readByte();
                    int y = input.readByte();
                    int z = input.readByte();
                    int type = input.readUnsignedByte();
                    if (type >= BedDefenseBlockType.values().length)
                        throw new IOException("Invalid bed defense block type");
                    int blockId = input.readUnsignedShort();
                    long key = ((long) (x + 16) << 12) | ((long) (y + 16) << 6) | (z + 16);
                    if (!positions.add(key)) throw new IOException("Duplicate placement position");
                    try {
                        placements.add(new BedDefensePlacement(x, y, z, BedDefenseBlockType.values()[type], blockId));
                    } catch (IllegalArgumentException exception) {
                        throw new IOException("Invalid bed defense placement", exception);
                    }
                }
                try {
                    preset = new BedDefensePreset(id, null, name, enabled, placements);
                } catch (IllegalArgumentException exception) {
                    throw new IOException("Invalid custom bed defense preset", exception);
                }
            }
            if (ids.add(preset.id)) result.add(preset);
        }
        for (BedDefensePreset preset : BedDefensePresets.PRESETS) {
            if (builtinIds.add(preset.builtinId)) result.add(preset);
        }
        setValue(result);
        notifyListeners(previousValue, value);
    }

    @Override public void write(DataOutputStream output) throws IOException {
        output.writeByte(1);
        ByteUtil.writeVarInt(output, value.size());
        for (BedDefensePreset preset : value) {
            output.writeBoolean(preset.builtinId != null);
            if (preset.builtinId != null) {
                output.writeUTF(preset.builtinId);
                output.writeBoolean(preset.enabled);
                continue;
            }
            output.writeLong(preset.id.getMostSignificantBits());
            output.writeLong(preset.id.getLeastSignificantBits());
            output.writeUTF(preset.name);
            output.writeBoolean(preset.enabled);
            ByteUtil.writeVarInt(output, preset.placements.size());
            for (BedDefensePlacement placement : preset.placements) {
                output.writeByte(placement.x);
                output.writeByte(placement.y);
                output.writeByte(placement.z);
                output.writeByte(placement.type.ordinal());
                output.writeShort(placement.blockId);
            }
        }
    }
}
