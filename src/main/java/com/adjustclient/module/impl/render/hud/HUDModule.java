package com.adjustclient.module.impl.render.hud;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.setting.Setting;
import com.adjustclient.game.player.SlotSpoofer;
import com.adjustclient.ui.friends.FriendsScreen;
import com.adjustclient.event.DirectionalPacketEvent;
import com.adjustclient.event.KeyBindingUpdateEvent;
import com.adjustclient.event.KeyInputEvent;
import com.adjustclient.event.ScreenMouseClickEvent;
import com.adjustclient.event.Render2DEvent;
import com.adjustclient.event.ScreenRender2DEvent;
import com.adjustclient.bind.BindMode;
import net.minecraft.network.Packet;
import net.minecraft.world.WorldSettings;
import com.adjustclient.bind.KeyboardKey;
import com.adjustclient.bind.MouseKey;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.render.hud.eventtimer.EventTimers;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ButtonSetting;
import com.adjustclient.setting.ColorSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.KeybindSetting;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.ui.hud.HudElement;
import com.adjustclient.ui.hud.HudElements;
import com.adjustclient.ui.hud.notification.Notifications;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import com.adjustclient.game.movable.SnapGuide;
import com.adjustclient.module.impl.render.ClickGui;
import com.adjustclient.module.impl.render.hud.playerlist.PlayerList;
import com.adjustclient.module.impl.render.hud.target.TargetHud;
import com.adjustclient.module.impl.render.hud.widget.HudWidgets;
import com.adjustclient.render.font.TextRenderer;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.render.ColorUtil;
import com.adjustclient.module.impl.combat.antibot.AntiBotModule;
import com.adjustclient.service.ScreenStack;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.service.party.ui.PartyPanel;
import com.adjustclient.service.party.ui.PartyPingRenderer;
import com.adjustclient.service.prefs.SkinModel;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.ui.chat.ChatChannelManager;
import com.adjustclient.ui.chat.ChatChannelTabs;
import com.adjustclient.ui.tabgui.TabGui;
import com.adjustclient.user.Cape;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.ui.clickgui.ClickGuiScreen;
import com.adjustclient.ui.mainmenu.AdjustMainMenu;

public final class HUDModule extends Module {

    public static HUDModule INSTANCE;
    public static HUDModule INSTANCE_ALT;
    public static ColorSetting colorSetting;
    public static NumberSetting radiance;
    public static BooleanSetting enableSecondary;
    public static ColorSetting secondary;
    public static ModeSetting<com.adjustclient.user.Cape> cape;
    public static Color previousColor;
    public static Animation colorAnimation;
    public static Color animatedColor;
    public static BooleanSetting tabOverlay;
    public static boolean changingCape;

    // ------------------------------------------------------------------ instance fields
    public EnchantInfo[] armorItems;
    public PlayerList playerList;
    public HudElements elements;
    public Notifications notifications;
    public BooleanSetting clickGuiSeparators;
    public SnapGuide armorMovable;
    public ModeSetting<SkinModel> invites;
    public BooleanSetting partySounds;
    public TargetHud targetHud;
    public BooleanSetting armor;
    public EventTimers eventTimers;
    public HudWidgets widgetsHolder;
    public BooleanSetting watermark;
    public List<Setting<?>> displayedSettings;
    public MovingObjectPosition pingTarget;
    public int armorOffset;
    public boolean pinging;

