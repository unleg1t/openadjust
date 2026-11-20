package com.adjustclient.utilities.hypixel.game;

import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.hypixel.HypixelGameState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.init.Blocks;

public class PixelPartyGameState extends HypixelGameState {
    public IBlockState targetBlock;
    public Object field48;
    public PixelPartyGameState() { super(HypixelGame.PIXEL_PARTY); }
    @Override public void onPacket(Packet<?> packet, boolean before) {
        super.onPacket(packet, before);
        if (startTimer == null || !before || !(packet instanceof S2FPacketSetSlot)) return;
        S2FPacketSetSlot slot = (S2FPacketSetSlot) packet;
        if (slot.func_149175_c() != 0 || slot.func_149173_d() != 44) return;
        ItemStack stack = slot.func_149174_e();
        if (mc.thePlayer.inventory.mainInventory[8] != null) {
            if (stack == null) targetBlock = null;
        } else if (stack != null && stack.getItem() instanceof ItemBlock) {
            ItemBlock item = (ItemBlock) stack.getItem();
            if (item.getBlock() == Blocks.bed) targetBlock = null;
            else targetBlock = item.getBlock().getStateFromMeta(stack.getMetadata());
        }
    }
}
