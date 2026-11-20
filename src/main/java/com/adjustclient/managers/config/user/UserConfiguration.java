package com.adjustclient.managers.config.user;

import com.adjustclient.Adjust;
import com.adjustclient.core.settings.*;
import com.adjustclient.managers.config.*;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.utilities.vfs.VirtualFileSystem;
import com.adjustclient.core.gui.account.BanFilter;
import com.adjustclient.modules.impl.render.hud.GlobalChatMode;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import com.adjustclient.core.changelog.Changelog;
import com.adjustclient.modules.impl.render.ClickGui;
import com.adjustclient.modules.impl.render.streamer.StreamerModule;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.managers.storage.LocalStorage;
import com.adjustclient.managers.util.ServiceUtil;

public class UserConfiguration implements SettingContainer {
    public byte[] lastSaved;
    public final ArrayList<Setting<?>> settings = new ArrayList<>();
    public final ConfigurationApplier configurationBinds = new ConfigurationApplier();
    public BanFilter banFilter = BanFilter.ALL;
    public com.adjustclient.core.gui.account.AddAccountType addAccountType = com.adjustclient.core.gui.account.AddAccountType.values()[0];
    public com.adjustclient.managers.user.Cape cape = com.adjustclient.managers.user.Cape.NONE;
    public String cubelifyToken;
    public String field32;
    public String field36;
    public long timestamp;
    public Object field48;
    public boolean localLoaded;
    public static UserConfiguration INSTANCE;
    public static boolean changelogUnread;
    public static NumberSetting scale;
    public static NumberSetting secondaryScale;
    public static ModeSetting<GlobalChatMode> chatMode;
    public static BooleanSetting senseEnabled;
    public static BooleanSetting onlineNotifications;
    public static BooleanSetting serverNotifications;
    public static String field2f84;
    public static List<NumberSetting> SCALES;
    public static ApiKeyProperty HYPIXEL_API_KEY;
    public static ApiKeyProperty ANTI_SNIPER_API_KEY;
    public static ApiKeyProperty SERAPH_API_KEY;
    public static ApiKeyProperty URCHIN_API_KEY;
    public static CommandPrefixProperty COMMAND_PREFIX;
    public static CommandPrefixProperty IRC_PREFIX;
    public static List<CommandPrefixProperty> PREFIXES;
    public static List<ApiKeyProperty> API_KEYS;

    static {
        scale = new NumberSetting(null, "Scale", 2, 1, 16, 1, null);
        secondaryScale = new NumberSetting(null, "Scale", 2, 1, 16, 1, null);
        chatMode = new ModeSetting<>(null, "Mode", GlobalChatMode.ALWAYS, null, Arrays.asList(GlobalChatMode.values()));
        senseEnabled = new BooleanSetting(null, "Enabled", true, "Yes", "No", null);
        senseEnabled.setDescription("Controls sense state. If disabled, you can't see who is using Adjust and other people won't know you are using Adjust.");
        onlineNotifications = new BooleanSetting(null, "Online Status", true, "On", "Off", null);
        onlineNotifications.setDescription("Receive a notification when your friends come online.");
        serverNotifications = new BooleanSetting(null, "Server Status", true, "On", "Off", null);
        serverNotifications.setDescription("Receive a notification when your friends switch servers.");
        SCALES = List.of(scale, secondaryScale);
        HYPIXEL_API_KEY = new ApiKeyProperty(null, "Hypixel");
        ANTI_SNIPER_API_KEY = new ApiKeyProperty(null, "Anti-Sniper");
        SERAPH_API_KEY = new ApiKeyProperty(null, "Seraph");
        URCHIN_API_KEY = new ApiKeyProperty(null, "Urchin");
        COMMAND_PREFIX = new CommandPrefixProperty("Commands", '.');
        IRC_PREFIX = new CommandPrefixProperty("IRC", '@');
        PREFIXES = List.of(COMMAND_PREFIX, IRC_PREFIX);
        API_KEYS = List.of(HYPIXEL_API_KEY, ANTI_SNIPER_API_KEY, SERAPH_API_KEY, URCHIN_API_KEY);
        SERAPH_API_KEY.setting.hideIf = () -> true;
        NumberSetting.Formatter formatter = setting -> {
            return setting.getValue().intValue() == setting.getMax().intValue() ? "Auto"
                    : NumberSetting.FORMAT.format(setting.getValue().intValue());
        };
        scale.formatter(formatter);
        secondaryScale.formatter(formatter);
    }

