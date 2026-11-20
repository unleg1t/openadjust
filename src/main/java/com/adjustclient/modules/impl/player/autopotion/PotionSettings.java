package com.adjustclient.modules.impl.player.autopotion;

import com.adjustclient.core.settings.*;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.modules.impl.player.autopotion.condition.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.potion.*;
import java.util.List;

public final class PotionSettings implements MinecraftInstance {
    public final AutoPotionModule owner;
    public final Int2ObjectOpenHashMap<ThrowTypes> effects = new Int2ObjectOpenHashMap<>();
    public final BooleanSetting standingJump;
    public final BooleanSetting moving;
    public final BooleanSetting ground;
    public static final Int2ObjectOpenHashMap<PotionCondition> CONDITIONS = new Int2ObjectOpenHashMap<>();

    static {
        PotionCondition full = new FullEffectCondition();
        CONDITIONS.put(Potion.moveSpeed.getId(), full);
        CONDITIONS.put(Potion.jump.getId(), full);
        PotionCondition effect = new EffectCondition();
        CONDITIONS.put(Potion.resistance.getId(), effect);
        CONDITIONS.put(Potion.healthBoost.getId(), effect);
        CONDITIONS.put(Potion.absorption.getId(), effect);
        CONDITIONS.put(Potion.regeneration.getId(), new RegenerationCondition());
        CONDITIONS.put(Potion.heal.getId(), new HealingCondition());
    }

    public PotionSettings(AutoPotionModule owner) {
        this.owner = owner;
        ExpandSetting types = new ExpandSetting(owner, "Throw Types");
        standingJump = new BooleanSetting(types, "Standing Jump", true);
        standingJump.setDescription("Throw potion above your head and jump. Full potion effects.");
        moving = new BooleanSetting(types, "Moving", true);
        moving.setDescription("Throw potion onto your head when moving forward. Full potion effects.");
        ground = new BooleanSetting(types, "Ground", false);
        ground.setDescription("Throw potion onto the ground. Minimized potion effects.");
        ExpandSetting potions = new ExpandSetting(owner, "Potions");
        register(new ThrowTypes(new BooleanSetting(potions, "Healing", true), Potion.heal.getId(), Potion.regeneration.getId()));
        register(new ThrowTypes(new BooleanSetting(potions, "Resistance", true), Potion.resistance.getId(),
                Potion.fireResistance.getId(), Potion.waterBreathing.getId(), Potion.healthBoost.getId(), Potion.absorption.getId()));
        register(new ThrowTypes(new BooleanSetting(potions, "Swiftness", true), Potion.moveSpeed.getId()));
        register(new ThrowTypes(new BooleanSetting(potions, "Jump Boost", false), Potion.jump.getId()));
        register(new ThrowTypes(new BooleanSetting(potions, "Strength", true), Potion.damageBoost.getId()));
    }

    public boolean shouldThrow(PotionCandidate candidate, EntityPlayer target, double multiplier) {
        if (candidate == null) return false;
        if (!CONDITIONS.containsKey(candidate.potionId)) return true;
        return CONDITIONS.get(candidate.potionId).shouldThrow(target, candidate.amplifier, multiplier);
    }

    public PotionCandidate findCandidate() {
        PotionCandidate best = null;
        int amplifier = Integer.MIN_VALUE;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
            if (stack == null || stack.getItem().getClass() != ItemPotion.class || !ItemPotion.isSplash(stack.getMetadata())) continue;
            List<PotionEffect> list = ((ItemPotion) stack.getItem()).getEffects(stack);
            PotionCandidate candidate = null;
            for (PotionEffect effect : list) {
                int id = effect.getPotionID();
                Potion potion = Potion.potionTypes[id];
                // A bad, disabled or unsupported effect disqualifies the entire mixed potion.
                if (potion.isBadEffect() || !effects.containsKey(id) || !effects.get(id).enabled.isEnabled()) {
                    candidate = null;
                    break;
                }
                if (!potion.isInstant() && effect.getDuration() < 20) continue;
                if (candidate != null || !effects.get(id).enabled.isEnabled() || mc.thePlayer.isPotionActive(id)
                        || owner.isPotionOnCooldown(id)) continue;
                candidate = new PotionCandidate(id, effect.getAmplifier(), slot);
            }
            if (candidate != null && candidate.amplifier > amplifier) {
                amplifier = candidate.amplifier;
                best = candidate;
            }
        }
        return best;
    }

    public PotionSettings register(ThrowTypes setting) {
        for (int id : setting.potionIds) effects.put(id, setting);
        return this;
    }
}
