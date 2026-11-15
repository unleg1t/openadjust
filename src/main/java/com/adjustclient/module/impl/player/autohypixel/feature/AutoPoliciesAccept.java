package com.adjustclient.module.impl.player.autohypixel.feature;

import com.adjustclient.Adjust;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.ItemPlacementAttemptEvent;
import com.adjustclient.module.impl.player.autohypixel.AutoHypixelModule;
import com.adjustclient.setting.BooleanSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.init.Items;

public class AutoPoliciesAccept extends AutoHypixelFeature {
    public static AutoPoliciesAccept INSTANCE;
    public final BooleanSetting enabled;
    public AutoPoliciesAccept(AutoHypixelModule owner) {
        super(owner);
        INSTANCE = this;
        enabled = new BooleanSetting(owner, "Auto Policies Accept", true);
    }
    @Override public boolean isFeatureEnabled() { return enabled.isEnabled(); }

    @EventHandler public void onPacket(com.adjustclient.event.PacketAppliedEvent event) {
        if (!isActive() || !(event.packet instanceof S3FPacketCustomPayload)) return;
        S3FPacketCustomPayload packet = (S3FPacketCustomPayload) event.packet;
        if (!packet.getChannelName().equals("MC|BOpen")) return;
        ItemStack held = mc.thePlayer.getHeldItem();
        if (held == null || held.getItem() != net.minecraft.init.Items.written_book || held.getTagCompound() == null) return;
        if (held.getTagCompound().getTagList("pages", 8).getStringTagAt(0).contains("/policies accept"))
            Adjust.chatMessageSender.sendMessage("/policies accept", false);
    }
}
