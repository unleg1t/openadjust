package com.adjustclient.module;

import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.module.impl.exploit.JoinClaimModule;
import com.adjustclient.render.ColorUtil;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ColorSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.Setting;
import com.adjustclient.util.ByteUtil;
import com.adjustclient.util.HashUtil;
import com.adjustclient.vfs.NamedVfsNode;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import com.adjustclient.module.impl.render.hud.HUDModule;

public final class ModuleSerializer extends NamedVfsNode<Module> {
    public static final Set<Setting<?>> RESTRICTED_SETTINGS = new java.util.HashSet<>();
    private static final Class<?>[] SETTING_TYPES = {
        BooleanSetting.class, com.adjustclient.setting.NumberSetting.class,
        com.adjustclient.setting.ModeSetting.class, ExpandSetting.class,
        com.adjustclient.setting.StringSetting.class, ColorSetting.class,
        com.adjustclient.setting.ButtonSetting.class, com.adjustclient.setting.KeybindSetting.class,
        com.adjustclient.setting.ItemsSetting.class, com.adjustclient.setting.BlocksSetting.class,
        com.adjustclient.setting.PositionSetting.class, com.adjustclient.setting.HotbarSetting.class
    };
    private final Module module;
    private final boolean partial;
    private String field12;

    public ModuleSerializer(Module module, boolean partial) {
        super(module);
        this.module = module;
        this.partial = partial;
    }

    @Override
    public String getName() {
        return module != null ? module.getName() : field12;
    }

    public void write(DataOutputStream output) throws IOException {
        ArrayList<Setting<?>> changed = new ArrayList<>(module.getSettings().size());
        for (Setting<?> setting : module.getSettings()) {
            if (setting.getParent() == module && setting.isChanged()) changed.add(setting);
        }
        int flags = module.isEnabled() ? 2 : 0;
        if (changed.isEmpty()) flags |= 4;
        if ((module.flags & 4) == 0) flags |= 8;
        output.writeByte(flags);
        if (changed.isEmpty()) return;
        output.writeShort(changed.size());
        for (Setting<?> setting : changed) {
            output.writeInt(HashUtil.hash(setting.getDisplayName()));
            int type = -1;
            Class<?>[] types = SETTING_TYPES;
            for (int i = 0; i < types.length; i++) {
                if (setting.getClass() == types[i]) {
                    type = i;
                    break;
                }
            }
            if (type == -1) throw new IllegalStateException("Type not present");
            output.writeByte(type);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            setting.write(new DataOutputStream(bytes));
            ByteUtil.writeVarInt(output, bytes.size());
            output.write(bytes.toByteArray());
        }
    }

    private boolean restoreLegacyTheme(Setting<?> setting, ArrayList<Setting<?>> restored) {
        if (setting != HUDModule.radiance) return false;
        Color color = ColorUtil.themeColor();
        BooleanSetting enabled = HUDModule.enableSecondary;
        ColorSetting customColor = HUDModule.secondary;
        if (color == null) {
            enabled.setValue(false);
            customColor.reset();
        } else {
            customColor.mode = ColorSetting.Mode.PLAIN;
            customColor.setValue(color);
            enabled.setValue(true);
        }
        restored.add(enabled);
        restored.add(customColor);
        return true;
    }

    public byte[] getSettingData(DataInputStream input, Setting<?> setting) throws IOException {
        if (setting.getParent() instanceof ExpandSetting) {
            ExpandSetting parent = (ExpandSetting) setting.getParent();
            byte[] parentData = getSettingData(input, parent);
            if (parentData == null) return null;
            try (DataInputStream expanded = new DataInputStream(new ByteArrayInputStream(parentData))) {
                int count = ByteUtil.readVarInt(expanded);
                if (count < 0) throw new IOException("Invalid expanded property count: " + count);
                int target = HashUtil.hash(parent.getDisplayName().concat(setting.getDisplayName()));
                for (int i = 0; i < count; i++) {
                    int hash = expanded.readInt();
                    int length = ByteUtil.readVarInt(expanded);
                    if (length < 0) throw new IOException("Invalid property length: " + length);
                    if (hash != target) {
                        expanded.skipNBytes(length);
                        continue;
                    }
                    byte[] data = new byte[length];
                    expanded.readFully(data);
                    return data;
                }
                return null;
            }
        }
        if (setting.getParent() != module) {
            throw new IOException("Property is not owned by module: " + module.getName());
        }
        if ((input.readUnsignedByte() & 4) != 0) return null;
        int count = input.readUnsignedShort();
        int target = HashUtil.hash(setting.getDisplayName());
        for (int i = 0; i < count; i++) {
            int hash = input.readInt();
            int type = input.readUnsignedByte();
            int length = ByteUtil.readVarInt(input);
            if (length < 0) throw new IOException("Invalid property length: " + length);
            if (hash != target) {
                input.skipNBytes(length);
                continue;
            }
            Class<?>[] types = SETTING_TYPES;
            if (type >= 12 || !types[type].isInstance(setting)) {
                throw new IOException("Invalid serialized property type: " + type);
            }
            byte[] data = new byte[length];
            input.readFully(data);
            return data;
        }
        return null;
    }

    public Boolean read(DataInputStream input) throws IOException {
        int flags = input.readUnsignedByte();
        Boolean enabled = null;
        if (!partial) {
            module.setHidden((flags & 8) == 0);
            if (!(module instanceof JoinClaimModule)) enabled = (flags & 2) != 0;
        }
        ArrayList<Setting<?>> restored;
        if ((flags & 4) != 0) {
            restored = new ArrayList<>(0);
        } else {
            int count = input.readUnsignedShort();
            restored = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                int hash = input.readInt();
                byte type = input.readByte();
                if (type < 0 || type >= 12) throw new IOException("Bad id: " + type);
                int length = ByteUtil.readVarInt(input);
                if (length < 0) continue;
                byte[] data = new byte[length];
                input.readFully(data);
                Setting<?> setting = null;
                for (Setting<?> candidate : module.getSettings()) {
                    if (candidate.getParent() != null && HashUtil.hash(candidate.getDisplayName()) == hash) {
                        setting = candidate;
                        break;
                    }
                }
                Set<Setting<?>> restricted = RESTRICTED_SETTINGS;
                if (partial && !restricted.contains(setting)) continue;
                if (setting == null || setting.getParent() != module) continue;
                try {
                    Class<?>[] types = SETTING_TYPES;
                    if (!types[type].isInstance(setting)) throw new ClassCastException();
                    setting.read(new DataInputStream(new ByteArrayInputStream(data)));
                    restored.add(setting);
                } catch (ClassCastException error) {
                    Logger.log(LogLevel.ERROR, true, "What is property '{}'?", type);
                } catch (Throwable error) {
                    error.printStackTrace();
                    Logger.log(LogLevel.WARN, true, "Unable to restore property '{}' in '{}'",
                            setting.getDisplayName(), module.getName());
                }
            }
        }
        for (Setting<?> setting : module.getSettings()) {
            if (setting.getParent() != module || restored.contains(setting) || setting.isTransient()) continue;
            Set<Setting<?>> restricted = RESTRICTED_SETTINGS;
            if (!partial || restricted.contains(setting)) setting.reset();
        }
        for (Setting<?> setting : restored) {
            if (restoreLegacyTheme(setting, restored)) return enabled;
        }
        return enabled;
    }
}
