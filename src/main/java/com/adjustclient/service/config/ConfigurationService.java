package com.adjustclient.service.config;

import com.adjustclient.Adjust;
import com.adjustclient.logging.Logger;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.service.BackendMessageHandler;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.service.storage.LocalStorage;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.vfs.LoadOptions;
import com.adjustclient.vfs.VirtualFileSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.service.util.ServiceUtil;

public class ConfigurationService implements BackendMessageHandler {
    public static boolean configurationsAvailable;
    public static boolean configurationReceived;
    public static boolean corruptFile;
    public final ArrayList<Configuration> pending = new ArrayList<>();
    public final ArrayList<ConfigurationManager> configurations = new ArrayList<>();
    public ConfigurationManager selected;
    public int nextRequestId = -9118778;
    public int catalogHash;
    public int challenge;
    public static byte[] dataKey;
    public static ArrayList<com.adjustclient.backend.auth.DeferredConfigurationReply> deferredReplies;
    public Object field28;
    private static final String CATALOG_FILE = "configurations.dat";
    private static final String IMPORT_PREFIX = "import_";
    private static final String IMPORT_SUFFIX = ".dat";
    private static final int CATALOG_MAGIC = 0x41434647;
    private static final int CATALOG_VERSION = 1;
    private int nextLocalId = 1;
    private boolean localInitialized;
    private long lastSyncCheckMs;

    /** Returns whether {@code name} satisfies local configuration naming rules (1..16 chars, no leading/trailing space). */
    public static boolean isValidConfigName(String name) {
        return name != null && !name.isEmpty() && name.length() < 17
                && !name.startsWith(" ") && !name.endsWith(" ")
                && name.matches("^[a-zA-Z0-9 !@#$%^&~*_\\-=+,./\"';:]*$");
    }

    private static String configFileName(int id) {
        return "config_" + id + ".dat";
    }

