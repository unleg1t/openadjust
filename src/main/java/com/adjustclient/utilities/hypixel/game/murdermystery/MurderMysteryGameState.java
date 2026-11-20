package com.adjustclient.utilities.hypixel.game.murdermystery;

import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.hypixel.HypixelGameState;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.entity.TrackedEntity;
import com.adjustclient.utilities.anticheat.entity.TrackedFakePlayer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import com.adjustclient.modules.impl.exploit.murdermystery.MurderMysteryModule;
import net.minecraft.init.Items;

public final class MurderMysteryGameState extends HypixelGameState {
    public final Map<MurderMysteryRole, Collection<MurderMysteryPlayer>> playersByRole = new HashMap<>();
    public final Int2ObjectOpenHashMap<MurderMysteryPlayer> players = new Int2ObjectOpenHashMap<>();
    public boolean murderer;
    public Object field56;
    public MurderMysteryGameState() {
        super(HypixelGame.MURDER_MYSTERY);
        for (MurderMysteryRole role : MurderMysteryRole.values()) playersByRole.put(role, new HashSet<>());
    }
    public void addPlayer(MurderMysteryPlayer player) {
        playersByRole.get(MurderMysteryRole.MURDERER).add(player);
        players.put(player.entityId, player);
    }
    public static boolean isMurderWeapon(ItemStack stack) {
        if (stack == null) return false;
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey("ExtraAttributes", 10) && tag.getCompoundTag("ExtraAttributes").hasKey("MELEE", 1);
    }
    @Override public void onPacket(Packet<?> packet, boolean before) {
        super.onPacket(packet, before);
        if (!before) return;
        if (packet instanceof S2FPacketSetSlot) {
            S2FPacketSetSlot slot = (S2FPacketSetSlot) packet;
            if ((lobby == null || !lobby) && mc.thePlayer != null && startTimer != null && !ended
                    && mc.thePlayer.openContainer != null && slot.func_149175_c() == mc.thePlayer.openContainer.windowId
                    && isMurderWeapon(slot.func_149174_e())) murderer = true;
        } else if (packet instanceof S04PacketEntityEquipment) {
            S04PacketEntityEquipment equipment = (S04PacketEntityEquipment) packet;
            if (mc.thePlayer == null || equipment.getEquipmentSlot() != 0 || !hasStartedFor(1000)
                    || lobby != null && lobby || equipment.getEntityID() == mc.thePlayer.getEntityId()) return;
            MurderMysteryPlayer known = getPlayer(equipment.getEntityID());
            if (known != null && known.role == MurderMysteryRole.MURDERER) return;
            ItemStack stack = equipment.getItemStack();
            if (isMurderWeapon(stack)) addPlayer(new MurderMysteryPlayer(MurderMysteryRole.MURDERER, equipment.getEntityID()));
            if (stack != null && (stack.getItem() instanceof ItemBow || stack.getItem() == Items.arrow))
                addPlayer(new MurderMysteryPlayer(MurderMysteryRole.DETECTIVE, equipment.getEntityID()));
        }
    }
    public MurderMysteryPlayer getPlayer(int entityId) { return players.get(entityId); }
    public void onPlayerResolved(MurderMysteryPlayer player) {
        if (player.role == MurderMysteryRole.MURDERER) MurderMysteryModule.INSTANCE.onMurdererDetected(player);
    }
    @Override public void onUpdate() {
        for (MurderMysteryPlayer player : players.values()) {
            if (player.name != null) continue;
            TrackedFakePlayer tracked = ServerTracker.INSTANCE.entityTracker.get(player.entityId, TrackedFakePlayer.class);
            if (tracked != null) {
                player.name = tracked.getEntity().getName();
                onPlayerResolved(player);
            }
        }
    }
    public MurderMysteryPlayer getPlayer(String name) {
        for (MurderMysteryPlayer player : players.values()) if (player.name != null && player.name.equals(name)) return player;
        return null;
    }
}
