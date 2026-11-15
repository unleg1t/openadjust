package com.adjustclient.module.impl.player.inventory.item.type;
import com.adjustclient.Adjust;
import com.adjustclient.module.impl.player.inventory.item.ItemType;
import com.adjustclient.module.impl.player.inventory.item.filter.SimpleItemFilter;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import java.util.Set;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.init.Blocks;
public final class BlockItemType extends ItemType {
    public static final Set<Block> BLACKLIST;
    static {
        BLACKLIST = Set.of(Blocks.web, Blocks.torch, Blocks.glass_pane, Blocks.stained_glass_pane,
                Blocks.ladder, Blocks.vine, Blocks.end_portal_frame, Blocks.end_portal,
                Blocks.portal, Blocks.cactus, Blocks.slime_block, Blocks.tripwire_hook,
                Blocks.tripwire, Blocks.gravel, Blocks.sand, Blocks.anvil);
    }
    public BlockItemType() {
        keepFilter = new SimpleItemFilter(320);
        sortKeyFunction = stack -> -stack.stackSize;
    }
    @Override public boolean matches(ItemStack stack) {
        return stack.getItem() instanceof ItemBlock && isValidBlock((ItemBlock) stack.getItem());
    }
    public static boolean isValidBlock(ItemBlock item) {
        Block block = item.getBlock();
        if (BLACKLIST.contains(block) || BlockUtil.isInteractable(block)
                || block instanceof BlockStairs || block instanceof BlockPumpkin || block instanceof BlockWall
                || block instanceof BlockBasePressurePlate || block instanceof BlockDoor || block instanceof BlockFenceGate
                || block instanceof BlockBush || block instanceof BlockFence) return false;
        if (block == Blocks.tnt) {
            var blocker = ServerTracker.INSTANCE.serverState;
            if (blocker != null && blocker.antiCheatModel != null
                    && blocker.antiCheatModel.serverType == ServerType.MINEMEN_CLUB) return false;
            var state = Adjust.gameStateTracker.state;
            if (state.startTimer != null && (state.game == HypixelGame.BED_WARS || state.game == HypixelGame.BED_WARS_PRACTICE)) return false;
        }
        return block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() == 1.0
                && block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() == 1.0
                && block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() == 1.0;
    }
    @Override public boolean equals(Object other) { return other instanceof BlockItemType; }
}
