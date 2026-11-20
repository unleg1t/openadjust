package com.adjustclient.utilities.vfs;

import com.adjustclient.Adjust;
import com.adjustclient.core.bind.Bind;
import com.adjustclient.core.bind.BindKey;
import com.adjustclient.core.bind.KeyboardKey;
import com.adjustclient.core.bind.MouseKey;
import com.adjustclient.core.bind.ScrollKey;
import com.adjustclient.core.commands.impl.ConfigCommand;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.Listener;
import com.adjustclient.core.events.SettingChangeEvent;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.modules.Module;
import com.adjustclient.modules.ModuleSerializer;
import com.adjustclient.modules.impl.combat.builder.BuilderModule;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.settings.property.KeybindManager;
import com.adjustclient.managers.config.Configuration;
import com.adjustclient.managers.config.ConfigurationService;
import com.adjustclient.managers.config.UserConfigurationData;
import com.adjustclient.managers.config.user.UserConfiguration;
import com.adjustclient.managers.util.ServiceUtil;
import com.adjustclient.core.settings.Setting;
import com.adjustclient.core.gui.clickgui.ClickGuiScreen;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.utilities.ByteUtil;
import com.adjustclient.utilities.HashUtil;
import com.adjustclient.utilities.Timer;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.client.Minecraft;
import com.adjustclient.utilities.script.engine.ScriptManager;

public class VirtualFileSystem implements Listener {
    public static VirtualFileSystem INSTANCE;
    public final ArrayList<FeatureToggle> deferredToggles = new ArrayList<>();
    public Timer dirtyTimer;
    public Timer userSaveTimer = new Timer();
    public Timer lastSaveTimer = new Timer();
    public boolean loading;
    public boolean snapshotted;
    public Object field24;

    public void autoSave() {
        if (!ConfigurationService.configurationsAvailable || !ConfigurationService.configurationReceived) return;
        if (dirtyTimer != null && (dirtyTimer.hasElapsed(60000L) || lastSaveTimer.hasElapsed(360000L)))
            save(null, new Runnable[0]);
        if (userSaveTimer.hasElapsed(120000L)) {
            Adjust.INSTANCE.screenStack.localStorage.save(null);
            userSaveTimer.reset();
        }
    }

    @SuppressWarnings("unused")
    private void vendorAutoSave() {
        if (Adjust.hydraConnection == null || !Adjust.hydraConnection.authenticated
                || !Adjust.hydraConnection.ready
                || !ConfigurationService.configurationsAvailable || !ConfigurationService.configurationReceived) return;
        if (dirtyTimer != null && (dirtyTimer.hasElapsed(60000L) || lastSaveTimer.hasElapsed(360000L)))
            save(null, new Runnable[0]);
        if (userSaveTimer.hasElapsed(120000L)) {
            Adjust.INSTANCE.screenStack.localStorage.save(null);
            userSaveTimer.reset();
        }
    }

    public void writeScripts(DataOutputStream output) throws IOException {
        Objects.requireNonNull(ScriptManager.INSTANCE.scripts);
        output.writeShort(0);
    }

    public void applyToggles(ArrayList<FeatureToggle> toggles) {
        for (FeatureToggle toggle : toggles) if (!toggle.toggled()) toggle.moduleFeature().toggle(false);
        for (FeatureToggle toggle : toggles) if (toggle.toggled()) toggle.moduleFeature().toggle(true);
    }

