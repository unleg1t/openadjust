package com.adjustclient.utilities.protocol.interaction;
import com.adjustclient.utilities.protocol.type.EnumHand;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import com.adjustclient.Adjust;

public class NativeInteractionHandler implements InteractionHandler {
    public void rightClickMouse(Minecraft argument) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack stack = mc.thePlayer.inventory.getCurrentItem();
        boolean useItem = true;
        if (mc.objectMouseOver != null) {
            switch (mc.objectMouseOver.typeOfHit) {
                case ENTITY:
                    useItem = interactWithEntity(EnumHand.MAIN_HAND, mc.thePlayer, mc.playerController, mc.objectMouseOver);
                    break;
                case BLOCK:
                    BlockPos pos = mc.objectMouseOver.getBlockPos();
                    if (mc.theWorld.getBlockState(pos).getBlock().getMaterial() == net.minecraft.block.material.Material.air) break;
                    int oldSize = stack == null ? 0 : stack.stackSize;
                    boolean shouldRightSwing = Adjust.mouseClickHelper == null
                            || Adjust.mouseClickHelper.right == null
                            || Adjust.mouseClickHelper.right.swing
                            || (Adjust.mouseClickHelper.right.key != null && Adjust.mouseClickHelper.right.key.pressTime >= 1);
                    if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, stack, pos, mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec)) {
                        if (shouldRightSwing) mc.thePlayer.swingItem();
                        mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C0APacketAnimation());
                        useItem = false;
                    }
                    if (stack == null) return;
                    if (stack.stackSize == 0) {
                        mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = null;
                    } else if (shouldRightSwing) {
                        if (stack.stackSize != oldSize || mc.playerController.isInCreativeMode()) mc.entityRenderer.itemRenderer.resetEquippedProgress();
                    }
                    break;
                default:
                    break;
            }
        } else {
            java.util.Objects.requireNonNull(argument);
        }
        if (useItem) {
            ItemStack held = mc.thePlayer.inventory.getCurrentItem();
            if (held != null && mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, held)) mc.entityRenderer.itemRenderer.resetEquippedProgress();
        }
    }

    public void playBlockPlaceSound(World world, Block block, BlockPos pos) {
        // SoundType#getPlaceSound handles the stone, anvil and ladder overrides:
        // "step.stone", "random.anvil_land", "dig.wood".
        world.playSoundEffect((float) pos.getX() + 0.5F, (float) pos.getY() + 0.5F,
                (float) pos.getZ() + 0.5F, block.stepSound.getPlaceSound(),
                (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F);
    }

    public boolean interactWithEntity(EnumHand hand, EntityPlayer player, PlayerControllerMP controller, MovingObjectPosition hit) {
        if (controller.isPlayerRightClickingOnEntity(player, hit.entityHit, hit)) return false;
        return !controller.interactWithEntitySendPacket(player, hit.entityHit);
    }

    public C03PacketPlayer createWalkingUpdatePacket(EntityPlayerSP player, boolean onGround,
            double x, double y, double z, float yaw, float pitch) {
        double dx = x - player.lastReportedPosX;
        double dy = y - player.lastReportedPosY;
        double dz = z - player.lastReportedPosZ;
        float dyaw = yaw - player.lastReportedYaw;
        float dpitch = pitch - player.lastReportedPitch;
        boolean moving = dx * dx + dy * dy + dz * dz > 9.0E-4D || player.positionUpdateTicks >= 20;
        boolean rotating = dyaw != 0.0F || dpitch != 0.0F;
        C03PacketPlayer result;
        if (player.ridingEntity != null) {
            result = new C03PacketPlayer.C06PacketPlayerPosLook(player.motionX, -999.0D, player.motionZ, yaw, pitch, onGround);
            moving = false;
        } else if (moving && rotating) {
            result = new C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, yaw, pitch, onGround);
        } else if (moving) {
            result = new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, onGround);
        } else if (rotating) {
            result = new C03PacketPlayer.C05PacketPlayerLook(yaw, pitch, onGround);
        } else {
            result = new C03PacketPlayer(onGround);
        }
        ++player.positionUpdateTicks;
        if (moving) {
            player.lastReportedPosX = x;
            player.lastReportedPosY = y;
            player.lastReportedPosZ = z;
            player.positionUpdateTicks = 0;
        }
        if (rotating) {
            player.lastReportedYaw = yaw;
            player.lastReportedPitch = pitch;
        }
        player.lastReportedOnGround = onGround;
        return result;
    }
}