    public HUDModule() {
        super("HUD", Category.RENDER);
        INSTANCE = this;
        INSTANCE_ALT = this;
        this.armorItems = new EnchantInfo[5];

        colorSetting = new ColorSetting(this, "Color", new Color(67, 90, 209), false, true, ColorSetting.Mode.PLAIN, null);
        enableSecondary = new BooleanSetting(this, "Enable Secondary", false, "On", "Off",
                () -> HUDModule.hideEnableSecondary());
        secondary = new ColorSetting(this, "Secondary", new Color(148, 81, 171), false, true, ColorSetting.Mode.PLAIN,
                () -> !isSecondaryActive());
        radiance = new NumberSetting(this, "Radiance", 0F, 0F, 345F, 1F, () -> HUDModule.hideRadiance());
        radiance.field44 = true;
        colorSetting.addListener(HUDModule::syncColorToTheme);
        ServiceUtil.runWhenInitialized((Runnable) HUDModule::syncColorToTheme);
        new ButtonSetting(this, "Randomize colors", HUDModule::randomizeColors);
        this.getSettings().add(UserConfiguration.scale);

        Bind clickGuiBind = Adjust.INSTANCE.bindManager.register("global.hud.clickgui", HUDModule::onClickGuiBind, "Click GUI");
        if (clickGuiBind.mutable) {                        // default keys: Keyboard.KEY_RSHIFT then Keyboard.KEY_INSERT
            clickGuiBind.addKey(new KeyboardKey(org.lwjgl.input.Keyboard.KEY_RSHIFT));
        }
        if (clickGuiBind.mutable) {
            clickGuiBind.addKey(new KeyboardKey(org.lwjgl.input.Keyboard.KEY_INSERT));
        }

        // ---- Widgets ---------------------------------------------------------------------------------------------
        this.widgetsHolder = new HudWidgets();
        ExpandSetting widgets = new ExpandSetting(this, "Widgets");
        this.widgetsHolder.widgets.forEach(widget -> widget.createSettings(widgets));

        // ---- Click GUI -------------------------------------------------------------------------------------------
        ExpandSetting clickGui = new ExpandSetting(this, "Click GUI");
        new KeybindSetting(clickGui, "Bind", clickGuiBind);
        clickGui.getSettings().add(UserConfiguration.secondaryScale);
        this.clickGuiSeparators = new BooleanSetting(clickGui, "Separators", true);
        this.clickGuiSeparators.addListener(HUDModule::onSeparatorsChanged);

        // ---- Other > Watermark -----------------------------------------------------------------------------------
        ExpandSetting other = new ExpandSetting(this, "Other");
        other.enabledPredicate = (BooleanSupplier) () -> this.watermark.isEnabled();
        this.watermark = new BooleanSetting(other, "Watermark", true);

        // ---- HUD element groups ----------------------------------------------------------------------------------
        this.elements = new HudElements(this);
        this.playerList = new PlayerList();
        this.eventTimers = new EventTimers();
        this.notifications = new Notifications();

        // ---- Party / Global Chat / Sense -------------------------------------------------------------------------
        ExpandSetting party = new ExpandSetting(this, "Party");
        ExpandSetting globalChat = new ExpandSetting(this, "Global Chat");
        ExpandSetting sense = new ExpandSetting(this, "Sense");
        sense.enabledPredicate = (BooleanSupplier) () -> UserConfiguration.senseEnabled.isEnabled();
        sense.setDescription("Reveal players in your game who are also using Adjust.");
        sense.getSettings().add(UserConfiguration.senseEnabled);
        UserConfiguration.senseEnabled.addListener(() -> HUDModule.onSenseChangedLambda(this));
        V();
        BooleanSetting tabOverlay = new BooleanSetting(sense, "Tab Overlay", true);
        tabOverlay.setDescription("Show other players' Adjust username in tab.");
        HUDModule.tabOverlay = tabOverlay;
        com.adjustclient.user.Cape[] capeOptions = Arrays.stream(com.adjustclient.user.Cape.values())
                .filter(HUDModule::capeSelectable)
                .toArray(com.adjustclient.user.Cape[]::new);
        cape = new ModeSetting<>(null, "Cape", Cape.NONE, null, capeOptions);
        cape.addListener((oldCape, newCape) -> HUDModule.onCapeChanged((Cape) oldCape, (Cape) newCape));
        sense.getSettings().add(cape);

        // ---- Chat Prefixes -----------------------------------------------------------------------------------------
        ExpandSetting chatPrefixes = new ExpandSetting(this, "Chat Prefixes");
        chatPrefixes.setDescription("Controls prefixes for chat related Adjust features.");
        chatPrefixes.getSettings().addAll(Arrays.asList(UserConfiguration.COMMAND_PREFIX.setting, UserConfiguration.IRC_PREFIX.setting));

        // ---- Invites (created without parent, appended to the module settings below) -----------------------------------
        this.invites = new ModeSetting<>(null, "Invites", SkinModel.EVERYONE,
                SkinModel.EVERYONE, SkinModel.FRIENDS, SkinModel.DISABLED);
        this.invites.setDescription("Controls who can message or invite you in the Adjust universe.");
        this.invites.addListener(() -> HUDModule.onInvitesChangedLambda(this));

        // ---- Party > Pinging -------------------------------------------------------------------------------------------
        ExpandSetting pinging = new ExpandSetting(party, "Pinging");
        Bind pingBind = Adjust.INSTANCE.bindManager.register("global.party.ping", this::onPartyPingBind, "Party Pinging");
        if (pingBind.mutable) {
            pingBind.addKey(new MouseKey(2));              // default: middle mouse button
        }
        new KeybindSetting(pinging, "Ping Bind", pingBind);
        this.partySounds = new BooleanSetting(party, "Sounds", true);
        // ---- Global Chat > Mode ----------------------------------------------------------------------------------------
        globalChat.getSettings().add(UserConfiguration.chatMode);
        UserConfiguration.chatMode.addListener(HUDModule::onGlobalChatModeChanged);

        // ---- Target HUD / Friends ---------------------------------------------------------------------------------------
        this.targetHud = new TargetHud(this);
        ExpandSetting friends = new ExpandSetting(this, "Friends");
        new ButtonSetting(friends, "Open GUI", HUDModule::openFriendsGui);
        Bind friendsBind = Adjust.INSTANCE.bindManager.register("global.hud.friends", HUDModule::onFriendsGuiBind, "Friends GUI");
        if (friendsBind.mutable) {
            friendsBind.addKey(new KeyboardKey(org.lwjgl.input.Keyboard.KEY_HOME));
        }
        new KeybindSetting(friends, "Friends GUI Bind", friendsBind);
        ExpandSetting friendNotifications = new ExpandSetting(friends, "Notifications");
        friendNotifications.getSettings().add(UserConfiguration.onlineNotifications);

        // ---- Armor -------------------------------------------------------------------------------------------------------
        this.armor = new BooleanSetting(this, "Armor", true);
        this.armorMovable = new SnapGuide();
        Adjust.movableManager.guides.add(this.armorMovable);
        this.armorMovable.active = true;
        this.getSettings().add(this.invites);              // Invites is the last top level setting

        // ---- widget layout bookkeeping ---------------------------------------------------------------------------------
        this.displayedSettings = new ArrayList<>(this.getSettings());
        widgets.visibleSettings = new ArrayList<>(widgets.getSettings());
        widgets.visibleSettings.addAll(0, List.of(this.playerList.group, this.eventTimers.group));
        this.displayedSettings.removeAll(widgets.visibleSettings);
        this.setEnabled(true, false);
        this.setHidden(false);
    }

