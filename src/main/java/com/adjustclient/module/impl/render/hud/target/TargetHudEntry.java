package com.adjustclient.module.impl.render.hud.target;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatColor;

import java.util.HashMap;
import com.adjustclient.Adjust;
import com.adjustclient.module.impl.exploit.playerdetect.PlayerDetectModule;
import com.adjustclient.module.impl.render.streamer.StreamerModule;
import com.adjustclient.service.servertracker.FriendServerTracker;

final class TargetHudEntry {

    Animation fade;
    EntityPlayer target;
    net.minecraft.util.ResourceLocation skin;
    ItemStack[] items;
    Animation healthAnim;
    Animation trailAnim;
    Animation absorptionAnim;
    String name;
    String clientTag;
    float minWidth;
    float width;
    float height;
    float healthRatio;
    float absorptionRatio;
    float healthChange;
    int ticks;
    boolean fadingOut;

    TargetHudEntry(EntityPlayer target, int ticks) {
        this.fade = new Animation(Easing.EASE_OUT_EXPO, 300L);
        this.target = target;
        net.minecraft.client.network.NetworkPlayerInfo info = net.minecraft.client.Minecraft.getMinecraft()
                .thePlayer.sendQueue.getPlayerInfo(target.getUniqueID());
        this.skin = info != null ? info.getLocationSkin() : net.minecraft.client.resources.DefaultPlayerSkin.getDefaultSkin(
                EntityPlayer.getUUID(target.getGameProfile()));
        this.items = new ItemStack[5];
        this.minWidth = 80.0F;
        this.width = 80.0F;
        this.ticks = ticks;
        this.fadingOut = false;
        this.update();
    }

    /** Per-tick refresh (called from {@link TargetHud#tick()} while the target is valid). */
    void update() {
        this.height = 24.0F;
        this.items[0] = this.target.inventory.getCurrentItem();
        ItemStack[] armor = this.target.inventory.armorInventory;
        this.items[1] = armor[3];
        this.items[2] = armor[2];
        this.items[3] = armor[1];
        this.items[4] = armor[0];

        float maxHealth = this.target.getMaxHealth();                                    // EntityLivingBase.bU
        float health = ((Float) this.target.getDataWatcher().getWatchedObject(6).getObject()).floatValue();
        float shownHealth = Math.min(health, maxHealth);
        this.healthRatio = shownHealth / maxHealth;
        if (this.healthAnim == null) {
            this.healthAnim = new Animation(Easing.EASE_OUT_EXPO, 220L);
            this.healthAnim.value = this.healthRatio;                                  // starts at the current value
            this.trailAnim = new Animation(Easing.EASE_OUT_EXPO, 1660L);
            this.trailAnim.value = this.healthRatio;
        }
        // absorption (DataWatcher id 17)
        Object absorptionObject = this.target.getDataWatcher().getWatchedObject(17).getObject();
        float absorption = absorptionObject instanceof Float ? ((Float) absorptionObject).floatValue() : 0.0F;
        float shownAbsorption = Math.min(absorption, maxHealth);
        if (shownAbsorption > 0.0F) {
            this.absorptionRatio = shownAbsorption / maxHealth;
            if (this.absorptionAnim == null) {
                this.absorptionAnim = new Animation(Easing.EASE_OUT_EXPO, 220L);
                this.absorptionAnim.value = this.absorptionRatio;
            }
        } else {
            this.absorptionAnim = null;
            this.absorptionRatio = 0.0F;
        }

        // display name: profile name, replaced by the alias of the nickname map when there is one
        String profileName = this.target.getGameProfile().getName();
        this.name = profileName;
        com.adjustclient.module.impl.exploit.playerdetect.hypixel.nick.NickDetector nickDetector = PlayerDetectModule.INSTANCE.nickDetector;
        java.util.Map<String, com.adjustclient.module.impl.exploit.playerdetect.hypixel.nick.NickedPlayer> nicknames = nickDetector == null ? null : nickDetector.nickedPlayers;
        if (nicknames != null && nicknames.containsKey(profileName)) {
            com.adjustclient.module.impl.exploit.playerdetect.hypixel.nick.NickedPlayer alias = nicknames.get(profileName);
            if (alias == null) {
                this.name = ChatColor.ITALIC + this.name;
            } else {
                this.name = alias.realName;
            }
        }

        // Adjust client tag of the player
        net.minecraft.entity.player.EntityPlayer self = net.minecraft.client.Minecraft.getMinecraft().thePlayer;
        if (this.target == self) {
            var userManager = Adjust.INSTANCE != null ? (com.adjustclient.property.UserManager) Adjust.INSTANCE.field40 : null;
            var selfUser = userManager != null ? (com.adjustclient.user.ClientUser) userManager.field16 : null;
            this.clientTag = selfUser != null ? selfUser.name : null;
        } else {
            com.adjustclient.user.ClientUser info = Adjust.INSTANCE.screenStack.serverTracker.findUser(this.target.getGameProfile().getName());
            this.clientTag = info == null ? null : info.name;
        }

        // the value shown as "+n" / "-n": (own absorption + own health) - (target absorption + target health), own = Minecraft.thePlayer
        Object ownAbsorptionObject = self.getDataWatcher().getWatchedObject(17).getObject();
        float ownAbsorption = ownAbsorptionObject instanceof Float ? ((Float) ownAbsorptionObject).floatValue() : 0.0F;
        float ownTotal = ownAbsorption + self.getDataWatcher().getWatchableObjectFloat(6);
        this.healthChange = ownTotal - (this.target.getDataWatcher().getWatchableObjectFloat(17) + this.target.getDataWatcher().getWatchableObjectFloat(6));

        // box width: name + 21, plus the client tag and 2.5; the width only ever grows
        String shownName = this.name;
        if (StreamerModule.nameMap != null) {                                                  // Streamer module active: hide the real name
            shownName = StreamerModule.replaceName(this.name, this.target.getGameProfile().getName());
        }
        float nameWidth = Fonts.TAHOMA_12.getStringWidth(shownName) + 21.0F;
        if (this.clientTag != null) {
            nameWidth = (Fonts.TAHOMA_12.getStringWidth(this.clientTag) + 2.5F) + nameWidth;
        }
        if (nameWidth >= this.minWidth) {
            this.width = nameWidth;
        }
    }
}
