package com.adjustclient.utilities.hypixel.game.build;

import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.hypixel.HypixelGameState;
import com.adjustclient.utilities.ChatUtil;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.*;

public final class SpeedBuildersGameState extends HypixelGameState {
    public BlockPos platform;
    public final Map<BlockPos, BuildBlock> build = new HashMap<>();
    public boolean building;
    public Object field56;
    public static final List<BlockPos> PLATFORMS;
    static {
        PLATFORMS = List.of(new BlockPos(45, 71, -18), new BlockPos(-16, 71, 45),
                new BlockPos(18, 71, 45), new BlockPos(45, 71, 16), new BlockPos(-18, 71, -45),
                new BlockPos(-45, 71, -16), new BlockPos(-45, 71, 18), new BlockPos(16, 71, -45));
    }
    public SpeedBuildersGameState() { super(HypixelGame.SPEED_BUILDERS); }
    public void findPlatform() {
        double distance = Double.MAX_VALUE;
        platform = null;
        for (BlockPos candidate : PLATFORMS) {
            double next = candidate.distanceSqToCenter(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            if (next < distance) { platform = candidate; distance = next; }
        }
    }
    @Override public void onPacket(Packet<?> packet, boolean before) {
        super.onPacket(packet, before);
        if (!before) return;
        if (packet instanceof S30PacketWindowItems) {
            S30PacketWindowItems items = (S30PacketWindowItems) packet;
            if (startTimer != null && items.func_148911_c() == 0) building = Arrays.stream(items.getItemStacks()).anyMatch(
                    stack -> stack != null && stack.getItem() instanceof ItemBlock);
        } else if (packet instanceof S45PacketTitle) {
            S45PacketTitle title = (S45PacketTitle) packet;
            if (title.getMessage() == null || !"5".equals(StringUtils.stripControlCodes(title.getMessage().getUnformattedText())) || building) return;
            mc.addScheduledTask(() -> {
                build.clear();
                if (platform == null) return;
                int y = 0;
                while (true) {
                    boolean found = false;
                    for (int x = -5; x <= 5; x++) {
                        for (int z = -5; z <= 5; z++) {
                            BlockPos pos = platform.add(x, y + 1, z);
                            IBlockState state = mc.theWorld.getBlockState(pos);
                            Block block = state.getBlock();
                            if (block == Blocks.air) continue;
                            block.setBlockBoundsBasedOnState(mc.theWorld, pos);
                            AxisAlignedBB bounds = block.getSelectedBoundingBox(mc.theWorld, pos);
                            build.put(pos, new BuildBlock(state, bounds));
                            found = true;
                        }
                    }
                    y++;
                    if (!found && y >= 4) break;
                }
                ChatUtil.sendClientMessage("Updated build: " + build.size());
            });
        }
    }
    @Override public void onUpdate() {
        if (startTimer != null && platform == null && mc.thePlayer.capabilities.allowFlying) findPlatform();
    }
}
