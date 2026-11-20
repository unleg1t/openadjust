package com.adjustclient.modules.impl.player.autohypixel.feature;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.ChatLineEvent;
import com.adjustclient.modules.impl.player.autohypixel.AutoHypixelModule;
import com.adjustclient.modules.impl.player.autohypixel.feature.chatstats.*;
import com.adjustclient.modules.impl.render.streamer.StreamerModule;
import com.adjustclient.core.settings.BooleanSetting;
import java.util.List;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;

public final class ChatStats extends AutoHypixelFeature {
    public static ChatStats INSTANCE;
    public final BooleanSetting enabled;
    public final List<ChatStatsProvider> providers = List.of(new UrchinStatsProvider(), new BedWarsStatsProvider());
    public ChatStats(AutoHypixelModule owner) {
        super(owner);
        INSTANCE = this;
        enabled = new BooleanSetting(owner, "Chat Stats", true);
        enabled.setDescription("Shows you stats of players in chat.");
    }
    @Override public boolean isFeatureEnabled() { return enabled.isEnabled(); }

    @EventHandler(priority = 1) public void onChat(ChatLineEvent event) {
        if (!isActive() || !event.pre || event.displayOnly) return;
        if (event.chat != Adjust.chatManager.minecraftChannel.history) return;
        IChatComponent component = event.component;
        String sender = StreamerModule.getChatSenderName(component);
        if (sender == null) return;
        String username = mc.getSession().getUsername();
        if (sender.equalsIgnoreCase(username)) return;
        String text = StringUtils.stripControlCodes(component.getUnformattedText());
        int separator = text.indexOf(':');
        if (separator == -1) return;
        if (text.substring(separator + 1).toLowerCase(java.util.Locale.ROOT).contains(username.toLowerCase(java.util.Locale.ROOT)))
            showStats(sender, StreamerModule.extractSenderComponent(component, sender));
    }

    public void showStats(String name, IChatComponent displayName) {
        for (ChatStatsProvider provider : providers) provider.showStats(name, displayName);
    }
}
