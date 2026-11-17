package com.adjustclient.module.impl.render.freecam;

import com.adjustclient.Adjust;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.SimulationPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.ChatColor;
import net.minecraft.world.WorldSettings;

public final class FreecamCamera extends EntityLivingBase implements com.adjustclient.util.rotation.SimulatedEntity {
    public ItemStack[] hotbar;
    public FreecamState state;
    public int[] tools;
    public ItemStack[] speedItems;
    public SimulationPlayer simulation;
    public final FreecamModule module;
    public float strafe;
    public float forward;
    public boolean jump;
    public boolean sneak;
    public net.minecraft.entity.Entity target;

    public FreecamCamera(FreecamModule module, World world) {
        super(world);
        this.module = module;
        hotbar = new ItemStack[9];
        state = FreecamState.MAIN;
        tools = new int[3];
        speedItems = new ItemStack[9];
        makeToolBar();
    }

    public FreecamControlMode getState() {
        if (state != FreecamState.HOUSING || distanceToCamera() == null) return null;
        if (selectedSlot() == 7) return FreecamControlMode.TELEPORT;
        return selectedSlot() == 6 ? FreecamControlMode.DECIMATOR : null;
    }
    @Override protected void entityInit() { }
    @Override public boolean isSneaking() { return sneak; }
    @Override public ItemStack getEquipmentInSlot(int slot) { return null; }
    @Override public void setHealth(float value) { }
    @Override public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound tag) { }
    @Override public void setCurrentItemOrArmor(int slot, ItemStack stack) { }
    @Override public ItemStack getCurrentArmor(int slot) { return null; }
    @Override public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound tag) { }
    public void scroll(int amount) {
        int direction = Math.max(-1, Math.min(1, amount));
        int slot = Math.floorMod(selectedSlot() - direction, 9);
        if (state == FreecamState.CHANGE_SPEED) FreecamModule.cameraSpeed.setValue(slot + 1);
        else tools[state.ordinal()] = slot;
        if (distanceToCamera() == null && state != FreecamState.HOUSING) scroll(direction);
    }
    public ItemStack getTool(int slot) {
        if (slot < 0 || slot >= 9) return null;
        if (state == FreecamState.HOUSING) return module.field72.mainInventory[slot];
        return (state == FreecamState.CHANGE_SPEED ? speedItems : hotbar)[slot];
    }
    public void updateSimulation() {
        if (simulation == null || simulation.worldObj != MinecraftInstance.mc.theWorld) {
            simulation = new SimulationPlayer(MinecraftInstance.mc.theWorld);
            simulation.motionX = MinecraftInstance.mc.thePlayer.motionX;
            simulation.motionY = MinecraftInstance.mc.thePlayer.motionY;
            simulation.motionZ = MinecraftInstance.mc.thePlayer.motionZ;
        }
        simulation.setPosition(posX, posY, posZ);
        simulation.prevPosX = simulation.lastTickPosX = posX;
        simulation.prevPosY = simulation.lastTickPosY = posY;
        simulation.prevPosZ = simulation.lastTickPosZ = posZ;
        simulation.rotationYaw = rotationYaw;
        simulation.prevRotationYaw = prevRotationYaw;
        if (!simulation.worldObj.isRemote) {
            for (net.minecraft.potion.PotionEffect effect : new java.util.ArrayList<>(simulation.getActivePotionEffects()))
                simulation.removePotionEffect(effect.getPotionID());
        }
        if (MinecraftInstance.mc.thePlayer.capabilities.isFlying) simulation.capabilities.isFlying = true;
    }
    public void updatePosition(int slot) {
        if (state == FreecamState.CHANGE_SPEED) {
            int value = Math.min(slot + 1, FreecamModule.cameraSpeed.getMax().intValue());
            value = Math.max(value, FreecamModule.cameraSpeed.getMin().intValue());
            FreecamModule.cameraSpeed.setValue(value);
        }
        else tools[state.ordinal()] = slot;
    }
    @Override public float getHealth() { return 20.0F; }
    public void handleKeyboard() {
        if (simulation == null) return;
        simulation.movementInput.moveForward = forward;
        simulation.movementInput.moveStrafe = strafe;
        if (MinecraftInstance.mc.gameSettings.keyBindSprint.getKeyCode() != 0)
            net.minecraft.client.settings.GameSettings.isKeyDown(MinecraftInstance.mc.gameSettings.keyBindSprint);
        int speed = FreecamModule.cameraSpeed.getValue().intValue();
        double movementSpeed = speed * 0.14 - (speed >= 4 ? speed * 0.05 : 0.0);
        simulation.movementSpeed = (float) movementSpeed;
        float flySpeed = (float) (movementSpeed / 2.54);
        simulation.capabilities.setFlySpeed(flySpeed);
        simulation.sprintKeyDown = forward != 0.0F || strafe != 0.0F;
        boolean oldJump = simulation.movementInput.jump;
        simulation.movementInput.jump = jump;
        simulation.movementInput.sneak = sneak;
        if (!oldJump && jump) {
            if (simulation.flyToggleTimer != 0) {
                simulation.capabilities.isFlying = !simulation.capabilities.isFlying;
                simulation.flyToggleTimer = 0;
            } else simulation.flyToggleTimer = 7;
        }
        if (simulation.capabilities.isFlying) {
            float verticalSpeed = (float) (movementSpeed / 2.54 * 3.0);
            if (sneak) simulation.motionY -= verticalSpeed;
            if (jump) simulation.motionY += verticalSpeed;
            if (simulation.onGround) simulation.capabilities.isFlying = false;
        }
        if (com.adjustclient.util.SimulationPlayer.simulationModeState == 2) {
            simulation.updateArmSwingProgress();
            simulation.rotationYawHead = simulation.rotationYaw;
        }
        simulation.moveStrafing = simulation.movementInput.moveStrafe;
        simulation.moveForward = simulation.movementInput.moveForward;
        simulation.isJumping = simulation.movementInput.jump;
    }
    @Override public ItemStack getHeldItem() { return null; }
    @Override public ItemStack[] getInventory() { return new ItemStack[0]; }
    public int selectedSlot() {
        return state == FreecamState.CHANGE_SPEED ? FreecamModule.cameraSpeed.getValue().intValue() - 1 : tools[state.ordinal()];
    }
    public ItemStack distanceToCamera() { return getTool(selectedSlot()); }
    public boolean onDisable() { return true; }
    public void rotateTowards() {
        if (!isSwingInProgress || swingProgressInt >= getArmSwingAnimationEnd() / 2 || swingProgressInt < 0) {
            swingProgressInt = -1;
            isSwingInProgress = true;
        }
        if (state != FreecamState.MAIN || selectedSlot() != 1) return;
        com.adjustclient.util.anticheat.entity.TrackedLivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (com.adjustclient.util.anticheat.entity.TrackedLivingEntity tracked :
                com.adjustclient.util.anticheat.ServerTracker.INSTANCE.entityTracker.getEntities(98, com.adjustclient.util.anticheat.entity.TrackedLivingEntity.class)) {
            net.minecraft.entity.Entity entity = tracked.getEntity();
            double dx = entity.posX - module.camera.posX;
            double dy = entity.posY - module.camera.posY;
            double dz = entity.posZ - module.camera.posZ;
            float horizontal = (float) Math.sqrt(dx * dx + dz * dz);
            float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
            float pitch = (float) -(Math.atan2(dy, horizontal) * 180.0 / Math.PI);
            float yawDistance = net.minecraft.util.MathHelper.wrapAngleTo180_float(module.camera.rotationYaw - yaw);
            float pitchDistance = module.camera.rotationPitch - pitch;
            double distance = Math.sqrt((double) yawDistance * yawDistance + (double) pitchDistance * pitchDistance);
            if (distance < best) { best = distance; nearest = tracked; }
        }
        if (nearest != null && best < 16.0) module.handleTools(nearest.getEntity());
    }
    public void makeNukeTool() {
        if (state == FreecamState.MAIN) {
            int slot = selectedSlot();
            switch (slot) {
                case 0:
                    state = FreecamState.CHANGE_SPEED;
                    break;
                case 1:
                    ((FreecamModule) module).updateCameraRay();
                    break;
                case 2:
                    if (((BooleanSetting) FreecamModule.cameraNoClip).isEnabled()) {
                        if (simulation == null) updateSimulation();
                        SimulationPlayer player = (SimulationPlayer) simulation;
                        player.capabilities.isFlying = true;
                        player.motionX = player.motionY = player.motionZ = 0.0;
                    }
                    BooleanSetting setting = (BooleanSetting) FreecamModule.cameraNoClip;
                    setting.setValue(!setting.isEnabled());
                    break;
                case 3:
                    FreecamModule owner = (FreecamModule) module;
                    if (owner.field72 == null) {
                        owner.field72 = new InventoryPlayer(MinecraftInstance.mc.thePlayer);
                        ((InventoryPlayer) owner.field72).mainInventory[6] = FreecamModule.readItemData(
                                (Item) Items.diamond_pickaxe, ChatColor.RED + "Nuke", 1,
                                ChatColor.GREEN + "Right Click " + ChatColor.GRAY + "to decimate", true);
                        ((InventoryPlayer) owner.field72).mainInventory[7] = FreecamModule.readItemData(
                                (Item) Items.stick, ChatColor.GOLD + "Teleport", 1,
                                ChatColor.GRAY + "Click to teleport your server-side player.", true);
                        ((InventoryPlayer) owner.field72).mainInventory[8] = FreecamModule.readItemData(
                                (Item) Items.feather, ChatColor.GRAY + "Back", 1, null, false);
                    }
                    if (Adjust.gameStateTracker.state.game == HypixelGame.HOUSING
                            && !Adjust.gameStateTracker.state.lobby) {
                        state = FreecamState.HOUSING;
                    }
                    break;
                case 8:
                    while (((FreecamModule) module).isEnabled()) ((FreecamModule) module).toggle(false);
                    break;
                default:
                    break;
            }
        } else if (state == FreecamState.CHANGE_SPEED) {
            state = FreecamState.MAIN;
        } else if (state == FreecamState.HOUSING && selectedSlot() == 8) {
            state = FreecamState.MAIN;
            updatePosition(8);
        }
    }
    public ItemStack makeNoClipTool() {
        boolean enabled = ((BooleanSetting) FreecamModule.cameraNoClip).isEnabled();
        Item item = enabled ? (Item) Items.saddle
                : Item.getItemFromBlock(Blocks.barrier);
        hotbar[2] = FreecamModule.readItemData(item, "", 1, null, false);
        return hotbar[2].setStackDisplayName((enabled ? ChatColor.RED + "Disable"
                : ChatColor.GREEN + "Enable") + " No-Clip");
    }
    @Override public void setPosition(double x, double y, double z) {
        if (simulation != null) {
            simulation.setPosition(x, y, z);
            simulation.motionX = simulation.motionY = simulation.motionZ = 0.0;
        }
        super.setPosition(x, y, z);
    }
    public void makeToolBar() {
        hotbar[1] = FreecamModule.readItemData((Item) Items.blaze_rod,
                ChatColor.DARK_PURPLE + "Magic Wand", 1, null, false);
        hotbar[0] = FreecamModule.readItemData((Item) Items.sugar,
                ChatColor.AQUA + "Speed", 1, null, false);
        hotbar[8] = FreecamModule.readItemData((Item) Items.feather,
                ChatColor.GRAY + "Back", 1, null, false);
        for (int i = 0; i < speedItems.length; i++) {
            speedItems[i] = FreecamModule.readItemData((Item) Items.sugar,
                    ChatColor.AQUA + "Speed " + ChatColor.GRAY + (i + 1), i + 1, null, false);
        }
        makeNoClipTool();
        hotbar[4] = FreecamModule.readItemData((Item) Items.lead,
                ChatColor.GOLD + "Player Control", 1,
                "Hold this item to take control of your player", false);
        if (Adjust.gameStateTracker.state.game == HypixelGame.HOUSING) {
            if (MinecraftInstance.mc.playerController.getCurrentGameType()
                    == net.minecraft.world.WorldSettings.GameType.ADVENTURE) return;
            hotbar[3] = FreecamModule.readItemData((Item) Items.jungle_door,
                    ChatColor.YELLOW + "Housing", 1, null, false);
        }
    }
}
