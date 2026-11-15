package com.adjustclient.game.player;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.Listener;
import com.adjustclient.event.LowLevelPacketReceiveEvent;
import com.adjustclient.util.Cooldown;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.RandomUtil;
import java.util.LinkedList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;

public class ChatMessageSender implements Listener, MinecraftInstance {
    public final LinkedList<QueuedMessage> queue = new LinkedList<>();
    public final Cooldown cooldown = new Cooldown(0L);
    public int sent;
    public int field16;
    private Object field20;

    static final class QueuedMessage {
        final String message;
        final boolean bypassQueue;
        private Object field12;
        QueuedMessage(String message, boolean bypassQueue) { this.message = message; this.bypassQueue = bypassQueue; }
    }

    public void update() {
        if (queue.isEmpty() || mc.thePlayer == null || mc.thePlayer.sendQueue == null) return;
        QueuedMessage first = queue.getFirst();
        if (sent >= 180 && !first.bypassQueue) return;
        if (!mc.thePlayer.sendQueue.doneLoadingTerrain || InventoryStateTracker.inServerGui || InventoryStateTracker.inServerInventory) {
            cooldown.timer.reset(); cooldown.durationMs = 0L; return;
        }
        if (!first.bypassQueue && !cooldown.isExpired(15L * first.message.length() + 280L)) return;
        QueuedMessage message = queue.pollFirst();
        if (message == null) return;
        sent += 20;
        GuiScreen.sendChatMessage(message.message, false);
        int delay = RandomUtil.nextInt(200);
        cooldown.timer.reset();
        cooldown.durationMs = delay;
    }

    @EventHandler public void onPacket(LowLevelPacketReceiveEvent event) {
        Packet<?> packet = event.packet;
        if (packet instanceof S04PacketEntityEquipment || packet instanceof S14PacketEntity
                || packet instanceof S18PacketEntityTeleport || packet instanceof S19PacketEntityHeadLook) {
            field16 = 0;
            return;
        }
        if (packet instanceof S01PacketJoinGame) {
            field16 = 0; sent = 0;
            cooldown.timer.reset(); cooldown.durationMs = 200L;
            queue.clear();
        }
    }

    public void sendMessage(String message, boolean bypassQueue) {
        message = message.trim();
        if (!message.isEmpty()) queue.add(new QueuedMessage(message, bypassQueue));
    }
}
