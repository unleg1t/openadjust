package com.adjustclient.modules.impl.render.freecam;

import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.core.settings.SettingContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;

public final class FreecamModule extends Module {
    public static FreecamModule INSTANCE;
    public static NumberSetting cameraSpeed;
    public static BooleanSetting cameraNoClip;
    public FreecamCamera camera;
    public Object previousView;
    public Object field40;
    public Object field44;
    public com.adjustclient.modules.impl.render.freecam.tool.FreecamController controller;
    public Object field52;
    public net.minecraft.util.MovingObjectPosition hitPosition;
    public net.minecraft.util.Vec3 active;
    public Object field64;
    public NumberSetting pathfinderTimeout;
    public static BooleanSetting packetBalanceRegain;
    public net.minecraft.entity.player.InventoryPlayer field72;
    public float field76;
    public int field80;
    public int field84;
    public boolean field88;
    public Object field92;

    public FreecamModule() {
        super("Freecam", Category.RENDER);
        INSTANCE = this;
        notifyToggle = false;
        cameraSpeed = new NumberSetting(this, "Speed", 2, 1, 9, 1);
        cameraSpeed.setDescription("Camera movement speed.");
        cameraNoClip = new BooleanSetting(this, "No Clip", false);
        cameraNoClip.setDisplayName("Noclip");
        cameraNoClip.setDescription("Controls if your camera can pass through blocks.");
        cameraNoClip.addListener(NO_CLIP_CHANGED);
        packetBalanceRegain = new BooleanSetting(this, "Packet Balance Regain", true);
        pathfinderTimeout = new NumberSetting(this, "Pathfinder timeout", 1500L, 300L, 3000L, 100L)
                .formatter(NumberSetting.DURATION);
        pathfinderTimeout.setDescription("Maximum time allowed for the pathfinder to finish computing.");
    }