    /** Loads the local configuration catalog and active profile from {@link LocalStorage}, creating a Default profile if absent. */
    public void initLocal() {
        if (localInitialized && !configurations.isEmpty()) {
            configurationsAvailable = true;
            configurationReceived = true;
            return;
        }
        configurations.clear();
        selected = null;
        nextLocalId = 1;
        int selectedId = 0;
        byte[] catalogBytes = LocalStorage.INSTANCE != null ? LocalStorage.INSTANCE.readNow(CATALOG_FILE) : null;
        if (catalogBytes != null && catalogBytes.length > 0) {
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(catalogBytes))) {
                int magic = in.readInt();
                int version = in.readInt();
                if (magic == CATALOG_MAGIC && version == CATALOG_VERSION) {
                    selectedId = in.readInt();
                    int savedNextId = in.readInt();
                    int count = in.readInt();
                    int maxId = 0;
                    for (int i = 0; i < count; i++) {
                        int id = in.readInt();
                        String name = in.readUTF();
                        long created = in.readLong();
                        long lastUsed = in.readLong();
                        if (id <= 0 || !isValidConfigName(name)) continue;
                        ConfigurationManager config = new ConfigurationManager();
                        config.id = id;
                        config.name = name;
                        config.created = created;
                        config.lastUsed = lastUsed;
                        configurations.add(config);
                        if (id > maxId) maxId = id;
                    }
                    nextLocalId = Math.max(savedNextId, maxId + 1);
                }
            } catch (Exception exception) {
                Logger.log(LogLevel.WARN, true, "Failed to read local configuration catalog: {}", exception);
                configurations.clear();
                nextLocalId = 1;
                selectedId = 0;
            }
        }

        int importedSelectedId = consumePendingImports();
        if (importedSelectedId > 0) {
            selectedId = importedSelectedId;
        }

        if (configurations.isEmpty()) {
            long now = System.currentTimeMillis();
            ConfigurationManager def = new ConfigurationManager();
            def.id = nextLocalId++;
            def.name = "Default";
            def.created = now;
            def.lastUsed = now;
            configurations.add(def);
            selected = def;
            byte[] initial = VirtualFileSystem.INSTANCE != null ? VirtualFileSystem.INSTANCE.emptyConfig() : null;
            if (initial != null && LocalStorage.INSTANCE != null) {
                LocalStorage.INSTANCE.saveNow(configFileName(def.id), initial);
            }
            saveCatalogLocal();
        } else {
            selected = find(selectedId);
            if (selected == null) selected = configurations.get(0);
            if (selected != null && VirtualFileSystem.INSTANCE != null && LocalStorage.INSTANCE != null) {
                byte[] data = LocalStorage.INSTANCE.readNow(configFileName(selected.id));
                if (data != null && data.length > 0) {
                    LoadOptions options = new LoadOptions();
                    options.keybindManager = Adjust.INSTANCE.bindManager;
                    options.modules = true;
                    options.keybinds = true;
                    options.properties = true;
                    ConfigurationManager startupConfig = selected;
                    Minecraft mc = Minecraft.getMinecraft();
                    if (mc == null || mc.isCallingFromMinecraftThread()) {
                        applyStartupConfig(data, options, startupConfig);
                    } else {
                        mc.addScheduledTask(() -> applyStartupConfig(data, options, startupConfig));
                    }
                } else {
                    byte[] initial = VirtualFileSystem.INSTANCE.emptyConfig();
                    if (initial != null) LocalStorage.INSTANCE.saveNow(configFileName(selected.id), initial);
                }
            }
        }

        if (UserConfiguration.INSTANCE != null) {
            UserConfiguration.INSTANCE.configurationBinds.refresh(configurations);
        }
        updateCatalogHash();
        if (importedSelectedId > 0) {
            saveCatalogLocal();
        }
        configurationsAvailable = true;
        configurationReceived = true;
        localInitialized = true;
    }

    /** Imports any staged {@code import_<Name>.dat} payloads in {@link LocalStorage#directory}, returning the last imported id. */
    private int consumePendingImports() {
        if (LocalStorage.INSTANCE == null || LocalStorage.INSTANCE.directory == null) return 0;
        java.io.File[] files = LocalStorage.INSTANCE.directory.listFiles();
        if (files == null) return 0;
        int lastImportedId = 0;
        for (java.io.File file : files) {
            String fileName = file.getName();
            if (!file.isFile() || !fileName.startsWith(IMPORT_PREFIX) || !fileName.endsWith(IMPORT_SUFFIX)) continue;
            String configName = fileName.substring(IMPORT_PREFIX.length(), fileName.length() - IMPORT_SUFFIX.length());
            if (!isValidConfigName(configName)) continue;
            byte[] payload = LocalStorage.INSTANCE.readNow(fileName);
            if (payload == null || payload.length == 0) continue;
            ConfigurationManager target = null;
            for (ConfigurationManager existing : configurations) {
                if (existing.name.equalsIgnoreCase(configName)) {
                    target = existing;
                    break;
                }
            }
            long now = System.currentTimeMillis();
            if (target == null) {
                target = new ConfigurationManager();
                target.id = nextLocalId++;
                target.name = configName;
                target.created = now;
                target.lastUsed = now;
                configurations.add(target);
            } else {
                target.lastUsed = now;
            }
            LocalStorage.INSTANCE.saveNow(configFileName(target.id), payload);
            LocalStorage.INSTANCE.deleteNow(fileName);
            lastImportedId = target.id;
        }
        return lastImportedId;
    }

    /** Polls {@link LocalStorage} at most once per second for externally added catalog entries or staged imports. */
    public void pollExternalChanges() {
        long now = System.currentTimeMillis();
        if (now - lastSyncCheckMs < 1000L) return;
        lastSyncCheckMs = now;
        if (!localInitialized || LocalStorage.INSTANCE == null) return;
        boolean changed = mergeCatalogFromDisk();
        int importedId = consumePendingImports();
        if (importedId > 0) changed = true;
        if (changed) {
            if (UserConfiguration.INSTANCE != null) {
                UserConfiguration.INSTANCE.configurationBinds.refresh(configurations);
            }
            updateCatalogHash();
            saveCatalogLocal();
        }
    }

    private boolean mergeCatalogFromDisk() {
        if (LocalStorage.INSTANCE == null) return false;
        byte[] catalogBytes = LocalStorage.INSTANCE.readNow(CATALOG_FILE);
        if (catalogBytes == null || catalogBytes.length == 0) return false;
        boolean added = false;
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(catalogBytes))) {
            int magic = in.readInt();
            int version = in.readInt();
            if (magic == CATALOG_MAGIC && version == CATALOG_VERSION) {
                in.readInt();
                int savedNextId = in.readInt();
                int count = in.readInt();
                if (savedNextId > nextLocalId) nextLocalId = savedNextId;
                for (int i = 0; i < count; i++) {
                    int id = in.readInt();
                    String name = in.readUTF();
                    long created = in.readLong();
                    long lastUsed = in.readLong();
                    if (id <= 0 || !isValidConfigName(name)) continue;
                    if (find(id) != null || findByNameRaw(name) != null) continue;
                    java.io.File payloadFile = new java.io.File(LocalStorage.INSTANCE.directory, configFileName(id));
                    if (!payloadFile.isFile()) continue;
                    ConfigurationManager config = new ConfigurationManager();
                    config.id = id;
                    config.name = name;
                    config.created = created;
                    config.lastUsed = lastUsed;
                    configurations.add(config);
                    if (id >= nextLocalId) nextLocalId = id + 1;
                    added = true;
                }
            }
        } catch (Exception ignored) {
        }
        return added;
    }

    private ConfigurationManager findByNameRaw(String name) {
        for (ConfigurationManager config : configurations) {
            if (config.name.equalsIgnoreCase(name)) return config;
        }
        return null;
    }

    private void applyStartupConfig(byte[] data, LoadOptions options, ConfigurationManager config) {
        if (VirtualFileSystem.INSTANCE != null && !VirtualFileSystem.INSTANCE.load(data, options)) {
            Logger.log(LogLevel.WARN, true, "Failed to deserialize local configuration '{}'", config.name);
        }
    }

    private void saveCatalogLocal() {
        if (LocalStorage.INSTANCE == null) return;
        mergeCatalogFromDisk();
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(bytes)) {
                out.writeInt(CATALOG_MAGIC);
                out.writeInt(CATALOG_VERSION);
                out.writeInt(selected != null ? selected.id : 0);
                out.writeInt(nextLocalId);
                out.writeInt(configurations.size());
                for (ConfigurationManager config : configurations) {
                    out.writeInt(config.id);
                    out.writeUTF(config.name != null ? config.name : "");
                    out.writeLong(config.created);
                    out.writeLong(config.lastUsed);
                }
            }
            LocalStorage.INSTANCE.saveNow(CATALOG_FILE, bytes.toByteArray());
        } catch (IOException exception) {
            Logger.log(LogLevel.WARN, true, "Failed to save local configuration catalog: {}", exception);
        }
    }

    /** Creates and persists a new local configuration with {@code name} and {@code data} (or current config if null). */
    public ConfigurationManager createLocal(String name, byte[] data) {
        if (!localInitialized) initLocal();
        if (!isValidConfigName(name)) return null;
        for (ConfigurationManager existing : configurations) {
            if (existing.name.equalsIgnoreCase(name)) return null;
        }
        byte[] payload = data != null ? data : (VirtualFileSystem.INSTANCE != null ? VirtualFileSystem.INSTANCE.emptyConfig() : null);
        if (payload == null) return null;
        ConfigurationManager config = new ConfigurationManager();
        config.id = nextLocalId++;
        config.name = name;
        config.created = System.currentTimeMillis();
        config.lastUsed = 0L;
        if (LocalStorage.INSTANCE != null) {
            LocalStorage.INSTANCE.saveNow(configFileName(config.id), payload);
        }
        configurations.add(config);
        if (UserConfiguration.INSTANCE != null) {
            UserConfiguration.INSTANCE.configurationBinds.refresh(configurations);
        }
        updateCatalogHash();
        saveCatalogLocal();
        return config;
    }

    /** Deletes a non-selected local configuration and its stored payload file. */
    public boolean deleteLocal(ConfigurationManager config) {
        if (config == null || !configurations.contains(config) || config == selected || configurations.size() <= 1) {
            return false;
        }
        configurations.remove(config);
        if (LocalStorage.INSTANCE != null) {
            LocalStorage.INSTANCE.deleteNow(configFileName(config.id));
        }
        if (UserConfiguration.INSTANCE != null) {
            UserConfiguration.INSTANCE.configurationBinds.refresh(configurations);
        }
        updateCatalogHash();
        saveCatalogLocal();
        return true;
    }

    /** Renames an existing local configuration if {@code newName} is valid and unique. */
    public boolean renameLocal(ConfigurationManager config, String newName) {
        if (config == null || !configurations.contains(config) || !isValidConfigName(newName)) return false;
        for (ConfigurationManager other : configurations) {
            if (other != config && other.name.equalsIgnoreCase(newName)) return false;
        }
        config.name = newName;
        if (UserConfiguration.INSTANCE != null) {
            UserConfiguration.INSTANCE.configurationBinds.refresh(configurations);
        }
        updateCatalogHash();
        saveCatalogLocal();
        return true;
    }

    public void onDisconnect() {
        nextRequestId = -9118778;
        pending.clear();
        deferredReplies = null;
        if (dataKey != null) dataKey = null;
    }

    @SuppressWarnings("unused")
    private void vendorOnDisconnect() {
        nextRequestId = -9118778;
        pending.clear();
        deferredReplies = null;
        configurations.clear();
        if (dataKey != null) dataKey = null;
    }

    public ConfigurationManager find(int id) {
        for (ConfigurationManager config : configurations) if (config.id == id) return config;
        return null;
    }

    public ConfigurationManager findByName(String name) {
        pollExternalChanges();
        for (ConfigurationManager config : configurations) if (config.name.equalsIgnoreCase(name)) return config;
        for (ConfigurationManager config : configurations)
            if (config.name.replaceAll(" ", "").equalsIgnoreCase(name)) return config;
        return null;
    }

    public void select(ConfigurationManager config) {
        if (config == null) return;
        selected = config;
        config.lastUsed = System.currentTimeMillis();
        saveCatalogLocal();
    }

    @SuppressWarnings("unused")
    private void vendorSelect(ConfigurationManager config) {
        selected = config;
        config.lastUsed = System.currentTimeMillis();
        com.adjustclient.service.util.ServiceUtil.sendToBackend(ByteBuffer.allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN).putInt(24).order(ByteOrder.BIG_ENDIAN).putInt(config.id).array());
    }

    public void requestData(ConfigurationManager config, ConfigurationLoadCallback callback) {
        if (callback == null) return;
        if (config == null || LocalStorage.INSTANCE == null) {
            callback.onReply(152532851, null);
            return;
        }
        byte[] data = LocalStorage.INSTANCE.readNow(configFileName(config.id));
        if (data == null) callback.onReply(152532851, null);
        else callback.onReply(193435832, data);
    }

    @SuppressWarnings("unused")
    private void vendorRequestData(ConfigurationManager config, ConfigurationLoadCallback callback) {
        pending.add(new Configuration(nextRequestId, 2, callback));
        com.adjustclient.service.util.ServiceUtil.sendToBackend(ByteBuffer.allocate(12)
                .order(ByteOrder.LITTLE_ENDIAN).putInt(23).order(ByteOrder.BIG_ENDIAN)
                .putInt(nextRequestId).putInt(config.id).array());
        nextRequestId++;
    }

    public static void handleCorruptFile(byte[] data, ConfigurationManager config) {
        LoadOptions options = new LoadOptions();
        options.keybindManager = Adjust.INSTANCE.bindManager;
        options.modules = true;
        options.keybinds = true;
        options.properties = true;
        if (!VirtualFileSystem.INSTANCE.load(data == null ? new byte[0] : data, options)) {
            NotificationBuilder notice = new NotificationBuilder("Configuration");
            notice.addText("Your configuration file ", (int) 4290690237L);
            notice.append(config.name);
            notice.addText(" was corrupted and has not been loaded.", (int) 4290690237L);
            notice.lifetimeSteps = 105;
            notice.show();
        }
        corruptFile = true;
    }

    public void loadFirstOrExit(ConfigurationManager config, int status, byte[] bytes) {
        if (status != 193435832) {
            Logger.log(LogLevel.ERROR, true, "Unable to load first configuration file: {}", status);
            return;
        }
        Minecraft.getMinecraft().addScheduledTask(() -> {
            handleCorruptFile(bytes, config);
        });
    }

    public void save(ConfigurationManager config, byte[] data, ConfigurationListener callback) {
        if (config == null || data == null || LocalStorage.INSTANCE == null) {
            if (callback != null) callback.onReply(-1073738824);
            return;
        }
        LocalStorage.INSTANCE.saveNow(configFileName(config.id), data);
        config.lastUsed = System.currentTimeMillis();
        saveCatalogLocal();
        if (callback != null) callback.onReply(262843055);
    }

    @SuppressWarnings("unused")
    private void vendorSave(ConfigurationManager config, byte[] data, ConfigurationListener callback) {
        pending.add(new Configuration(nextRequestId, 1, callback));
        byte[] packet = com.adjustclient.backend.transport.HydraPacketEncoder.encodeSendConfiguration(nextRequestId, config.id, data);
        com.adjustclient.service.util.ServiceUtil.sendToBackend(packet);
        nextRequestId++;
    }

    public void receiveData(int requestId, int status, int hash, byte[] bytes) {
        if (dataKey == null) return;
        configurationReceived = true;
        for (Configuration request : new ArrayList<>(pending)) {
            if (request.requestId == requestId && request.kind == 2) {
                deliverData(request, hash, bytes, status);
                pending.remove(request);
                break;
            }
        }
    }

    @SuppressWarnings("unused")
    private void vendorReceiveData(int requestId, int status, int hash, byte[] bytes) {
        if (dataKey == null) {
            if (deferredReplies == null) deferredReplies = new ArrayList<>();
            deferredReplies.add(new com.adjustclient.backend.auth.DeferredConfigurationReply(requestId, status, hash, bytes));
            return;
        }
        configurationReceived = true;
        for (Configuration request : new ArrayList<>(pending)) {
            if (request.requestId == requestId && request.kind == 2) {
                deliverData(request, hash, bytes, status);
                pending.remove(request);
                break;
            }
        }
    }

    public void deliverData(Configuration request, int expectedHash, byte[] bytes, int status) {
        byte[] data = bytes == null ? new byte[0] : bytes.clone();
        int state = data.length == 0 ? 0 : dataKey[0];
        for (int i = 0; i < data.length; i++) {
            state += 22;
            if (i > 0 && i % 3 == 0) state += dataKey[(3 * i) % 32];
            int mixed = (state + 32 * i - 58) ^ (data[i] + ((i & 127) ^ (state << 7)));
            data[i] = (byte) (state + (state ^ (byte) mixed));
        }
        int hash = data.length == 0 ? 0 : 354;
        for (byte value : data) hash = 33 * hash + value + (value >= 0 ? 1 : 0);
        if (hash != expectedHash) {
            data = null;
            status = 7434855;
        }
        ((ConfigurationLoadCallback) request.callback).onReply(status, data);
    }

    public void loadAll(String[] names, int[] ids, int integrity, long[] created, long[] lastUsed) {
        configurations.clear();
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == 0) continue;
            ConfigurationManager config = new ConfigurationManager();
            config.name = names[i];
            config.created = created[i];
            config.lastUsed = lastUsed[i];
            config.id = ids[i];
            configurations.add(config);
        }
        if (configurations.isEmpty()) {
            Logger.log(LogLevel.ERROR, true, "Unable to load configuration files.");
            return;
        }
        com.adjustclient.service.config.user.UserConfiguration.INSTANCE.configurationBinds.refresh(configurations);
        if (!corruptFile) {
            ConfigurationManager first = find(ServiceUtil.preferredConfigurationId);
            if (first == null) {
                first = configurations.getFirst();
                select(first);
            }
            if (!corruptFile) {
                selected = first;
                ConfigurationManager target = first;
                requestData(first, (status, data) -> loadFirstOrExit(target, status, data));
            }
        }
        updateCatalogHash();
    }

    @SuppressWarnings("unused")
    private void vendorLoadFirstOrExit(ConfigurationManager config, int status, byte[] bytes) {
        if (status != 193435832) {
            Logger.log(LogLevel.ERROR, true, "Unable to load first configuration file: {}", status);
            System.exit(0);
            return;
        }
        Minecraft.getMinecraft().addScheduledTask(() -> handleCorruptFile(bytes, config));
    }

    @SuppressWarnings("unused")
    private void vendorLoadAll(String[] names, int[] ids, int integrity, long[] created, long[] lastUsed) {
        configurations.clear();
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == 0) continue;
            ConfigurationManager config = new ConfigurationManager();
            config.name = names[i];
            config.created = created[i];
            config.lastUsed = lastUsed[i];
            config.id = ids[i];
            configurations.add(config);
        }
        if (configurations.isEmpty()) {
            Logger.log(LogLevel.ERROR, true, "Unable to load configuration files.");
            System.exit(0);
            return;
        }
        com.adjustclient.service.config.user.UserConfiguration.INSTANCE.configurationBinds.refresh(configurations);
        if (!corruptFile) {
            ConfigurationManager first = find(ServiceUtil.preferredConfigurationId);
            if (first == null) {
                first = configurations.getFirst();
                select(first);
            }
            if (!corruptFile) {
                selected = first;
                ConfigurationManager target = first;
                requestData(first, (status, data) -> vendorLoadFirstOrExit(target, status, data));
            }
        }
        updateCatalogHash();
    }

    public void onConnect() {
        pending.clear();
        if (!localInitialized) initLocal();
    }

    @SuppressWarnings("unused")
    private void vendorOnConnect() {
        pending.clear();
        requestCatalog();
        configurations.clear();
    }

    public void requestCatalog() {
    }

    @SuppressWarnings("unused")
    private void vendorRequestCatalog() {
        int count = (int) (Math.random() * 5.0 + 2.0);
        int noise = 0;
        for (int i = 0; i < count; i++) {
            if (Math.random() > 0.4) noise += com.adjustclient.util.RandomUtil.nextInt(2) + 1;
        }
        java.util.Random random = (java.util.Random) RandomUtil.RANDOM;
        challenge = random.nextInt() ^ (pending.hashCode() + configurations.hashCode());
        if ((random.nextInt() ^ (pending.hashCode() + configurations.hashCode())) != challenge) {
            challenge += noise + count;
            if (Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated)
                Adjust.hydraConnection.send(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(22)
                        .order(ByteOrder.BIG_ENDIAN).putInt(challenge).array());
        }
    }

    public int updateCatalogHash() {
        int hash = 666;
        for (ConfigurationManager config : configurations)
            for (byte value : config.name.getBytes(java.nio.charset.StandardCharsets.UTF_8)) hash = 33 * hash + value;
        catalogHash = hash;
        return hash;
    }
}
