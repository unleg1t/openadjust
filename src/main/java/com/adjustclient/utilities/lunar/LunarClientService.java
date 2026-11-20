package com.adjustclient.utilities.lunar;

import com.adjustclient.core.events.Listener;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.modules.impl.exploit.ClientSpooferModule;
import com.adjustclient.modules.impl.exploit.ClientSpooferMode;
import com.adjustclient.utilities.lunar.websocket.LunarAuthenticator;
import com.adjustclient.utilities.lunar.websocket.LunarTabLogoCache;
import com.adjustclient.utilities.lunar.websocket.LunarWebSocketClient;
import com.adjustclient.managers.storage.LocalStorage;
import com.mojang.util.UUIDTypeAdapter;
import java.util.UUID;
import net.minecraft.client.Minecraft;

public final class LunarClientService implements Listener {
    public String version;
    public String gitCommit;
    public String brand;
    public String jwt;
    public UUID sessionUuid;
    public final LunarTabLogoCache tabLogoCache = new LunarTabLogoCache();
    public Object field28;
    public long seed;
    public static LunarClientService INSTANCE;

    public void refreshSession() {
        net.minecraft.util.Session session = Minecraft.getMinecraft().getSession();
        if (session == null) return;
        UUID uuid = session.getProfile().getId();
        String sessionToken = session.getToken();
        if (uuid == null || sessionToken == null || sessionToken.isEmpty() || "0".equals(sessionToken)) return;
        LunarWebSocketClient socket = tabLogoCache.client;
        if (uuid.equals(sessionUuid) && socket != null && socket.connected
                && socket.socket != null
                && !socket.socket.isInputClosed()
                && !socket.socket.isOutputClosed()) return;
        jwt = null;
        sessionUuid = null;
        new Thread(() -> {
            try {
            String token = LunarAuthenticator.authenticate();
            jwt = token;
            sessionUuid = uuid;
            if (tabLogoCache.client != null)
                tabLogoCache.client.stop();
            tabLogoCache.requested.clear();
            tabLogoCache.logos.clear();
            LunarWebSocketClient client = new LunarWebSocketClient(tabLogoCache);
            tabLogoCache.client = client;
            client.reconnected = () -> tabLogoCache.requested.clear();
            client.start();
            } catch (Exception error) {
                Logger.log(LogLevel.WARN, true, "JWT fetch failed, peer icons disabled: {}", new Object[]{error});
            }
        }, "LunarClient-Auth").start();
    }

    public static void onJwtFetchFailed(Throwable error) {
        Logger.log(LogLevel.WARN, true, "JWT fetch failed, peer icons disabled: {}", new Object[]{error});
    }

    public static boolean isLunarSpoofingEnabled() {
        ClientSpooferModule module = (ClientSpooferModule) ClientSpooferModule.INSTANCE;
        return module.isEnabled() && module.mode.getValue() == ClientSpooferMode.LUNAR_CLIENT;
    }

    public void setLunarVersion(String version, String gitCommit) {
        this.version = version;
        this.gitCommit = gitCommit;
        brand = "lunarclient:" + version;
        String shortCommit = gitCommit.substring(0, Math.min(gitCommit.length(), 7));
        if (Logger.debug) {
            Logger.log(LogLevel.DEBUG, true, "Updated Lunar Client version {} ({})",
                    new Object[]{version, shortCommit});
        }
    }

    public static void initialize() throws java.io.IOException {
        LunarClientService service = new LunarClientService();
        INSTANCE = service;
        LocalStorage.INSTANCE.read("lunar_config.dat", bytes -> {
            LunarConfig.load(config -> {
                service.seed = config.seed;
                if (config.version != null && config.gitCommit != null)
                    service.setLunarVersion(config.version, config.gitCommit);
                if (config.version != null && config.gitCommit != null
                        && System.currentTimeMillis() - config.fetchedAt <= 86400000L) return;
                LunarVersionFetcher fetcher = new LunarVersionFetcher(config);
                if (service.version != null) fetcher.fetchVersionAsync();
                else {
                    Logger.log(LogLevel.WARN, true, "No Lunar Client version currently detected, blocking until one is received (this will not happen on future launches)", new Object[0]);
                    fetcher.fetchVersion();
                }
            }, bytes);
        });
    }
}