    public void updateCameraRay() {
        net.minecraft.util.Vec3 eyes = new net.minecraft.util.Vec3(camera.posX, camera.posY + camera.height * 0.85F, camera.posZ);
        net.minecraft.util.Vec3 look = camera.getLook(1.0F);
        net.minecraft.util.MovingObjectPosition hit = com.adjustclient.utilities.MinecraftInstance.mc.theWorld.rayTraceBlocks(
                eyes, eyes.addVector(look.xCoord * 300.0, look.yCoord * 300.0, look.zCoord * 300.0), false, false, true);
        if (hit == null || hit.typeOfHit != net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK)
            hit = com.adjustclient.utilities.WorldUtil.pickEntities(camera, false, true, camera.rotationYaw, camera.rotationPitch, 300.0, 300.0, 0.0);
        if (hit == null) return;
        if (hit.typeOfHit == net.minecraft.util.MovingObjectPosition.MovingObjectType.ENTITY) handleTools(hit.entityHit);
        else if (hit.typeOfHit == net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK) {
            net.minecraft.util.BlockPos pos = hit.getBlockPos();
            net.minecraft.block.Block block = com.adjustclient.utilities.MinecraftInstance.mc.theWorld.getBlockState(pos).getBlock();
            camera.setPosition(pos.getX() + 0.5, pos.getY() + block.getBlockBoundsMaxY(), pos.getZ() + 0.5);
        }
    }
    public boolean isMouseDown() {
        return camera != null && camera.state == FreecamState.MAIN && camera.selectedSlot() == 4
                && org.lwjgl.input.Mouse.isButtonDown(1);
    }
    public boolean isRightMouseDown() {
        return camera != null && camera.state == FreecamState.MAIN && camera.selectedSlot() == 4
                && org.lwjgl.input.Mouse.isButtonDown(0) && com.adjustclient.utilities.MinecraftInstance.mc.currentScreen == null
                && com.adjustclient.utilities.MinecraftInstance.mc.inGameHasFocus;
    }
    public void updateTarget() {
        active = null;
        if (!isRightMouseDown()) return;
        net.minecraft.util.Vec3 eyes = new net.minecraft.util.Vec3(camera.posX, camera.posY + camera.height * 0.85F, camera.posZ);
        net.minecraft.util.Vec3 look = camera.getLook(1.0F);
        net.minecraft.util.Vec3 end = eyes.addVector(look.xCoord * 85.0, look.yCoord * 85.0, look.zCoord * 85.0);
        java.util.List<net.minecraft.entity.Entity> entities = com.adjustclient.utilities.MinecraftInstance.mc.theWorld.getEntitiesInAABBexcluding(
                com.adjustclient.utilities.MinecraftInstance.mc.thePlayer,
                camera.getEntityBoundingBox().addCoord(look.xCoord * 85.0, look.yCoord * 85.0, look.zCoord * 85.0).expand(1.0, 1.0, 1.0), this::isSpectating);
        net.minecraft.util.MovingObjectPosition hit = com.adjustclient.utilities.WorldUtil.pickFromEntities(
                camera, false, true, entities, camera.rotationYaw, camera.rotationPitch, 85.0, 85.0, 0.0);
        active = hit == null || hit.typeOfHit == net.minecraft.util.MovingObjectPosition.MovingObjectType.MISS || hit.hitVec == null ? end : hit.hitVec;
    }
    @com.adjustclient.core.events.EventHandler
    public void openCreative(com.adjustclient.core.events.InventoryOpenRequestEvent event) {
        if (!isEnabled() || camera == null) return;
        if (camera.state != FreecamState.HOUSING) return;
        com.adjustclient.utilities.MinecraftInstance.mc.displayGuiScreen(new net.minecraft.client.gui.inventory.GuiContainerCreative(field72));
        event.setCancelled(true);
    }
    @com.adjustclient.core.events.EventHandler
    public void updateRotation(com.adjustclient.core.events.MouseDeltaEvent input) {
        if (!isEnabled() || !isRightMouseDown() || active == null || input.silent) return;
        EntityPlayerSP player = com.adjustclient.utilities.MinecraftInstance.mc.thePlayer;
        com.adjustclient.utilities.Vec2f current = com.adjustclient.core.rotation.handler.RenderRotationTracker.INSTANCE.rotation;
        float yaw = current == null ? player.rotationYaw : current.x;
        float pitch = current == null ? player.rotationPitch : current.y;
        double dx = active.xCoord - player.posX;
        double dy = active.yCoord - (player.posY + player.getEyeHeight());
        double dz = active.zCoord - player.posZ;
        float targetYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float targetPitch = (float) -(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 180.0 / Math.PI);
        float sensitivity = com.adjustclient.utilities.MinecraftInstance.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        double step = sensitivity * sensitivity * sensitivity * 8.0 * 0.1500000059604645;
        float snappedPitch = MathHelper.clamp_float((float) (Math.round((targetPitch - pitch) / step) * step + pitch), -90.0F, 90.0F);
        input.deltaY = Math.round((float) ((snappedPitch - pitch) / 0.15 / input.sensitivity));
        if (!com.adjustclient.utilities.MinecraftInstance.mc.gameSettings.invertMouse) input.deltaY = -input.deltaY;
        float deltaYaw = MathHelper.wrapAngleTo180_float(targetYaw - yaw);
        float snappedYaw = (float) (Math.round(deltaYaw / step) * step);
        input.deltaX = Math.round((float) (((snappedYaw + yaw) - yaw) / 0.15 / input.sensitivity));
        input.silent = true;
    }
    @com.adjustclient.core.events.EventHandler
    public void handleInput(com.adjustclient.core.events.KeyBindingStateEvent event) {
        if (!isEnabled() || camera == null) return;
        net.minecraft.client.settings.GameSettings settings = com.adjustclient.utilities.MinecraftInstance.mc.gameSettings;
        if (event.binding == settings.keyBindUseItem) {
            if (event.pressed) camera.makeNukeTool();
            event.setCancelled(true);
        } else if (event.binding == settings.keyBindAttack) {
            if (event.pressed) camera.rotateTowards();
            event.setCancelled(true);
        }
    }
    @com.adjustclient.core.events.EventHandler
    public void handleServerPosition(com.adjustclient.core.events.TeleportEvent event) {
        if (!isEnabled() || camera == null) return;
        if (!com.adjustclient.utilities.anticheat.ServerTracker.INSTANCE.bedTimer.hasElapsed(250L)) {
            camera.posX = camera.prevPosX = camera.lastTickPosX = event.x;
            camera.posY = camera.prevPosY = camera.lastTickPosY = event.y;
            camera.posZ = camera.prevPosZ = camera.lastTickPosZ = event.z;
            camera.rotationYaw = event.yaw;
            camera.rotationPitch = event.pitch;
            camera.setPosition(event.x, event.y, event.z);
        }
    }
    @com.adjustclient.core.events.EventHandler(priority = -10)
    public void handleMotion(com.adjustclient.core.events.CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled() || camera == null) return;
        com.adjustclient.core.events.MovementInputEvent input = isMouseDown()
                ? new com.adjustclient.core.events.MovementInputEvent(false, false, false, false, false, false)
                : com.adjustclient.core.events.MovementInputEvent.fromKeyboard();
        handleKeys(input);
        updateTarget();
        camera.prevSwingProgress = camera.swingProgress;
        camera.updateArmSwingProgress();
        if (!cameraNoClip.isEnabled()) {
            if (camera.simulation == null) return;
            camera.handleKeyboard();
            camera.simulation.tick();
        }
        if (camera.state == FreecamState.HOUSING && camera.selectedSlot() < 8) {
            if (controller == null) controller = new com.adjustclient.modules.impl.render.freecam.tool.FreecamController(this);
            controller.updateBalance(event);
        } else if (controller != null) {
            controller.stop();
            controller = null;
        }
    }
    @com.adjustclient.core.events.EventHandler
    public void onAction(com.adjustclient.core.events.MouseButtonInputEvent event) {
        if (!isEnabled()) return;
        if (controller != null && event.down) controller.queueAction(camera.getState(), event.button);
    }
    @com.adjustclient.core.events.EventHandler
    public void updateCamera(com.adjustclient.core.events.Render2DEvent event) {
        if (!isEnabled() || controller == null) return;
        if (camera.getState() != null) controller.renderBalance(event);
        if (com.adjustclient.Adjust.gameStateTracker.state instanceof com.adjustclient.utilities.hypixel.game.HousingGameState housing)
            controller.renderProgress(event, MathHelper.clamp_float((float) housing.field52 / housing.field56, 0.0F, 1.0F), 20.0F);
    }
    @com.adjustclient.core.events.EventHandler
    public void render(com.adjustclient.core.events.Render3DEvent event) {
        if (!isEnabled() || camera == null) return;
        camera.prevRotationPitch = camera.rotationPitch;
        camera.prevRotationYaw = camera.rotationYaw;
        if (!cameraNoClip.isEnabled() && camera.simulation != null) {
            com.adjustclient.utilities.SimulationPlayer player = camera.simulation;
            player.rotationYaw = camera.rotationYaw;
            player.prevRotationYaw = camera.prevRotationYaw;
            double x = player.prevPosX + (player.posX - player.prevPosX) * event.partialTicks;
            double y = player.prevPosY + (player.posY - player.prevPosY) * event.partialTicks;
            double z = player.prevPosZ + (player.posZ - player.prevPosZ) * event.partialTicks;
            camera.posX = camera.prevPosX = camera.lastTickPosX = x;
            camera.posY = camera.prevPosY = camera.lastTickPosY = y;
            camera.posZ = camera.prevPosZ = camera.lastTickPosZ = z;
            player.capabilities.allowFlying = true;
        }
        hitPosition = com.adjustclient.utilities.WorldUtil.pickEntities(camera, false, true, camera.rotationYaw, camera.rotationPitch, 1.0, 85.0, 0.0);
        if (hitPosition.typeOfHit != net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK || hitPosition.getBlockPos() == null) return;
        net.minecraft.client.Minecraft mc = com.adjustclient.utilities.MinecraftInstance.mc;
        net.minecraft.util.BlockPos position = hitPosition.getBlockPos();
        net.minecraft.block.Block block = mc.theWorld.getBlockState(position).getBlock();
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        net.minecraft.client.renderer.GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
        org.lwjgl.opengl.GL11.glLineWidth(2.0F);
        net.minecraft.client.renderer.GlStateManager.disableTexture2D();
        if (optifine.Config.isShaders()) shadersmod.client.Shaders.ak();
        net.minecraft.client.renderer.GlStateManager.depthMask(false);
        if (block.getMaterial() != net.minecraft.block.material.Material.air && mc.theWorld.getWorldBorder().contains(position)) {
            block.setBlockBoundsBasedOnState(mc.theWorld, position);
            net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox(block.getSelectedBoundingBox(mc.theWorld, position)
                    .expand(0.002000000094994903, 0.002000000094994903, 0.002000000094994903)
                    .offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ));
        }
        net.minecraft.client.renderer.GlStateManager.depthMask(true);
        net.minecraft.client.renderer.GlStateManager.enableTexture2D();
        if (optifine.Config.isShaders()) shadersmod.client.Shaders.aj();
        net.minecraft.client.renderer.GlStateManager.disableBlend();
    }
    public static void activateForPlayer(EntityPlayer player) {
        if (player instanceof EntityPlayerSP) return;
        FreecamModule module = FreecamModule.INSTANCE;
        if (!module.isEnabled()) module.toggle(true);
        module.handleTools(player);
    }
    public void handleKeys(com.adjustclient.core.events.MovementInputEvent event) {
        double forward = (event.forward ? 1.0 : 0.0) - (event.back ? 1.0 : 0.0);
        double strafe = (event.left ? 1.0 : 0.0) - (event.right ? 1.0 : 0.0);
        boolean sprint = net.minecraft.client.settings.GameSettings.isKeyDown(com.adjustclient.utilities.MinecraftInstance.mc.gameSettings.keyBindSprint);
        double target = 1.0;
        if (sprint) target += (++field84 < 26 ? 0.4 : 0.7) * (field76 + 1.0F);
        else field84 = 0;
        if (target < field76) field76 = Math.max(0.0F, field76 - 0.5F);
        else field76 = (float) Math.min(target, (field76 + 0.1) + (target - (field76 + 0.1)) * 0.2000000029802322);
        double speed = cameraSpeed.getValue().intValue() * 10.0 / 20.0 * Math.max(0.7, field76);
        double length = Math.hypot(forward, strafe);
        if (length > 1.0) { forward /= length; strafe /= length; }
        forward *= speed;
        strafe *= speed;
        double y = event.sneak ? camera.posY - speed : (event.jump ? camera.posY + speed : camera.posY);
        double yaw = Math.toRadians(camera.rotationYaw);
        double strafeYaw = Math.toRadians(camera.rotationYaw - 90.0F);
        double x = camera.posX - Math.sin(yaw) * forward - Math.sin(strafeYaw) * strafe;
        double z = camera.posZ + Math.cos(yaw) * forward + Math.cos(strafeYaw) * strafe;
        if (cameraNoClip.isEnabled()) {
            camera.prevPosX = camera.lastTickPosX = camera.posX;
            camera.prevPosY = camera.lastTickPosY = camera.posY;
            camera.prevPosZ = camera.lastTickPosZ = camera.posZ;
            camera.setPosition(x, y, z);
        } else {
            if (camera.simulation == null) camera.updateSimulation();
            resetBalance(event);
        }
        event.forward = event.back = event.left = event.right = false;
        event.jump = field88 && com.adjustclient.utilities.MinecraftInstance.mc.thePlayer.capabilities.isFlying;
        event.sneak = field88;
    }
    public void handleTools(net.minecraft.entity.Entity entity) {
        net.minecraft.world.World world = com.adjustclient.utilities.MinecraftInstance.mc.theWorld;
        for (double x = -0.5; x <= 0.5; x += 0.5) {
            for (double y = 0.0; y < 2.5; y += 0.5) {
                for (double z = -0.5; z <= 0.5; z += 0.5) {
                    double ox = x + Math.random() * 0.1;
                    double oy = y + Math.random() * 0.1;
                    double oz = z + Math.random() * 0.1;
                    world.spawnParticle(net.minecraft.util.EnumParticleTypes.PORTAL, true,
                            entity.posX, entity.posY, entity.posZ, ox, oy, oz);
                    world.spawnParticle(net.minecraft.util.EnumParticleTypes.PORTAL, true,
                            entity.posX, entity.posY, entity.posZ,
                            x + Math.random() * 0.1, y + Math.random() * 0.1, z + Math.random() * 0.1);
                }
            }
        }
        if (camera.getDistanceSqToEntity(entity) <= 64.0) return;
        double yaw = Math.toRadians(entity.rotationYaw - 180.0F);
        double x = entity.posX - Math.sin(yaw) * 3.5;
        double y = entity.posY + 1.350000023841858;
        double z = entity.posZ + Math.cos(yaw) * 3.5;
        camera.prevPosX = camera.lastTickPosX = x;
        camera.prevPosY = camera.lastTickPosY = y;
        camera.prevPosZ = camera.lastTickPosZ = z;
        camera.setPosition(x, y, z);
        com.adjustclient.utilities.Vec2f rotation = com.adjustclient.utilities.RotationUtil.getYawBetween(
                new net.minecraft.util.Vec3(entity.posX, entity.posY, entity.posZ), new net.minecraft.util.Vec3(x, y, z));
        camera.rotationYaw = rotation.x;
        camera.rotationPitch = rotation.y;
    }
    public void resetBalance(com.adjustclient.core.events.MovementInputEvent event) {
        camera.strafe = (event.left ? 1.0F : 0.0F) - (event.right ? 1.0F : 0.0F);
        camera.forward = (event.forward ? 1.0F : 0.0F) - (event.back ? 1.0F : 0.0F);
        if (event.sneak) { camera.strafe *= 0.3F; camera.forward *= 0.3F; }
        camera.jump = event.jump;
        camera.sneak = event.sneak;
    }
    @Override public void toggle(boolean enabled) {
        if (!enabled && isEnabled() && (camera.state == FreecamState.CHANGE_SPEED || camera.state == FreecamState.HOUSING)) {
            camera.state = FreecamState.MAIN;
            return;
        }
        net.minecraft.client.Minecraft mc = com.adjustclient.utilities.MinecraftInstance.mc;
        if (enabled) {
            enabled = mc.thePlayer != null && mc.theWorld != null;
        }
        try {
            super.toggle(enabled);
        } catch (Throwable error) {
            reportToggleError(error, enabled);
        }
    }
    public static void updateKeyState() {
        net.minecraft.client.settings.KeyBinding.unPressAllKeys();
        for (net.minecraft.client.settings.KeyBinding key : com.adjustclient.modules.impl.movement.inventorymove.InventoryMoveModule.MOVEMENT_KEYS) {
            syncKeyBinding(key);
        }
    }

    private static void syncKeyBinding(net.minecraft.client.settings.KeyBinding key) {
        int code = key.getKeyCode();
        boolean down = false;
        if (code + 100 >= 0 && code + 100 < 0x164 && code != 0) {
            if (code < 0) down = org.lwjgl.input.Mouse.isButtonDown(code + 100);
            else if (org.lwjgl.input.Keyboard.isCreated()) down = org.lwjgl.input.Keyboard.isKeyDown(code);
        }
        if (down) {
            key.pressed = true;
        } else {
            key.pressTime = 0;
            key.pressed = false;
        }
    }
    public void setCamera() {
        if (isEnabled()) toggle(false);
        net.minecraft.client.Minecraft mc = com.adjustclient.utilities.MinecraftInstance.mc;
        toggle(mc.thePlayer != null && mc.theWorld != null);
    }
    @com.adjustclient.core.events.EventHandler
    public void formatItem(com.adjustclient.core.events.DirectionalPacketEvent event) {
        if (event.isCancelled() || !isEnabled() || controller == null || event.outgoing) return;
        net.minecraft.network.Packet<?> packet = event.packet;
        if (!(packet instanceof net.minecraft.network.play.server.S14PacketEntity)
                && !(packet instanceof net.minecraft.network.play.server.S04PacketEntityEquipment)
                && !(packet instanceof net.minecraft.network.play.server.S18PacketEntityTeleport)
                && !(packet instanceof net.minecraft.network.play.server.S19PacketEntityHeadLook)
                && !(packet instanceof net.minecraft.network.play.server.S32PacketConfirmTransaction)) return;
        com.adjustclient.modules.impl.render.freecam.tool.PendingAction action = controller.worker.pending;
        if (action == null || !action.complete) return;
        net.minecraft.network.NetworkManager manager = com.adjustclient.utilities.MinecraftInstance.mc.thePlayer.sendQueue.getNetworkManager();
        manager.isChannelOpen();
        controller.timer.reset();
        controller.execute(action);
        manager.getIsencrypted();
    }
    public static ItemStack readItemData(Item item, String name, int count, String description, boolean enchanted) {
        NBTTagCompound tag = new NBTTagCompound();
        if (!tag.hasKey("display", 10)) tag.setTag("display", new NBTTagCompound());
        tag.getCompoundTag("display").setString("Name", name);
        if (enchanted && !tag.hasKey("ench", 9)) tag.setTag("ench", new NBTTagList());
        ItemStack stack = new ItemStack(item, count, 0);
        stack.setTagCompound(tag);
        return stack;
    }
    public boolean isSpectating(net.minecraft.entity.Entity entity) {
        if (entity == camera) return false;
        if (entity instanceof EntityPlayer player && player.isSpectator()) return false;
        return entity.canBeCollidedWith();
    }
    @Override public void onEnable() {
        net.minecraft.client.Minecraft mc = com.adjustclient.utilities.MinecraftInstance.mc;
        field88 = mc.gameSettings.keyBindSneak.isKeyDown();
        updateKeyState();
        camera = new FreecamCamera(this, mc.theWorld);
        EntityPlayerSP player = mc.thePlayer;
        camera.posX = camera.prevPosX = camera.lastTickPosX = player.posX;
        camera.posY = camera.prevPosY = camera.lastTickPosY = player.posY;
        camera.posZ = camera.prevPosZ = camera.lastTickPosZ = player.posZ;
        camera.setPosition(player.posX, player.posY, player.posZ);
        com.adjustclient.utilities.Vec2f rotation = com.adjustclient.core.rotation.handler.RenderRotationTracker.INSTANCE.rotation;
        camera.rotationYaw = camera.prevRotationYaw = rotation == null ? player.rotationYaw : rotation.x;
        camera.rotationPitch = camera.prevRotationPitch = rotation == null ? player.rotationPitch : rotation.y;
        camera.height = Float.intBitsToFloat(1072959503);
        field40 = field44 = previousView = null;
        camera.tools[0] = field80;
        mc.cameraOverride = camera;
        field52 = null;
    }
    @Override public void onDisable() {
        active = null;
        if (camera != null) { field80 = camera.tools[0]; camera = null; }
        net.minecraft.client.Minecraft mc = com.adjustclient.utilities.MinecraftInstance.mc;
        mc.cameraOverride = null;
        if (mc.thePlayer != null) {
            com.adjustclient.utilities.Vec2f rotation = com.adjustclient.core.rotation.handler.RenderRotationTracker.INSTANCE.rotation;
            mc.thePlayer.renderArmPitch = mc.thePlayer.prevRenderArmPitch = rotation == null ? mc.thePlayer.rotationPitch : rotation.y;
            mc.thePlayer.renderArmYaw = mc.thePlayer.prevRenderArmYaw = rotation == null ? mc.thePlayer.rotationYaw : rotation.x;
        }
        if (controller != null) { controller.stop(); controller = null; }
        updateKeyState();
        syncKeyBinding(mc.gameSettings.keyBindSneak);
    }
    private static final Runnable UNUSED_CALLBACK = () -> { throw new NullPointerException(); };
    private static final java.util.function.ToDoubleFunction<com.adjustclient.utilities.anticheat.entity.TrackedLivingEntity> UNUSED_COMPARATOR = entity -> { throw new NullPointerException(); };
    private static final Runnable NO_CLIP_CHANGED = () -> { if (INSTANCE.camera != null) INSTANCE.camera.makeNoClipTool(); };

    @com.adjustclient.core.events.EventHandler(priority = 41)
    public void onIngameAction(com.adjustclient.core.events.IngameActionEvent event) {
        if (!this.isEnabled() || this.controller == null) return;
        event.suppressActions = true;
        event.actionStage = false;
    }
}
