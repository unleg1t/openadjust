package com.adjustclient.hypixel.game.bedwars;

import com.google.gson.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

public final class BedWarsTimerProtocol {
    public final Int2ObjectAVLTreeMap<TimerMessage> timers = new Int2ObjectAVLTreeMap<>();
    public Object field8;
    public static final Map<Integer, Message> TYPES;

    static {
        // Keep the static identities: these Minecraft fields are initialized at run time.
        Item diamond = (Item) Items.diamond;
        Item emerald = (Item) Items.emerald;
        Item bed = (Item) Items.bed;
        Item suddenDeath = Item.getItemFromBlock((net.minecraft.block.Block) Blocks.dragon_egg);
        Item end = Item.getItemFromBlock((net.minecraft.block.Block) Blocks.bedrock);
        ItemStack moreDragons = new ItemStack(end);
        TYPES = Map.of(
            1, new Message("Diamond II", new ItemStack(diamond)),
            2, new Message("Emerald II", new ItemStack(emerald)),
            3, new Message("Diamond III", new ItemStack(diamond)),
            4, new Message("Emerald III", new ItemStack(emerald)),
            5, new Message("Bed Gone", new ItemStack(bed)),
            6, new Message("Sudden Death", new ItemStack(suddenDeath)),
            7, new Message("More Dragons", moreDragons),
            8, new Message("Game End", new ItemStack(end)));
    }

    public List<TimerMessage> getTimers() { return List.copyOf(timers.values()); }

    public static ItemStack parseItem(String name) {
        Item item = Item.getByNameOrId(name.toLowerCase());
        return item == null ? null : new ItemStack(item);
    }

    public void onPayload(S3FPacketCustomPayload packet) {
        if (!"badlion:timers".equals(packet.getChannelName())) return;
        PacketBuffer buffer = packet.getBufferData();
        byte[] data = new byte[buffer.readableBytes()];
        int index = buffer.readerIndex();
        buffer.readBytes(data);
        buffer.readerIndex(index);
        String message = new String(data, StandardCharsets.UTF_8);
        if (!message.contains("|")) return;
        String[] parts = message.split("\\|", 2);
        handleMessage(parts[0], new JsonParser().parse(parts[1]).getAsJsonObject());
    }

    public void handleMessage(String action, JsonObject data) {
        switch (action) {
            case "UPDATE_TIMER": {
                int id = data.get("id").getAsInt();
                if (timers.containsKey(id)) {
                    String name = data.get("name").getAsString();
                    ItemStack item = parseItem(data.getAsJsonObject("item").get("type").getAsString());
                    int time = data.get("time").getAsInt();
                    timers.put(id, new TimerMessage(new Message(name, item), time));
                }
                break;
            }
            case "REMOVE_TIMER": timers.remove(data.get("id").getAsInt()); break;
            case "REMOVE_ALL_TIMERS":
            case "CHANGE_WORLD": timers.clear(); break;
            case "ADD_TIMER": {
                int id = data.get("id").getAsInt();
                if (!timers.containsKey(id)) {
                    String name = data.get("name").getAsString();
                    ItemStack item = parseItem(data.getAsJsonObject("item").get("type").getAsString());
                    int time = data.get("time").getAsInt();
                    timers.put(id, new TimerMessage(new Message(name, item), time));
                }
                break;
            }
            case "SYNC_TIMERS": {
                int id = data.get("id").getAsInt();
                int time = data.get("time").getAsInt();
                if (timers.containsKey(id)) {
                    TimerMessage timer = timers.get(id);
                    timer.updatedAt = System.currentTimeMillis();
                    timer.time = time;
                } else if (TYPES.containsKey(id)) {
                    timers.put(id, new TimerMessage(TYPES.get(id), time));
                }
                break;
            }
            default: break;
        }
    }

    static class Message {
        public final String name;
        public final ItemStack item;
        public Object field12;
        Message(String name, ItemStack item) { this.name = name; this.item = item; }
    }

    public static final class TimerMessage extends Message {
        public int time;
        public long updatedAt;
        public Object field24;
        public TimerMessage(Message message, int time) {
            super(message.name, message.item);
            this.time = time;
            updatedAt = System.currentTimeMillis();
        }
    }
}
