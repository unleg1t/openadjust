package com.adjustclient.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.gson.JsonParser;
import com.adjustclient.Adjust;
import com.adjustclient.rotation.handler.RenderRotationTracker;
import com.adjustclient.util.anticheat.ServerTracker;
import net.minecraft.world.WorldSettings;
import net.minecraft.block.Block;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.hypixel.game.murdermystery.MurderMysteryGameState;
import com.adjustclient.server.ServerType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.*;
import net.minecraft.init.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.adjustclient.module.impl.player.fastuse.FastUseModule;
import com.adjustclient.game.player.InventoryStateTracker;
import com.adjustclient.module.impl.combat.TeamsDetectionModule;

public final class WorldUtil {
    public static boolean canInteract() {
        Minecraft mc = Minecraft.getMinecraft();
        MovingObjectPosition hit = mc.objectMouseOver;
        if (hit == null) return true;
        if (mc.playerController.getIsHittingBlock()) return false;
        if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
            return hit.entityHit == null || hit.entityHit.getClass() != EntityVillager.class;
        if (hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return true;
        if (mc.thePlayer.movementInput.sneak) return true;
        return !BlockUtil.isInteractable(mc.theWorld.getBlockState(hit.getBlockPos()).getBlock());
    }
    public static boolean isLookingAtBlock(boolean useMouseOver) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || !mc.gameSettings.keyBindAttack.isKeyDown()
                || mc.playerController.getCurrentGameType() == WorldSettings.GameType.ADVENTURE) return false;
        if (mc.playerController.getIsHittingBlock()) return true;
        Vec2f rotation = RenderRotationTracker.INSTANCE.rotation;
        MovingObjectPosition hit = useMouseOver
                ? pickFromPlayer(false, rotation == null ? mc.thePlayer.rotationYaw : rotation.x,
                        rotation == null ? mc.thePlayer.rotationPitch : rotation.y,
                        mc.playerController.getBlockReachDistance()) : mc.objectMouseOver;
        return hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
    }
    public static boolean isMovingTowards(float yaw) {
        Minecraft mc = Minecraft.getMinecraft();
        Vec2f rotation = RenderRotationTracker.INSTANCE.rotation;
        float direction = rotation == null ? mc.thePlayer.rotationYaw : rotation.x;
        float forward = mc.gameSettings.keyBindForward.isKeyDown() ? 1.0F : 0.0F;
        if (mc.gameSettings.keyBindBack.isKeyDown()) forward = mc.gameSettings.keyBindForward.isKeyDown() ? 0.0F : -1.0F;
        float strafe = mc.gameSettings.keyBindLeft.isKeyDown() ? 1.0F : 0.0F;
        if (mc.gameSettings.keyBindRight.isKeyDown()) strafe = mc.gameSettings.keyBindLeft.isKeyDown() ? 0.0F : -1.0F;
        if (forward < 0.0F) direction += 180.0F;
        float offset = forward > 0.0F ? 45.0F : forward < 0.0F ? -45.0F : 90.0F;
        if (strafe > 0.0F) direction -= offset;
        if (strafe < 0.0F) direction += offset;
        return Math.abs(MathHelper.wrapAngleTo180_float(yaw - direction)) <= 45.0F;
    }
    public static boolean isTeammate(Entity entity) {
        if (!TeamsDetectionModule.INSTANCE.isEnabled()) return false;
        Minecraft mc = Minecraft.getMinecraft();
        ScorePlayerTeam own = (ScorePlayerTeam) mc.thePlayer.getTeam();
        if (own == null) return false;
        ScorePlayerTeam other = mc.theWorld.getScoreboard().getPlayersTeam(entity.getName());
        if (other == null) return false;
        boolean hypixel = ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL;
        if (hypixel) {
            var state = Adjust.gameStateTracker.state;
            if (state.startTimer == null || state.game == HypixelGame.HOUSING) return false;
            if (state instanceof MurderMysteryGameState)
                return !((MurderMysteryGameState) state).murderer;
            if (own.getColorPrefix().equals(other.getColorPrefix())
                    && Boolean.FALSE.equals(state.lobby) && state.game != HypixelGame.THE_HYPIXEL_PIT) return true;
        }
        if (!own.getAllowFriendlyFire() && own == other) return true;
        char color = lastFormattingCode(own.getColorPrefix());
        boolean same = color == lastFormattingCode(other.getColorPrefix());
        if (hypixel && color != ChatColor.RED.getFormattingCode())
            same = color == ChatColor.BLUE.getFormattingCode() && same;
        return same;
    }
    public static float getKnockbackStrength(EntityPlayer player) {
        return (EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, player.getCurrentEquippedItem())
                + (player.isSprinting() ? 1 : 0)) / 2.0F;
    }
    public static boolean isUsableItem(ItemStack stack) {
        if (stack == null) return false;
        Minecraft mc = Minecraft.getMinecraft();
        Item item = stack.getItem();
        Class<?> type = item == null ? null : item.getClass();
        if (item instanceof ItemFood && mc.thePlayer.canEat(((ItemFood) item).alwaysEdible)) return true;
        if (type == ItemBow.class && (mc.thePlayer.capabilities.isCreativeMode || mc.thePlayer.inventory.hasItem(Items.arrow))) return true;
        if (type == ItemArmor.class && mc.thePlayer.inventory.armorInventory[EntityLiving.getArmorPosition(stack) - 1] == null) return true;
        return type == ItemMinecart.class || type == ItemFlintAndSteel.class || type == ItemEgg.class
                || type == ItemSnowball.class || type == ItemEnderPearl.class || type == ItemFishingRod.class
                || type == ItemMonsterPlacer.class || type == ItemBucketMilk.class || type == ItemBucket.class
                || type == ItemPotion.class || type == ItemFirework.class || type == ItemBoat.class
                || type == ItemFireball.class || item == Items.dye;
    }
    public static void setMouseOver(MovingObjectPosition hit) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.pointedEntity = hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY ? hit.entityHit : null;
        mc.objectMouseOver = hit;
        ServerTracker.INSTANCE.mouseOverOverridden = true;
    }
    public static Integer findHotbarSlot(int startSlot, java.util.function.Function<Integer, Boolean> filter) {
        for (int slot = startSlot; slot < 9; slot++) if (filter.apply(slot)) return slot;
        for (int slot = startSlot; slot >= 0; slot--) if (filter.apply(slot)) return slot;
        return null;
    }
    public static void cancelMovementTick(com.adjustclient.event.CancellableEvent event, boolean updateUseItem) {
        event.cancelled = true;
        net.minecraft.client.entity.EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        player.previousTickPitch = player.tickPitch;
        player.tickPitch = player.rotationPitch;
        player.prevRenderYawOffset = player.renderYawOffset;
        player.prevRotationYawHead = player.rotationYawHead;
        player.prevRotationYaw = player.rotationYaw;
        player.prevRotationPitch = player.rotationPitch;
        player.prevDistanceWalkedModified = player.distanceWalkedModified;
        player.prevCameraYaw = player.cameraYaw;
        player.prevCameraPitch = player.cameraPitch;
        player.prevSwingProgress = player.swingProgress;
        player.updateEntityActionState();
        player.cameraYaw += -player.cameraYaw * 0.4F;
        player.cameraPitch += -player.cameraPitch * 0.8F;
        double dx = player.posX - player.prevPosX;
        double dz = player.posZ - player.prevPosZ;
        float bodyYaw = (float) (dx * dx + dz * dz) > 0.0025000002F
                ? (float) MathHelper.atan2(dz, dx) * 180.0F / (float) Math.PI - 90.0F : player.renderYawOffset;
        if (player.swingProgress > 0.0F) bodyYaw = player.rotationYaw;
        player.updateDistance(bodyYaw, 0.0F);
        player.prevRenderYawOffset = player.renderYawOffset - MathHelper.wrapAngleTo180_float(player.renderYawOffset - player.prevRenderYawOffset);
        player.prevRotationYawHead = player.rotationYawHead - MathHelper.wrapAngleTo180_float(player.rotationYawHead - player.prevRotationYawHead);
        player.prevChasingPosX = player.chasingPosX;
        player.prevChasingPosY = player.chasingPosY;
        player.prevChasingPosZ = player.chasingPosZ;
        double chaseX = player.posX - player.chasingPosX;
        double chaseY = player.posY - player.chasingPosY;
        double chaseZ = player.posZ - player.chasingPosZ;
        if (chaseX > 10.0D || chaseX < -10.0D) player.prevChasingPosX = player.chasingPosX = player.posX;
        if (chaseZ > 10.0D || chaseZ < -10.0D) player.prevChasingPosZ = player.chasingPosZ = player.posZ;
        if (chaseY > 10.0D || chaseY < -10.0D) player.prevChasingPosY = player.chasingPosY = player.posY;
        player.prevLimbSwingAmount = player.limbSwingAmount;
        player.limbSwingAmount += -player.limbSwingAmount * 0.4F;
        player.limbSwing += player.limbSwingAmount;
        player.chasingPosX += chaseX * 0.25D;
        player.chasingPosZ += chaseZ * 0.25D;
        player.chasingPosY += chaseY * 0.25D;
        player.prevPosX = player.posX;
        player.prevPosY = player.posY;
        player.prevPosZ = player.posZ;
        if (player.hurtTime > 0) player.hurtTime--;
        if (player.hurtResistantTime > 0) player.hurtResistantTime--;
        player.previousHurtTime = player.hurtTime;
        if (!updateUseItem) return;
        if (player.itemInUse != null) {
            if (player.itemInUse != player.getCurrentEquippedItem()) {
                player.clearItemInUse();
            } else {
                if (player.itemInUseCount <= 25 && player.itemInUseCount % 4 == 0)
                    player.updateItemUse(player.itemInUse, 5);
                if (--player.itemInUseCount == 0 && FastUseModule.INSTANCE.isEnabled()
                        && FastUseModule.INSTANCE.eating.getActiveMode()
                                == com.adjustclient.module.impl.player.fastuse.FastUseModule.EatingMode.PINGLESS)
                    player.onItemUseFinish();
            }
        }
        player.onUpdateWalkingPlayer();
    }
    public static AxisAlignedBB entityBoundsAt(Entity entity, Vec3 position) {
        AxisAlignedBB box = entity.getEntityBoundingBox().offset(position.xCoord - entity.posX,
                position.yCoord - entity.posY, position.zCoord - entity.posZ);
        double halfWidth = Math.min((box.maxX - box.minX) / 2.0D, 0.300000011920929D);
        double x = (box.minX + box.maxX) / 2.0D;
        double z = (box.minZ + box.maxZ) / 2.0D;
        return new AxisAlignedBB(x - halfWidth, box.minY, z - halfWidth, x + halfWidth, box.maxY, z + halfWidth);
    }
    public static ItemStack applyNbtJson(ItemStack stack, String json) throws NBTException {
        stack.setTagCompound(JsonToNBT.getTagFromJson(json));
        return stack;
    }
    public static boolean isFreeAt(Vec3 feet, double topY) {
        Minecraft mc = Minecraft.getMinecraft();
        float halfWidth = mc.thePlayer.width * 0.5F;
        AxisAlignedBB box = new AxisAlignedBB(feet.xCoord - halfWidth, feet.yCoord, feet.zCoord - halfWidth,
                feet.xCoord + halfWidth, topY, feet.zCoord + halfWidth);
        return !BlockUtil.isColliding(box);
    }
    public static MovingObjectPosition rayTracePlayerBlocks(float yaw, float pitch, double range) {
        Minecraft mc = Minecraft.getMinecraft();
        Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
        Vec3 look = mc.thePlayer.getVectorForRotation(pitch, yaw);
        return mc.theWorld.rayTraceBlocks(eyes, eyes.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range), false, false, true);
    }
    public static MovingObjectPosition pickFromPlayer(boolean throughWalls, float yaw, float pitch, double range) {
        Minecraft mc = Minecraft.getMinecraft();
        return pickEntities(mc.thePlayer, throughWalls, true, yaw, pitch, range,
                mc.playerController.getBlockReachDistance(), 0.0D);
    }
    public static MovingObjectPosition rayTrace(Entity entity, double range, float yaw, float pitch) {
        Vec3 eyes = entity.getPositionEyes(1.0F);
        Vec3 look = entity.getVectorForRotation(pitch, yaw);
        return Minecraft.getMinecraft().theWorld.rayTraceBlocks(eyes,
                eyes.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range), false, false, true);
    }
    public static MovingObjectPosition rayTraceBlock(BlockPos pos, Entity entity, double range, float yaw, float pitch) {
        Minecraft mc = Minecraft.getMinecraft();
        Vec3 eyes = entity.getPositionEyes(1.0F);
        Vec3 look = entity.getVectorForRotation(pitch, yaw);
        Vec3 end = eyes.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        block.setBlockBoundsBasedOnState(mc.theWorld, pos);
        return block.collisionRayTrace(mc.theWorld, pos, eyes, end);
    }
    public static MovingObjectPosition pickEntities(Entity entity, boolean overwrite, boolean traceBlocks, float yaw, float pitch, double range, double blockRange, double additionalBorder) {
        Minecraft mc = Minecraft.getMinecraft();
        Vec3 look = entity.getVectorForRotation(pitch, yaw);
        AxisAlignedBB search = entity.getEntityBoundingBox().addCoord(look.xCoord * range, look.yCoord * range, look.zCoord * range).expand(1.0D, 1.0D, 1.0D);
        Predicate<Entity> collidable = candidate -> candidate.canBeCollidedWith();
        Predicate<Entity> targetFilter = candidate -> RotationUtil.ignoredRaytraceEntity == null || RotationUtil.ignoredRaytraceEntity == candidate;
        List<Entity> candidates = mc.theWorld.getEntitiesInAABBexcluding(entity, search,
                Predicates.and(EntitySelectors.NOT_SPECTATING, collidable, targetFilter));
        List<org.apache.commons.lang3.tuple.Pair<Entity, AxisAlignedBB>> boxes = new ArrayList<>();
        for (Entity candidate : candidates) boxes.add(org.apache.commons.lang3.tuple.Pair.of(candidate, candidate.getEntityBoundingBox()));
        return pickFromBounds(entity, overwrite, traceBlocks, boxes, yaw, pitch, range, blockRange, additionalBorder);
    }
    public static MovingObjectPosition pickFromEntities(Entity entity, boolean overwrite, boolean traceBlocks, List<Entity> entities, float yaw, float pitch, double range, double blockRange, double additionalBorder) {
        List<org.apache.commons.lang3.tuple.Pair<Entity, AxisAlignedBB>> boxes = new ArrayList<>();
        for (Entity candidate : entities) boxes.add(org.apache.commons.lang3.tuple.Pair.of(candidate, candidate.getEntityBoundingBox()));
        return pickFromBounds(entity, overwrite, traceBlocks, boxes, yaw, pitch, range, blockRange, additionalBorder);
    }
    public static String lookupUuid(String name) throws java.io.IOException {
        String json = org.apache.commons.io.IOUtils.toString(
                java.net.URI.create("https://api.mojang.com/users/profiles/minecraft/" + name), StandardCharsets.UTF_8);
        return new JsonParser().parse(json).getAsJsonObject().get("id").getAsString();
    }
    public static boolean isPlaceableItem(ItemStack stack) {
        if (stack == null) return false;
        Item item = stack.getItem();
        Class<?> type = item == null ? null : item.getClass();
        return item instanceof ItemBlock || type == ItemHangingEntity.class || type == ItemBed.class
                || type == ItemSkull.class || type == ItemDoor.class || type == ItemReed.class
                || type == ItemSign.class || item == Items.redstone;
    }
    public static boolean isColumnCollisionFree(Vec3 feet, double topY, float halfWidth) {
        return !BlockUtil.isColliding(new AxisAlignedBB(feet.xCoord - halfWidth, feet.yCoord, feet.zCoord - halfWidth,
                feet.xCoord + halfWidth, topY, feet.zCoord + halfWidth));
    }
    public static MovingObjectPosition pickFromBounds(Entity entity, boolean overwrite, boolean traceBlocks, List<org.apache.commons.lang3.tuple.Pair<Entity, AxisAlignedBB>> entities, float yaw, float pitch, double range, double blockRange, double additionalBorder) {
        Minecraft mc = Minecraft.getMinecraft();
        if (entity == null || mc.theWorld == null) return null;
        MovingObjectPosition blockHit = traceBlocks ? rayTrace(entity, blockRange, yaw, pitch) : null;
        Vec3 eyes = entity.getPositionEyes(1.0F);
        double closest = blockHit == null ? range : eyes.distanceTo(blockHit.hitVec);
        Vec3 look = entity.getVectorForRotation(pitch, yaw);
        Vec3 end = eyes.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);
        Entity selected = null;
        Vec3 selectedPoint = null;
        for (org.apache.commons.lang3.tuple.Pair<Entity, AxisAlignedBB> entry : entities) {
            Entity candidate = entry.getLeft();
            float border = (float) additionalBorder + candidate.getCollisionBorderSize();
            AxisAlignedBB box = entry.getRight().expand(border, border, border);
            MovingObjectPosition intercept = box.calculateIntercept(eyes, end);
            if (box.isVecInside(eyes)) {
                if (closest >= 0.0D) {
                    selected = candidate;
                    selectedPoint = intercept == null ? eyes : intercept.hitVec;
                    closest = 0.0D;
                }
            } else if (intercept != null) {
                double distance = eyes.distanceTo(intercept.hitVec);
                if (overwrite || distance < closest || closest == 0.0D) {
                    selected = candidate;
                    selectedPoint = intercept.hitVec;
                    closest = distance;
                }
            }
        }
        return selected == null ? blockHit : new MovingObjectPosition(selected, selectedPoint);
    }
    public static boolean isHoldingWeapon() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return false;
        ItemStack stack = Adjust.slotSpoofer.field17 ? Adjust.slotSpoofer.getRenderItemStack() : mc.thePlayer.getCurrentEquippedItem();
        return stack != null && (stack.getItem() != null && stack.getItem().getClass() == ItemSword.class || hasWeaponEnchantment(stack));
    }
    public static boolean canSprint(boolean ignoreRequirements) {
        Minecraft mc = Minecraft.getMinecraft();
        if (InventoryStateTracker.inServerGui || InventoryStateTracker.inServerInventory || !isMovingTowards(mc.thePlayer.rotationYaw)) return false;
        if (ignoreRequirements) return true;
        return (mc.thePlayer.getFoodStats().getFoodLevel() >= 7 || mc.thePlayer.capabilities.allowFlying)
                && mc.thePlayer.moveForward >= 0.8F && !mc.thePlayer.isUsingItem()
                && !mc.thePlayer.isPotionActive(net.minecraft.potion.Potion.blindness)
                && (!mc.thePlayer.isSneaking() || mc.thePlayer.isPlayerSleeping())
                && !InventoryStateTracker.inServerGui && !InventoryStateTracker.inServerInventory;
    }
    public static String lookupUsername(String uuid) throws java.io.IOException {
        String json = org.apache.commons.io.IOUtils.toString(
                java.net.URI.create("https://api.mojang.com/user/profile/" + uuid), StandardCharsets.UTF_8);
        return new JsonParser().parse(json).getAsJsonObject().get("name").getAsString();
    }
    public static int safeFallDistance(EntityLivingBase entity) {
        return entity.isPotionActive(net.minecraft.potion.Potion.jump)
                ? entity.getActivePotionEffect(net.minecraft.potion.Potion.jump).getAmplifier() + 4 : 3;
    }
    public static boolean isColumnFreeOfLiquidAndClimbables(Vec3 feet, double topY, float halfWidth) {
        return !BlockUtil.isInLiquidOrClimbable(new AxisAlignedBB(feet.xCoord - halfWidth, feet.yCoord, feet.zCoord - halfWidth,
                feet.xCoord + halfWidth, topY, feet.zCoord + halfWidth));
    }
    public static Integer findKnockbackSlot() {
        ItemStack[] inventory = Minecraft.getMinecraft().thePlayer.inventory.mainInventory;
        int highest = 0;
        Integer result = null;
        for (int slot = 0; slot < 9; slot++) {
            if (inventory[slot] == null) continue;
            int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, inventory[slot]);
            if (level > highest) { highest = level; result = slot; }
        }
        return result;
    }
    public static char lastFormattingCode(String prefix) {
        int index = prefix.lastIndexOf('§');
        return index < 0 || index >= prefix.length() - 1 ? 0 : prefix.charAt(index + 1);
    }
    public static boolean hasWeaponEnchantment(ItemStack stack) {
        if (!stack.hasTagCompound()) return false;
        net.minecraft.nbt.NBTTagList enchants = stack.getTagCompound().getTagList("ench", 10);
        for (int i = 0; i < enchants.tagCount(); i++) {
            int id = enchants.getCompoundTagAt(i).getShort("id");
            if (id == Enchantment.knockback.effectId || id == Enchantment.sharpness.effectId
                    || id == Enchantment.fireAspect.effectId) return true;
        }
        return false;
    }
    public static void clearMouseOver() {
        Minecraft mc = Minecraft.getMinecraft();
        mc.objectMouseOver.typeOfHit = MovingObjectPosition.MovingObjectType.MISS;
        mc.pointedEntity = null;
        ServerTracker.INSTANCE.mouseOverOverridden = true;
    }
    public static boolean isOverVoid() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        return isFreeAt(new Vec3(player.posX, player.posY, player.posZ), 0.0D);
    }
    public static boolean hasBlockBelowEyes() {
        BlockPos pos = new BlockPos(Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0F)).offset(EnumFacing.DOWN);
        AxisAlignedBB box = BlockUtil.getCollisionBox(pos);
        return box != null && box.maxY - box.minY != 0.0D;
    }
    public static Integer getTeamColor(String playerName) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return null;
        ScorePlayerTeam team = mc.theWorld.getScoreboard().getPlayersTeam(playerName);
        String prefix = team == null ? null : getTeamPrefix(team);
        return prefix == null || prefix.length() < 2 ? null : mc.fontRendererObj.getColorCode(prefix.charAt(1));
    }
    public static String getTeamPrefix(ScorePlayerTeam team) {
        if (team == null) return null;
        String value = team.getColorPrefix();
        if (value == null || (ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL
                && Adjust.gameStateTracker.state.game == HypixelGame.HOUSING
                && Boolean.FALSE.equals(Adjust.gameStateTracker.state.lobby))) {
            String suffix = team.getColorSuffix();
            if (suffix != null && !suffix.isEmpty()) value = suffix;
        }
        return value == null ? null : net.minecraft.client.gui.FontRenderer.getFormatFromString(value);
    }
    public static AxisAlignedBB playerBoundsAt(Vec3 position, float height) {
        return new AxisAlignedBB(position.xCoord - 0.300000011920929D, position.yCoord, position.zCoord - 0.300000011920929D,
                position.xCoord + 0.300000011920929D, position.yCoord + height, position.zCoord + 0.300000011920929D);
    }
}
