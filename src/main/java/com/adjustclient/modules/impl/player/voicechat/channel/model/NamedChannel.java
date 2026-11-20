package com.adjustclient.modules.impl.player.voicechat.channel.model;

import com.adjustclient.modules.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.modules.impl.player.voicechat.protocol.property.ChannelProperty;
import com.adjustclient.managers.session.SessionService;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public final class NamedChannel extends Channel {
    public NamedChannel(UUID id) { super(id); }

    @Override public String getName() {
        return (String) properties.properties.get(ChannelProperty.NAME);
    }

    @Override public boolean isOwner() {
        UUID owner = (UUID) properties.properties.get(ChannelProperty.OWNER);
        Session session = VoiceChatModule.INSTANCE.sessionManager.session;
        if (session == null) session = Minecraft.getMinecraft().getSession();
        return owner != null && Objects.equals(owner, SessionService.resolveSessionUuid(session));
    }
}
