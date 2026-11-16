package com.adjustclient.module.impl.player.voicechat.client;

import com.adjustclient.module.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.module.impl.player.voicechat.client.session.VoiceSessionHandler;
import com.adjustclient.module.impl.player.voicechat.protocol.type.VoiceServer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import com.mojang.authlib.exceptions.AuthenticationException;

public final class VoiceSessionManager {
    public final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "VoiceSession");
        thread.setDaemon(true);
        return thread;
    });
    public final VoiceChatClient client;
    public final VoiceSender sender;
    public final VoiceSessionHandler handler;
    public Session session;
    public final Set<String> failedTokens = ConcurrentHashMap.newKeySet();

    public VoiceSessionManager(VoiceSessionHandler handler) {
        this.handler = handler;
        client = new VoiceChatClient();
        client.listener = handler;
        sender = new VoiceSender(client);
    }

    public void switchSession(Session session) {
        if (session == null || !VoiceChatModule.INSTANCE.isEnabled()) return;
        this.session = session;
        client.authenticators.put(VoiceServer.MOJANG, serverHash -> {
            Session activeSession = this.session;
            if (activeSession == null || activeSession.getProfile() == null
                    || activeSession.getProfile().getId() == null
                    || activeSession.getToken() == null
                    || activeSession.getToken().isEmpty()
                    || "0".equals(activeSession.getToken())) {
                if (activeSession != null && activeSession.getToken() != null) {
                    failedTokens.add(activeSession.getToken());
                }
                return null;
            }
            try {
                Minecraft.getMinecraft().getSessionService().joinServer(activeSession.getProfile(),
                        activeSession.getToken(), serverHash);
                return new AuthenticationResponse(activeSession.getUsername(), null);
            } catch (com.mojang.authlib.exceptions.InvalidCredentialsException exception) {
                failedTokens.add(activeSession.getToken());
                return null;
            } catch (Exception exception) {
                return null;
            }
        });
        client.authenticators.put(VoiceServer.LABYCONNECT, serverHash -> {
            return null;
        });
        if (client.udpClient != null && client.udpClient.open.get()) {
            client.disconnect("Switching session");
            reconnect();
        }
    }

    public void reconnect() {
        Session activeSession = session;
        if (activeSession == null || activeSession.getToken() == null
                || failedTokens.contains(activeSession.getToken())) return;
        executor.execute(() -> {
            try {
                String host = VoiceChatModule.INSTANCE.settings.useBackupServer.isEnabled()
                        ? "voice2.labymod.net" : "voice.labymod.net";
                InetSocketAddress address;
                try {
                    address = new InetSocketAddress(InetAddress.getAllByName(host)[0], 8066);
                } catch (UnknownHostException exception) {
                    address = InetSocketAddress.createUnresolved(host, 8066);
                }
                final InetSocketAddress target = address;
                Thread connectThread = new Thread(() -> client.connect(target, VoiceServer.MOJANG), "VoiceConnect");
                connectThread.setDaemon(true);
                connectThread.start();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }
}