    @Override public ArrayList<Setting<?>> getSettings() { return settings; }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(256);
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeShort(18);
        com.adjustclient.core.gui.clickgui.ClickGuiScreen screen = com.adjustclient.core.gui.clickgui.ClickGuiScreen.INSTANCE;
        if (screen == null || !screen.initialized || !screen.dragMoved) {
            out.writeByte(0);
        } else {
            out.writeByte(screen.tabs.size());
            for (com.adjustclient.core.gui.clickgui.ClickGuiTab tab : screen.tabs) {
                out.writeInt(tab.id);
                out.writeInt(Float.floatToRawIntBits(tab.x));
                out.writeInt(Float.floatToRawIntBits(tab.y));
            }
        }
        for (ApiKeyProperty property : API_KEYS) {
            out.writeBoolean(property.key == null);
            if (property.key != null) {
                out.writeLong(property.key.getMostSignificantBits());
                out.writeLong(property.key.getLeastSignificantBits());
            }
        }
        out.writeByte(banFilter.ordinal());
        out.writeByte(addAccountType.ordinal());
        out.writeByte(chatMode.getValue().ordinal());
        out.writeBoolean(senseEnabled.isEnabled());
        out.writeBoolean(onlineNotifications.isEnabled());
        out.writeShort(Changelog.lastRead != null ? Changelog.lastRead.id : 0);
        writeConfigBinds(out);
        for (NumberSetting setting : SCALES) out.writeByte(setting.getValue().intValue());
        writePrefixes(out);
        out.writeInt(Changelog.contentHash);
        writeNickname(out);
        out.writeLong(timestamp);
        com.adjustclient.utilities.Vec2f position = ClickGui.globalConfigurations != null
                ? ClickGui.globalConfigurations.position : null;
        out.writeFloat(position == null ? -1.0F : position.x);
        out.writeFloat(position == null ? -1.0F : position.y);
        writeFriendsPosition(out);
        out.writeByte(cape != null ? cape.id : 0);
        out.writeUTF(cubelifyToken == null ? "" : cubelifyToken);
        out.writeUTF(field32 == null ? "" : field32);
        out.writeUTF(field36 == null ? "" : field36);
        return bytes.toByteArray();
    }

    public void save(AtomicBoolean completed) {
        try {
            if (!localLoaded && LocalStorage.INSTANCE != null
                    && LocalStorage.INSTANCE.readNow("user_configuration.dat") != null) {
                return;
            }
            byte[] data = serialize();
            if (!Arrays.equals(lastSaved, data) && LocalStorage.INSTANCE != null) {
                LocalStorage.INSTANCE.saveNow("user_configuration.dat", data);
                lastSaved = data;
            }
        } catch (Exception error) {
            Logger.log(LogLevel.WARN, true, "Failed to serialize user configuration due to: {}", error);
        } finally {
            if (completed != null) completed.set(true);
        }
    }

    /** Loads persisted user configuration synchronously from {@link LocalStorage} when present. */
    public void loadLocal() {
        if (LocalStorage.INSTANCE == null) return;
        byte[] data = LocalStorage.INSTANCE.readNow("user_configuration.dat");
        localLoaded = true;
        if (data != null && data.length >= 2) {
            load(data);
        }
    }

    @SuppressWarnings("unused")
    private void vendorSave(AtomicBoolean completed) {
        try {
            byte[] data = serialize();
            if (Arrays.equals(lastSaved, data) || !ServiceUtil.userConfigurationWritable) return;
            com.adjustclient.utilities.backend.api.PendingRequest request =
                    new com.adjustclient.utilities.backend.api.request.config.SaveUserConfigurationRequest(data).toPacket(); // endpoint /configuration/saveUserConfiguration
            request.sendAsync(reply -> {
                if (reply.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.OK) {
                    Logger.log(LogLevel.INFO, true, "User configuration was saved.");
                    lastSaved = data;
                } else if (Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated
                        && Adjust.hydraConnection.ready) {
                    Logger.log(LogLevel.WARN, true, "Failed to save user configuration due to: {}", reply);
                }
                if (completed != null) completed.set(true);
            });
            if (completed != null) for (int elapsed = 0; !completed.get() && elapsed <= 4000; elapsed += 5) {
                try { Thread.sleep(5L); } catch (InterruptedException error) { throw new RuntimeException(error); }
            }
        } catch (Exception error) {
            Logger.log(LogLevel.WARN, true, "Failed to serialize user configuration due to: {}", error);
        }
    }

    public void readIntValue(DataInputStream in, int version) throws IOException {
        changelogUnread = version < 7 || in.readInt() != Changelog.contentHash;
    }

    public void writeConfigBinds(DataOutputStream out) throws IOException {
        if (com.adjustclient.managers.config.ConfigurationService.configurationsAvailable
                && com.adjustclient.managers.config.ConfigurationService.configurationReceived
                && Adjust.INSTANCE != null && Adjust.INSTANCE.screenStack != null
                && Adjust.INSTANCE.screenStack.configurations != null) {
            configurationBinds.refresh(Adjust.INSTANCE.screenStack.configurations.configurations);
        }
        out.writeByte(configurationBinds.binds.size());
        for (UserConfigurationData data : configurationBinds.binds) {
            out.writeInt(data.configId);
            VirtualFileSystem.writeKeys(out, data.bind);
        }
    }

    public void read(byte[] data) throws IOException {
        if (data.length < 2) return;
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        int version = in.readUnsignedShort();
        readTabs(in);
        readApiKeys(in, version);
        deserialize(version, in);
        if (version >= 1) readLastVersion(in);
        if (version >= 2) readConfigBinds(in);
        if (version >= 3) readScales(in);
        if (version >= 5) readPrefixes(in);
        readIntValue(in, version);
        if (version >= 9) readNickname(in);
        if (version >= 10) timestamp = in.readLong();
        if (version >= 11) readClickGuiPosition(in);
        if (version >= 12) readFriendsPosition(in);
        if (version >= 13) readCape(in);
        if (version >= 15) readCubelifyToken(in);
        if (version >= 16) readHexToken(in);
        if (version >= 17) readStringValue(in);
    }

    public void load(byte[] data) {
        try { read(data); lastSaved = data; }
        catch (Exception error) {
            error.printStackTrace();
            Logger.log(LogLevel.WARN, true, "Failed to parse received user configuration due to: {}", error);
        }
    }

    public static void setNickname(String nickname) {
        if (Objects.equals((String) field2f84, nickname)) return;
        field2f84 = nickname;
        boolean mapped = StreamerModule.INSTANCE.isEnabled() || nickname != null;
        if ((com.adjustclient.modules.impl.render.streamer.StreamerModule.nameMap != null) != mapped) {
            synchronized (StreamerModule.nameRefreshLock) {
                com.adjustclient.modules.impl.render.streamer.StreamerModule.nameMap = mapped
                        ? com.google.common.collect.HashBiMap.create() : null;
            }
            StreamerModule.INSTANCE.serverNames = null;
        }
    }

    public void writeFriendsPosition(DataOutputStream out) throws IOException {
        com.adjustclient.managers.ScreenStack stack = Adjust.INSTANCE != null ? Adjust.INSTANCE.screenStack : null;
        com.adjustclient.utilities.Vec2f position = stack != null && stack.friendsScreen != null
                ? stack.friendsScreen.position : null;
        out.writeFloat(position == null ? -1.0F : position.x);
        out.writeFloat(position == null ? -1.0F : position.y);
    }

    public void writeNickname(DataOutputStream out) throws IOException {
        out.writeBoolean(field2f84 != null);
        if (field2f84 != null) out.writeUTF((String) field2f84);
    }

    public void writePrefixes(DataOutputStream out) throws IOException {
        for (CommandPrefixProperty property : PREFIXES) out.writeChar(property.getChar());
    }

    public static boolean isUserSetting(Setting<?> setting) {
        if (SCALES.stream().anyMatch(candidate -> candidate == setting)) return true;
        if (API_KEYS.stream().anyMatch(property -> property.setting == setting)) return true;
        return setting == scale || setting == secondaryScale || setting == COMMAND_PREFIX.setting
                || setting == IRC_PREFIX.setting || setting == onlineNotifications || setting == serverNotifications
                || setting == senseEnabled || setting == chatMode;
    }

    public void readStringValue(DataInputStream in) throws IOException {
        String text = in.readUTF();
        if (text.isEmpty()) {
            field36 = null;
            return;
        }
        try {
            UUID.fromString(text);
            field36 = text;
        } catch (IllegalArgumentException ignored) {
            field36 = null;
        }
    }

    public void deserialize(int version, DataInputStream in) throws IOException {
        BanFilter[] filters = BanFilter.values();
        int filter = in.readUnsignedByte();
        banFilter = filters[filter];
        com.adjustclient.core.gui.account.AddAccountType[] types = com.adjustclient.core.gui.account.AddAccountType.values();
        int type = in.readUnsignedByte();
        addAccountType = types[type < types.length ? type : 0];
        if (version >= 4) {
            GlobalChatMode[] modes = GlobalChatMode.values();
            int mode = in.readUnsignedByte();
            chatMode.setMode(modes[mode < modes.length ? mode : 0]);
            if (version >= 6) senseEnabled.setValue(in.readBoolean());
        }
        if (version >= 8) onlineNotifications.setValue(in.readBoolean());
    }

    public void readHexToken(DataInputStream in) throws IOException {
        String text = in.readUTF();
        if (text.matches("[a-f0-9]{64}")) field32 = text;
    }

    public void readApiKeys(DataInputStream in, int version) throws IOException {
        for (ApiKeyProperty property : API_KEYS) {
            if (property == URCHIN_API_KEY && version < 18) continue;
            property.setKey(in.readBoolean() ? null : new UUID(in.readLong(), in.readLong()));
        }
    }

    public void readCubelifyToken(DataInputStream in) throws IOException {
        String text = in.readUTF();
        if (text.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.[0-9a-f]{128}")) cubelifyToken = text;
    }

    public void readCape(DataInputStream in) throws IOException {
        cape = com.adjustclient.managers.user.Cape.BY_ID.getOrDefault(in.readUnsignedByte(), com.adjustclient.managers.user.Cape.NONE);
        if (!HUDModule.changingCape) com.adjustclient.modules.impl.render.hud.HUDModule.cape.setMode(cape);
        Adjust.INSTANCE.screenStack.preferences.cape.codec.value = cape;
    }

    public void readFriendsPosition(DataInputStream in) throws IOException {
        float x = in.readFloat(), y = in.readFloat();
        if (x == -1.0F || y == -1.0F || com.adjustclient.core.rendering.font.Fonts.TAHOMA_12 == null) return;
        com.adjustclient.managers.ScreenStack stack = Adjust.INSTANCE.screenStack;
        if (stack.friendsScreen == null) stack.friendsScreen = new com.adjustclient.core.gui.friends.FriendsScreen();
        if (stack.friendsScreen.position != null) {
            stack.friendsScreen.position.x = x;
            stack.friendsScreen.position.y = y;
        } else {
            stack.friendsScreen.position = new com.adjustclient.utilities.Vec2f(x, y);
        }
    }

    public void readClickGuiPosition(DataInputStream in) throws IOException {
        float x = in.readFloat(), y = in.readFloat();
        if (x != -1.0F && y != -1.0F) {
            if (ClickGui.globalConfigurations.position != null) {
                ClickGui.globalConfigurations.position.x = x;
                ClickGui.globalConfigurations.position.y = y;
            } else {
                ClickGui.globalConfigurations.position = new com.adjustclient.utilities.Vec2f(x, y);
            }
        }
    }

    public void readNickname(DataInputStream in) throws IOException {
        if (in.readBoolean()) {
            String nickname = in.readUTF();
            setNickname(com.adjustclient.core.commands.impl.NickCommand.isValidNickname(nickname) ? nickname : null);
        } else setNickname(null);
    }

    public void readPrefixes(DataInputStream in) throws IOException {
        for (CommandPrefixProperty property : PREFIXES) property.setChar(in.readChar());
    }

    public void readScales(DataInputStream in) throws IOException {
        for (NumberSetting setting : SCALES) {
            int value = in.readUnsignedByte();
            int step = setting.getStep().intValue();
            int rounded = step * Math.round((float) value / step);
            setting.setValue(Math.max(setting.getMin().intValue(), Math.min(setting.getMax().intValue(), rounded)));
        }
    }

    public void readConfigBinds(DataInputStream in) throws IOException {
        int count = in.readUnsignedByte();
        for (int i = 0; i < count; i++) {
            UserConfigurationData data = configurationBinds.getOrCreate(in.readInt());
            data.bind.clearKeys();
            VirtualFileSystem.readKeys(data.bind, in);
        }
    }

    public void readLastVersion(DataInputStream in) {
        try {
            int id = in.readUnsignedShort();
            com.adjustclient.core.changelog.ChangelogLink version = Changelog.versions.stream()
                    .filter(candidate -> candidate.id == id).findFirst().orElse(null);
            if (version == null) Logger.log(LogLevel.WARN, true, "No version known by id {}", id);
            else com.adjustclient.core.changelog.Changelog.updateUnread(version);
        } catch (Exception error) {
            Logger.log(LogLevel.WARN, true, "Failed to read last launched version: {}", error);
        }
    }

    public void readTabs(DataInputStream in) throws IOException {
        int count = in.readUnsignedByte();
        if (com.adjustclient.core.rendering.font.Fonts.TAHOMA == null
                || com.adjustclient.core.rendering.font.Fonts.TAHOMA_12 == null
                || com.adjustclient.core.rendering.font.Fonts.ICONS_13 == null) {
            for (int i = 0; i < count; i++) {
                in.readInt();
                in.readInt();
                in.readInt();
            }
            return;
        }
        com.adjustclient.core.gui.clickgui.ClickGuiScreen screen = com.adjustclient.core.gui.clickgui.ClickGuiScreen.instance();
        if (count == 0) {
            screen.layoutDirty = true;
            return;
        }
        int[] ids = new int[count];
        float[] xs = new float[count];
        float[] ys = new float[count];
        boolean valid = true;
        for (int i = 0; i < count; i++) {
            ids[i] = in.readInt();
            xs[i] = Float.intBitsToFloat(in.readInt());
            ys[i] = Float.intBitsToFloat(in.readInt());
            if (!Float.isFinite(xs[i]) || !Float.isFinite(ys[i]) || xs[i] < 0.0F || ys[i] < 0.0F) {
                valid = false;
            }
        }
        if (!valid) {
            screen.initialized = false;
            screen.dragMoved = false;
            screen.layoutDirty = true;
            return;
        }
        for (int i = 0; i < count; i++) {
            for (com.adjustclient.core.gui.clickgui.ClickGuiTab tab : screen.tabs) {
                if (tab.id == ids[i]) {
                    tab.x = xs[i];
                    tab.y = ys[i];
                    break;
                }
            }
        }
        screen.initialized = true;
        screen.dragMoved = true;
        screen.layoutDirty = true;
    }
}