    @Override
    public List<Setting<?>> B_() {
        return this.displayedSettings;
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            return;
        }
        ((com.adjustclient.service.prefs.PreferenceCodec) Adjust.INSTANCE.screenStack.preferences.field8.codec).value = Integer.valueOf(((Color) colorSetting.getValue()).getRGB());
    }

    @Override
    public void onDisable() {
        this.armorOffset = 0;
    }

    /** "Secondary colour is in use": Enable Secondary is on (and not hidden) and the HUD colour is a plain colour. */
    public static boolean isSecondaryActive() {
        return enableSecondary.isEnabled() && colorSetting.getMode() == ColorSetting.Mode.PLAIN;
    }

    /** The current HUD colour: the animated colour while a transition runs, else the HUD > Color setting. */
    public static Color getHudColor() {
        if (animatedColor != null) {
            return animatedColor;
        }
        return colorSetting.getColor();
    }

    /** {@link #getHudColor()} with the alpha replaced (clamped to 0..255). */
    public static Color getColor(int alpha) {
        Color base = animatedColor != null ? animatedColor : colorSetting.getColor();
        int a = MathHelper.clamp_int(alpha, 0, 255);
        if (a != base.getAlpha()) {
            return new Color(base.getRed(), base.getGreen(), base.getBlue(), a);
        }
        return base;
    }

    /** Starts the 185 ms transition from {@code previous} to the (new) HUD colour. */
    public static void startColorTransition(Color previous) {
        previousColor = previous;
        colorAnimation = new Animation(Easing.LINEAR, 185L);
        updateColorTransition();
    }

    /** Advances the colour transition: blends previousColor -> HUD > Color; clears the animation when it has finished. */
    public static void updateColorTransition() {
        Animation anim = colorAnimation;
        if (anim == null) {
            return;
        }
        float progress = anim.update(1.0F);
        if (anim.finished) {
            colorAnimation = null;
            animatedColor = null;
            return;
        }
        animatedColor = ColorUtil.blend((Color) colorSetting.getValue(), previousColor, progress);
    }

    public static float getHudScale() {
        int s = UserConfiguration.scale.getInt();
        if (s == 16) {
            return 1000.0F;
        }
        return (float) ((double) (s - 1) * 0.25 + 2.0);
    }

    public static float getClickGuiScale() {
        int s = UserConfiguration.secondaryScale.getInt();
        if (s == 16) {
            return 1000.0F;
        }
        return (float) ((double) (s - 1) * 0.25 + 2.0);
    }

    public static void V() {
        boolean enabled = UserConfiguration.senseEnabled.isEnabled();
        Adjust.INSTANCE.screenStack.preferences.booleanPreference.codec.value = Boolean.valueOf(enabled);
        Adjust.INSTANCE.screenStack.preferences.booleanPreference.codec.lastSentValue = null;
    }

    public static MovingObjectPosition getPingTarget() {
        Minecraft mc = Minecraft.getMinecraft();
        Entity view = mc.getRenderViewEntity() != null ? mc.getRenderViewEntity() : mc.thePlayer;
        double reach = 16 * mc.gameSettings.renderDistanceChunks;
        float yaw = view.rotationYaw;
        float pitch = view.rotationPitch;
        net.minecraft.util.Vec3 look = view.getVectorForRotation(pitch, yaw);
        net.minecraft.util.AxisAlignedBB box = view.getEntityBoundingBox()
                .addCoord(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach)
                .expand(1.0, 1.0, 1.0);
        List<Entity> entities = mc.theWorld.getEntitiesInAABBexcluding(view, box,
                com.google.common.base.Predicates.and(net.minecraft.util.EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
        List<Entity> candidates = entities.stream().filter(HUDModule::antiBotTest).toList();
        List<org.apache.commons.lang3.tuple.Pair<Entity, net.minecraft.util.AxisAlignedBB>> pairs = new ArrayList<>();
        for (Entity e : candidates) {
            pairs.add(org.apache.commons.lang3.tuple.ImmutablePair.of(e, e.getEntityBoundingBox()));
        }
        return com.adjustclient.util.WorldUtil.pickFromBounds(view, false, true, pairs, yaw, pitch, reach, reach, 0.3);
    }

    /** While the ping bind is held: recompute the target every call and preview it in the party manager; otherwise clear it. */
    public void updatePingTarget() {
        if (!this.pinging) {
            this.pingTarget = null;
            return;
        }
        this.pingTarget = getPingTarget();
        MovingObjectPosition target = this.pingTarget;
        if (target == null) {
            return;
        }
        PartyPanel party = Adjust.INSTANCE.screenStack.partyService.party;
        if (target.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            party.previewEntityPing(target.entityHit);
        } else if (target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            net.minecraft.util.BlockPos pos = target.getBlockPos();
            party.pingRenderer.previewBlock((double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
        } else {
            party.pingRenderer.setPreview(null);
            this.pingTarget = null;
        }
    }

    private void onPartyPingBind(BindMode mode) {
        PartyPanel party = Adjust.INSTANCE.screenStack.partyService.party;
        if (party == null) {
            this.pinging = false;
            this.pingTarget = null;
            return;
        }
        int c = HudSwitchMap.MODE[mode.ordinal()];
        if (c != 1) {                                      // PRESS
            if (c == 2) {
                this.pinging = true;
            }
            return;
        }
        // RELEASE: send the ping
        party.pingRenderer.setPreview(null);
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.objectMouseOver == null
                || mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            this.pingTarget = null;
            this.pinging = false;
            return;
        }
        MovingObjectPosition target = this.pingTarget != null ? this.pingTarget : getPingTarget();
        if (target != null && target.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
            if (target.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                party.sendEntityPing(mc.thePlayer.dimension, target.entityHit);
            } else if (target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                net.minecraft.util.BlockPos pos = target.getBlockPos();
                party.sendBlockPing(mc.thePlayer.dimension, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
            } else {
                this.pingTarget = null;
                this.pinging = false;
                return;
            }
        } else {
            new com.adjustclient.ui.hud.notification.NotificationBuilder("Party").addText("Couldn't ping that location!", 0xFFBEBCBD).show();
        }
        this.pingTarget = null;
        this.pinging = false;
    }

    public void updateArmorItems() {
        if (!this.armor.isEnabled()) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        SlotSpoofer sm = Adjust.slotSpoofer;
        net.minecraft.entity.player.InventoryPlayer inv = mc.thePlayer.inventory;
        ItemStack held;
        if (sm != null && Adjust.slotSpoofer.active && !Adjust.slotSpoofer.renderSpoofed) {
            held = (inv.currentItem >= 0 && inv.currentItem < 9) ? inv.mainInventory[inv.currentItem] : null;
        } else {
            held = inv.getCurrentItem();
        }
        EnchantInfo cur = this.armorItems[0];
        if (held == null) {
            if (cur != null) {
                this.armorItems[0] = null;
            }
        } else if (cur == null || cur.stack != held && !ItemStack.areItemStacksEqual(cur.stack, held)) {
            this.armorItems[0] = new EnchantInfo(held);
            EnchantInfo e0 = this.armorItems[0];
            if (e0.lines.isEmpty() && !e0.isDamaged()) {
                this.armorItems[0] = null;
            }
        }
        ItemStack[] armorInv = mc.thePlayer.inventory.armorInventory;
        setArmorItem(4, armorInv[0]);
        if (armorInv.length >= 2) {
            setArmorItem(3, armorInv[1]);
            if (armorInv.length >= 3) {
                setArmorItem(2, armorInv[2]);
                if (armorInv.length >= 4) {
                    setArmorItem(1, armorInv[3]);
                }
            }
        }
    }

    /** Replaces slot {@code slot} of the EnchantInfo array when the stack changed. */
    private void setArmorItem(int slot, ItemStack stack) {
        EnchantInfo cur = this.armorItems[slot];
        if (stack == null && cur == null) {
            return;
        }
        if (stack == null) {
            this.armorItems[slot] = null;
            return;
        }
        if (cur != null && (cur.stack == stack || ItemStack.areItemStacksEqual(cur.stack, stack))) {
            return;
        }
        this.armorItems[slot] = new EnchantInfo(stack);
        if (slot == 0) {
            EnchantInfo e0 = this.armorItems[0];
            if (e0 != null && e0.lines.isEmpty() && !e0.isDamaged()) {
                this.armorItems[0] = null;
            }
        }
    }

    /** Draws the held item and the four armor pieces above the hotbar, plus their enchantment/durability text. */
    public void renderArmor(net.minecraft.client.gui.ScaledResolution originalResolution) {
        if (!this.armor.isEnabled()) {
            this.armorOffset = 0;
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        WorldSettings.GameType mode = mc.playerController.currentGameType;
        if (mode != WorldSettings.GameType.SURVIVAL && mode != WorldSettings.GameType.ADVENTURE) {
            this.armorOffset = 0;
            return;
        }
        float hudScale = getHudScale();
        double guiScale = mc.gameSettings.guiScale;
        net.minecraft.client.gui.ScaledResolution sr = originalResolution;
        if (hudScale != guiScale) {
            sr = new net.minecraft.client.gui.ScaledResolution(mc, hudScale);
            mc.entityRenderer.setupOverlayRendering(sr);
            NvgContext.setScale((float) sr.getScaleFactor_double());
        }
        int x0 = sr.getScaledWidth() / 2 + 9;
        boolean inWater = mc.thePlayer.isInsideOfMaterial(net.minecraft.block.material.Material.water);
        int y = sr.getScaledHeight() - (inWater ? 66 : 56);
        int offset = inWater ? 17 : 7;
        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        int x = x0;
        for (EnchantInfo info : this.armorItems) {
            if (info == null) {
                continue;
            }
            info.x = x;
            info.y = y;
            mc.getRenderItem().renderItemAndEffectIntoGUI(info.stack, x, y);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, info.stack, x, y, null);
            x += 17;
        }
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        for (EnchantInfo info : this.armorItems) {
            if (info == null) {
                continue;
            }
            net.minecraft.client.renderer.GlStateManager.disableLighting();
            net.minecraft.client.renderer.GlStateManager.disableDepth();
            for (int i = 0; i < info.lines.size(); i++) {
                String line = info.lines.get(i);           // abbreviated enchantment text, e.g. "Pro 4", "Silk"
                Fonts.TAHOMA_8.drawString(net.minecraft.util.ChatColor.BOLD + line,
                        (float) info.x, ((float) info.y + 2.5F - Fonts.TAHOMA_8.size) - (float) (4 * i), 0xFFFAFAFB);
            }
            if (info.isDamaged()) {
                // remaining durability (or "Inf") drawn with Color(235, 235, 245, 255) at (x + 8.5F, y + 9.0F)
                Fonts.TAHOMA_10.drawCenteredString(net.minecraft.util.ChatColor.BOLD + info.durabilityText,
                        (float) info.x + 8.5F, (float) info.y + 9.0F, 0xFFEBEBF5);
            }
            net.minecraft.client.renderer.GlStateManager.enableLighting();
            net.minecraft.client.renderer.GlStateManager.enableDepth();
        }
        net.minecraft.client.renderer.GlStateManager.disableLighting();
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (hudScale != guiScale) {
            mc.entityRenderer.setupOverlayRendering(originalResolution);
            NvgContext.setScale((float) originalResolution.getScaleFactor_double());
        }
        this.armorOffset = offset;
    }

    /** Render2D handler. */
    @com.adjustclient.event.EventHandler(priority = -10)
    public void onRender2D(ScreenRender2DEvent e) {
        if (!this.isEnabled()) {
            return;
        }
        net.minecraft.client.gui.ScaledResolution sr = e.scaledResolution;
        this.armorMovable.x = (float) (0.5 * sr.getScaledWidth_double());
        this.armorMovable.y = (float) (0.5 * sr.getScaledHeight_double());
        if (this.playerList.enabled.isEnabled() && this.playerList.holdBind.isHeld()) {
            this.playerList.render(e);
        } else {
        }
        if (this.eventTimers.enabled.isEnabled()) {
            this.eventTimers.render(e);
        }
        this.elements.render(sr);
        renderArmor(sr);
        if (this.targetHud.enabled.isEnabled()) {
            // Target HUD: drawn at the screen centre + (18, 18) in the HUD colour
            float tx = (float) ((float) sr.getScaledWidth() * 0.5F) + 18.0F;
            float ty = (float) ((float) sr.getScaledHeight() * 0.5F) + 18.0F;
            this.targetHud.render(e, colorSetting.getColor(), tx, ty);
        }
    }

    /** Tick handler. */
    @com.adjustclient.event.EventHandler(priority = -20)
    public void onTick(com.adjustclient.event.CancellablePlayerUpdateEvent e) {
        if (e.isCancelled()) return;
        this.onTick();
    }

    public void onTick() {
        if (!this.isEnabled()) {
            return;
        }
        if (this.playerList.enabled.isEnabled() && this.playerList.holdBind.isHeld()) {
            this.playerList.tick();
        }
        if (this.eventTimers.enabled.isEnabled()) {
            this.eventTimers.tick();
        }
        for (HudElement element : this.elements.elements) {
            element.update();
        }
        updateArmorItems();
        this.targetHud.tick();
    }

    @com.adjustclient.event.EventHandler
    public void onKey(KeyBindingUpdateEvent e) {
        if (e.isCancelled() || !this.isEnabled()) {
            return;
        }
        for (HudElement element : this.elements.elements) {
            if (element.isEnabled() && element.onKey(e.keyCode)) {
                e.cancelled = true;
                return;
            }
        }
    }

    @com.adjustclient.event.EventHandler
    public void onKeyboardInput(KeyInputEvent e) {
        if (e.isCancelled() || !this.isEnabled()) {
            return;
        }
        org.lwjgl.input.Keyboard.KeyEvent keyEvent =
                new org.lwjgl.input.Keyboard.KeyEvent(e.character, e.keyCode, e.down, 0L, false);
        ((TabGui) this.elements.field8).forwardKeyEvent(keyEvent);
        if (keyEvent.consumed) {
            e.cancelled = true;
        }
    }

    @com.adjustclient.event.EventHandler
    public void onMouseButton(com.adjustclient.event.MouseButtonInputEvent e) {
        if (e.isCancelled() || !this.isEnabled() || e.button < 0) {
            return;
        }
        for (HudElement element : this.elements.elements) {
            if (element.isEnabled() && element.onMouse(e.down ? 1 : 0, e.button)) {
                e.cancelled = true;
                return;
            }
        }
    }

    /** Mouse click handler. */
    @com.adjustclient.event.EventHandler
    public void onMouse(ScreenMouseClickEvent e) {
        if (!this.isEnabled()) {
            return;
        }
        if (e.inputAction == BindMode.PRESSED && e.mouseButton == 0) {
            if (this.notifications.runClick()) {
                e.cancelled = true;
                return;
            }
        }
        if (e.inputAction != BindMode.PRESSED && e.mouseButton != -1) {
            return;
        }
        if (this.playerList.enabled.isEnabled() && this.playerList.holdBind.isHeld()) {
            if (this.playerList.onMouse(e.mouseButton, e.mouseX, e.mouseY)) {
                e.cancelled = true;
            }
        }
    }

    /** Packet handler: keeps the Target HUD target in sync (own attacks set it, join/respawn/teleport clear it). */
    @com.adjustclient.event.EventHandler
    public void onPacket(DirectionalPacketEvent e) {
        if (e.isCancelled() || !this.isEnabled() || !this.targetHud.enabled.isEnabled()) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        Packet<?> packet = e.packet;
        if (e.outgoing) {
            if (packet instanceof net.minecraft.network.play.client.C02PacketUseEntity
                    && ((net.minecraft.network.play.client.C02PacketUseEntity) packet).getAction() == net.minecraft.network.play.client.C02PacketUseEntity.Action.ATTACK) {
                Entity target = ((net.minecraft.network.play.client.C02PacketUseEntity) packet).getEntityFromWorld(mc.theWorld);
                if (target != null && target instanceof net.minecraft.entity.player.EntityPlayer
                        && !AntiBotModule.isBot(target.entityId)) {
                    mc.addScheduledTask(() -> this.targetHud.setTarget((net.minecraft.entity.player.EntityPlayer) target));
                }
            }
            return;
        }
        if (packet instanceof net.minecraft.network.play.server.S01PacketJoinGame
                || packet instanceof net.minecraft.network.play.server.S07PacketRespawn) {
            mc.addScheduledTask(() -> this.targetHud.clear());
        } else if (packet instanceof net.minecraft.network.play.server.S08PacketPlayerPosLook) {
            if (mc.thePlayer == null) {
                return;
            }
            net.minecraft.network.play.server.S08PacketPlayerPosLook pl = (net.minecraft.network.play.server.S08PacketPlayerPosLook) packet;
            double dx = mc.thePlayer.posX - pl.getX();
            double dy = mc.thePlayer.posY - pl.getY();
            double dz = mc.thePlayer.posZ - pl.getZ();
            if (dx * dx + dy * dy + dz * dz > 1024.0) {    // teleport further than 32 blocks
                mc.addScheduledTask(() -> this.targetHud.clear());
            }
        }
    }

    @com.adjustclient.event.EventHandler
    public void onDisconnect(com.adjustclient.event.DisconnectTeardownEvent event) {
        if (this.isEnabled() && this.playerList != null) {
            this.playerList.clearCaches();
        }
    }

    /** Resets the per-session HUD state (colour sync, player list caches, event timers). */
    public void reset() {
        // 1. same colour sync as onEnable
        ((com.adjustclient.service.prefs.PreferenceCodec) Adjust.INSTANCE.screenStack.preferences.field8.codec).value = Integer.valueOf(((Color) colorSetting.getValue()).getRGB());
        if (this.playerList.enabled.isEnabled() && this.playerList.holdBind.isHeld()) {
            if (this.playerList.columns.isEnabled(com.adjustclient.module.impl.render.hud.playerlist.column.PlayerListColumn.SEEN)) {
                this.playerList.seen.forEach(this.playerList::countPlayer);
            } else {
                this.playerList.counts.clear();
            }
            this.playerList.seen.clear();
            this.playerList.reload();
            this.playerList.scrollAnimation.value = 0;
            this.playerList.scroll = 0;
        }
        if (this.eventTimers.enabled.isEnabled()) {
            this.eventTimers.entries = null;
            this.eventTimers.apiEvents = null;
            this.eventTimers.lastFetch = null;
        }
    }

    /** Refreshes the keybind rows of the Tab GUI after a bind change. */
    @com.adjustclient.event.EventHandler
    public void refreshTabGuiKeybinds(com.adjustclient.event.BindChangeEvent event) {
        TabGui tabGui = (TabGui) this.elements.field8;
        if (tabGui.root != null && tabGui.settings.enabled.isEnabled()) {            // TabGuiSettings.enabled
            tabGui.visitKeybindNodes(new TabGui.RefreshKeybindNodes(event), tabGui.root);
        }
    }

    private static boolean hideEnableSecondary() {
        return colorSetting.getMode() != ColorSetting.Mode.PLAIN;
    }

    private static boolean hideRadiance() {
        return false;
    }

    private static void onGlobalChatModeChanged() {
        ChatChannelManager chat = Adjust.chatManager;
        if (chat == null) {
            return;
        }
        com.adjustclient.service.globalchat.GlobalChatChannel channel = chat.globalChannel;
        if (channel == null) {
            return;
        }
        if (UserConfiguration.chatMode.getValue() == GlobalChatMode.DISABLED) {
            chat.removeChannel(channel);
        } else {
            chat.addChannel(channel);
        }
        net.minecraft.client.gui.GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof net.minecraft.client.gui.GuiChat) {
            ((net.minecraft.client.gui.GuiChat) screen).chatChannelTabs.rebuild();
        }
    }

    private static void onClickGuiBind(BindMode mode) {
        if (mode != BindMode.PRESSED) {
            return;
        }
        if (AdjustMainMenu.clientUiAllowed) {
            com.adjustclient.ui.clickgui.ClickGuiScreen gui = (com.adjustclient.ui.clickgui.ClickGuiScreen) ClickGuiScreen.INSTANCE;
            if (gui == null) {
                gui = com.adjustclient.ui.clickgui.ClickGuiScreen.instance();
                ClickGuiScreen.INSTANCE = gui;
            }
            if (!gui.fade.closing) {
                if (ClickGui.isOpen()) {
                    ClickGui.openGlobalConfigurations();
                } else {
                    ClickGui.openClickGui();
                }
            }
        }
    }

    private static void onFriendsGuiBind(BindMode mode) {
        if (mode == BindMode.PRESSED) {
            openFriendsGui();
        }
    }

    private static void openFriendsGui() {
        ScreenStack cfg = Adjust.INSTANCE.screenStack;
        if (AdjustMainMenu.clientUiAllowed) {
            FriendsScreen screen = cfg.friendsScreen != null ? cfg.friendsScreen : (cfg.friendsScreen = new FriendsScreen());
            com.adjustclient.service.util.ServiceUtil.closeOverlay(ScreenStack.deferOverlayChanges, () -> cfg.openOverlay(screen));
        }
    }

    private static void onSeparatorsChanged() {
        var clickGui = (com.adjustclient.ui.clickgui.ClickGuiScreen) ClickGuiScreen.INSTANCE;
        if (clickGui != null) {
            clickGui.layoutDirty = true;
        }
    }

    private static void syncColorToTheme() {
        if (INSTANCE_ALT == null) {
            return;
        }
        ScreenStack cfg = Adjust.INSTANCE != null ? Adjust.INSTANCE.screenStack : null;
        if (cfg == null) {
            return;
        }
        if (cfg.partyService != null && cfg.partyService.party != null) {
            cfg.partyService.party.title = null;
        }
        if (cfg.preferences != null && cfg.preferences.field8 != null && cfg.preferences.field8.codec != null) {
            ((com.adjustclient.service.prefs.PreferenceCodec) cfg.preferences.field8.codec).value = Integer.valueOf(((Color) colorSetting.getValue()).getRGB());
        }
    }

    private static void randomizeColors() {
        java.util.Random random = RandomUtil.RANDOM;
        Color primary = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
        colorSetting.setValue(primary);
        if (enableSecondary.isEnabled() && colorSetting.getMode() == ColorSetting.Mode.PLAIN) {
            Color second = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
            secondary.setValue(second);
        }
    }

    private static void onSenseChangedLambda(HUDModule self) {
        Objects.requireNonNull(self);
        V();
    }

    private static void onInvitesChangedLambda(HUDModule self) {
        ((com.adjustclient.service.prefs.PreferenceCodec) Adjust.INSTANCE.screenStack.preferences.skinModel.codec).value = self.invites.getValue();
    }

    private static void onCapeChanged(Cape oldValue, Cape newValue) {
        changingCape = true;
        UserConfiguration.INSTANCE.cape = newValue;
        if (Adjust.INSTANCE != null && Adjust.INSTANCE.screenStack != null && Adjust.INSTANCE.screenStack.preferences != null
                && Adjust.INSTANCE.screenStack.preferences.cape != null && Adjust.INSTANCE.screenStack.preferences.cape.codec != null) {
            ((com.adjustclient.service.prefs.PreferenceCodec) Adjust.INSTANCE.screenStack.preferences.cape.codec).value = newValue;
        }
        changingCape = false;
    }

    private static boolean capeSelectable(com.adjustclient.user.Cape cape) {
        return !cape.field32;
    }

    private static boolean antiBotTest(Entity entity) {
        return !AntiBotModule.isBot(entity.entityId);
    }

    private static float entityVirtual208(Entity entity) {
        return 0.0F;
    }

}