    public void serialize(Module value, DataOutputStream output) throws IOException {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream data = new DataOutputStream(bytes)) {
                new ModuleSerializer(value, false).write(data);
            }
            byte[] payload = bytes.toByteArray();
            output.writeInt(HashUtil.hash(value.getName()));
            ByteUtil.writeVarInt(output, payload.length);
            output.write(payload);
        } catch (Exception error) {
            System.out.println("Failed to serialize something!");
            error.printStackTrace();
            output.writeInt(0);
        }
    }

    public void readScripts(LoadOptions options, DataInputStream input) throws IOException {
        int count = input.readUnsignedShort();
        for (int i = 0; i < count; i++) {
            int hash = input.readInt();
            int length = input.readInt();
            if (length > 0) {
                byte[] payload = new byte[length];
                input.readFully(payload);
            }
            Objects.requireNonNull(ScriptManager.INSTANCE.scripts);
        }
        Objects.requireNonNull(ScriptManager.INSTANCE);
    }

    public static void writeKeys(DataOutputStream output, Bind bind) throws IOException {
        List<BindKey> keys = new ArrayList<>(bind.keys);
        int count = Math.min(8, keys.size());
        output.writeByte(count);
        int keyboardMask = 0;
        for (int i = 0; i < count; i++) if (keys.get(i).getClass() == KeyboardKey.class) keyboardMask |= 128 >> i;
        output.writeByte(keyboardMask);
        for (int i = 0; i < count; i++) {
            BindKey key = keys.get(i);
            if (key instanceof KeyboardKey) ByteUtil.writeVarInt(output, ((KeyboardKey) key).keyCode);
            else if (key instanceof MouseKey) ByteUtil.writeVarInt(output, ((MouseKey) key).button);
            else if (key instanceof ScrollKey) ByteUtil.writeVarInt(output, ((ScrollKey) key).keyCode());
            else throw new RuntimeException("Unknown type.");
        }
    }

    public void save(AtomicBoolean completed, Runnable... callbacks) {
        dirtyTimer = null;
        lastSaveTimer.reset();
        ConfigurationService configurations = Adjust.INSTANCE.screenStack.configurations;
        if (configurations == null || configurations.selected == null) {
            markDirty();
            return;
        }
        byte[] data = emptyConfig();
        if (data == null) {
            ServiceUtil.handleSerializationFailure();
            return;
        }
        Timer timer = new Timer();
        configurations.save(configurations.selected, data,
                status -> onSaveReply(configurations.selected, timer, callbacks, completed, status));
        if (completed != null) {
            for (int waited = 0; !completed.get() && waited <= 4000; waited += 5) {
                try { Thread.sleep(5); }
                catch (InterruptedException error) { throw new RuntimeException(error); }
            }
        }
    }

    public boolean load(byte[] config, LoadOptions options) {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            Logger.log(LogLevel.ERROR, true, "Configuration was loaded from a separate thread.");
            throw new RuntimeException();
        }
        if (!options.bedDefenseOnly) {
            if (!snapshotted) {
                snapshotState();
                snapshotted = true;
            } else HUDModule.startColorTransition(HUDModule.colorSetting.getColor());
        }
        loading = true;
        boolean loaded = readConfig(config, options);
        loading = false;
        if (!options.bedDefenseOnly) {
            if (ClickGuiScreen.INSTANCE != null) ClickGuiScreen.INSTANCE.resetModifiers();
            dirtyTimer = null;
        }
        return loaded;
    }

    public boolean deserialize(LoadOptions options, DataInputStream input, ArrayList<FeatureToggle> toggles) throws IOException {
        int hash = input.readInt();
        if (hash == 0) return false;
        byte[] payload = new byte[ByteUtil.readVarInt(input)];
        ByteUtil.readFully(input, payload);
        if (!options.modules) return false;
        for (Module module : Adjust.INSTANCE.moduleManager.view) {
            if (HashUtil.hash(module.getName()) != hash) continue;
            if (options.bedDefenseOnly) {
                if (!(module instanceof BuilderModule)) return false;
                BuilderModule builder = (BuilderModule) module;
                ModuleSerializer codec = new ModuleSerializer(module, false);
                byte[] setting = codec.getSettingData(new DataInputStream(new ByteArrayInputStream(payload)), builder.bedDefense.defenses);
                if (setting != null) {
                    builder.bedDefense.defenses.read(new DataInputStream(new ByteArrayInputStream(setting)));
                    return true;
                }
                return false;
            }
            try (DataInputStream data = new DataInputStream(new ByteArrayInputStream(payload))) {
                try {
                    Boolean enabled = new ModuleSerializer(module, options.partial).read(data);
                    if (enabled != null) {
                        if (module.field31) {
                            Minecraft.getMinecraft().addScheduledTask(() -> module.toggle(enabled));
                        }
                        toggles.add(new FeatureToggle(module, enabled));
                    }
                } catch (Exception error) {
                    error.printStackTrace();
                    System.out.println("failed to deserialize: " + module.getName());
                }
            } catch (Exception error) {
                System.out.println("failed to deserialize (x2): " + module.getName());
            }
            return false;
        }
        return false;
    }

    public void onSaveReply(com.adjustclient.managers.config.ConfigurationManager config, Timer timer, Runnable[] callbacks, AtomicBoolean completed, int status) {
        if (status == 262843055) {
            Logger.log(LogLevel.INFO, true, "Configuration '{}' automatically saved in {}ms", config.name, System.currentTimeMillis() - timer.lastMs);
        } else Logger.log(LogLevel.ERROR, true, "Failed to automatically save configuration '{}'", config.name);
        for (Runnable callback : callbacks) callback.run();
        if (completed != null) completed.set(true);
    }

    public byte[] emptyConfig() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(4096);
            try (GZIPOutputStream gzip = new GZIPOutputStream(bytes); DataOutputStream output = new DataOutputStream(gzip)) {
                output.writeShort(204);
                output.writeShort(Adjust.INSTANCE.moduleManager.view.size());
                for (Module module : Adjust.INSTANCE.moduleManager.view) serialize(module, output);
                writeKeybindings(output);
                writeProperties(output);
                writeScripts(output);
            }
            return bytes.toByteArray();
        } catch (Exception error) {
            error.printStackTrace();
            NotificationBuilder notice = new NotificationBuilder("Virtual File System");
            notice.addText("Serialization of your file system failed. ", (int) 4289219403L);
            notice.addText("Please report this bug with a console log attached!", (int) 4290690237L);
            notice.lifetimeSteps = 240;
            notice.show();
            return null;
        }
    }

    public void writeProperties(DataOutputStream output) throws IOException {
        new ModuleSerializer((Module) Adjust.INSTANCE.field28, false).write(output);
    }

    public void readProperties(LoadOptions options, DataInputStream input) {
        if (!options.properties) return;
        try {
            Module module = (Module) Adjust.INSTANCE.field28;
            Boolean enabled = new ModuleSerializer(module, false).read(input);
            if (enabled != null) module.toggle(enabled);
        } catch (Exception error) {
            System.out.println("Failed to deserialize property manager");
            error.printStackTrace();
        }
    }

    public boolean readConfig(byte[] config, LoadOptions options) {
        if (config.length == 0) return !options.bedDefenseOnly;
        if (Arrays.equals(config, new byte[] {'N', 'E', 'W', '!'})) {
            if (options.bedDefenseOnly) return false;
            if (options.keybinds) ConfigCommand.resetBindings();
            if (options.modules) {
                ConfigCommand.resetModules();
                ConfigCommand.resetAllSettings();
            }
            return true;
        }
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(config)); DataInputStream input = new DataInputStream(gzip)) {
            try {
                int version = input.readUnsignedShort();
                if (version != 203 && version != 204) throw new IOException("File system version changed");
                int count = input.readUnsignedShort();
                if (count == 3083) throw new IOException("This config is for Fabric");
                ArrayList<FeatureToggle> toggles = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    boolean applied = deserialize(options, input, toggles);
                    if (options.bedDefenseOnly && applied) return true;
                }
                if (options.bedDefenseOnly) return false;
                if (Minecraft.getMinecraft().theWorld != null) applyToggles(toggles);
                else {
                    deferredToggles.clear();
                    deferredToggles.addAll(toggles);
                }
                readKeybindings(options, input);
                readProperties(options, input);
                if (version == 204) readScripts(options, input);
                return true;
            } catch (Exception error) {
                error.printStackTrace();
                return false;
            }
        } catch (IOException error) {
            error.printStackTrace();
            return false;
        }
    }

    public void writeKeybindings(DataOutputStream output) throws IOException {
        ArrayList<Bind> selected = new ArrayList<>();
        for (Bind bind : Adjust.INSTANCE.bindManager.view) {
            if (bind.field29 && bind.mutable && !bind.keys.isEmpty()) selected.add(bind);
        }
        byte[] payload;
        if (selected.isEmpty()) payload = new byte[0];
        else {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream data = new DataOutputStream(bytes)) {
                data.writeByte(1);
                ByteUtil.writeVarInt(data, selected.size());
                for (Bind bind : selected) {
                    data.writeInt(bind.id);
                    writeKeys(data, bind);
                }
            }
            payload = bytes.toByteArray();
        }
        ByteUtil.writeVarInt(output, payload.length);
        output.write(payload);
    }

    public void readKeybindings(LoadOptions options, DataInputStream input) throws IOException {
        UserConfigurationData[] preserved = UserConfiguration.INSTANCE.configurationBinds.binds.toArray(
                length -> new UserConfigurationData[length]);
        for (UserConfigurationData data : preserved) data.bind.mutable = false;
        if (options.keybinds) options.keybindManager.clearKeys();
        for (UserConfigurationData data : preserved) data.bind.mutable = true;
        int length = ByteUtil.readVarInt(input);
        if (length == 0) return;
        byte[] payload = new byte[length];
        ByteUtil.readFully(input, payload);
        if (!options.keybinds) return;
        try (DataInputStream data = new DataInputStream(new ByteArrayInputStream(payload))) {
            if (data.readByte() != 1) throw new RuntimeException("Bad version");
            int count = ByteUtil.readVarInt(data);
            for (int i = 0; i < count; i++) {
                Bind bind = options.keybindManager.getByHash(data.readInt());
                if (bind != null && !bind.field29) bind = null;
                readKeys(bind, data);
            }
        } catch (Exception error) {
            Logger.log(LogLevel.ERROR, true, "Failed to deserialize keybindings: {}", error);
        }
    }

    public static void snapshotState() {
        for (Module module : Adjust.INSTANCE.moduleManager.view) module.savedFlags = module.flags;
        for (Bind bind : Adjust.INSTANCE.bindManager.view) bind.field12 = new HashSet<>(bind.keys);
    }

    public void applyDeferredToggles() {
        if (deferredToggles.isEmpty()) return;
        ArrayList<FeatureToggle> toggles = new ArrayList<>(deferredToggles);
        deferredToggles.clear();
        boolean prevLoading = loading;
        loading = true;
        try {
            applyToggles(toggles);
        } finally {
            loading = prevLoading;
        }
    }

    @EventHandler
    public void onSettingChange(SettingChangeEvent event) {
        // Includes both property collections, API-key properties and the individually excluded user settings.
        if (!UserConfiguration.isUserSetting(event.setting)) markDirty();
    }

    public static void readKeys(Bind bind, DataInputStream input) throws IOException {
        int count = input.readUnsignedByte();
        if (count > 8) throw new RuntimeException("Bad key count: " + count);
        int keyboardMask = input.readUnsignedByte();
        for (int i = 0; i < count; i++) {
            int code = ByteUtil.readVarInt(input);
            if ((keyboardMask & (128 >> i)) != 0) {
                if (bind != null) bind.setKey(new KeyboardKey(code));
            } else if (bind != null) {
                if (code == 1069 || code == 1070) bind.setKey(new ScrollKey(code));
                else {
                    Supplier<MouseKey> factory = () -> new MouseKey(code);
                    bind.setKey(factory.get());
                }
            }
        }
    }

    public void markDirty() {
        if (loading) return;
        if (ConfigurationService.configurationsAvailable && ConfigurationService.configurationReceived && ServiceUtil.pendingTasksReady) {
            if (dirtyTimer != null) dirtyTimer.reset();
            else {
                lastSaveTimer.reset();
                dirtyTimer = new Timer();
            }
        }
    }
}
