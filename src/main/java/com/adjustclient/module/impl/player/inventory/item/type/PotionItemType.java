package com.adjustclient.module.impl.player.inventory.item.type;
import com.adjustclient.module.impl.player.inventory.item.ItemType;
import com.adjustclient.module.impl.player.inventory.item.filter.SimpleItemFilter;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.potion.*;
import java.util.List;
public final class PotionItemType extends ItemType {
    public final int potionId;
    public PotionItemType(int potionId) {
        this.potionId = potionId;
        keepFilter = new SimpleItemFilter(999);
        sortKeyFunction = stack -> sortKey(stack);
    }
    @Override public boolean matches(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemPotion)) return false;
        List<PotionEffect> effects = Items.potionitem.getEffects(stack);
        if (effects == null) return false;
        for (PotionEffect effect : effects) {
            int id = effect.getPotionID();
            if ((effect.getDuration() < 20 && effect.getDuration() != 1) || id < 0 || id >= Potion.potionTypes.length) continue;
            Potion potion = Potion.potionTypes[id];
            return potion != null && potionId == potion.id;
        }
        return false;
    }
    public static int sortKey(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemPotion)) return 0;
        float strength = 0.0F;
        for (PotionEffect effect : Items.potionitem.getEffects(stack)) {
            strength += (float) Math.min(3600, Math.max(1, effect.getDuration()))
                    * ((float) Math.max(1, effect.getAmplifier() + 1) * 1.5F);
        }
        return (int) -((Math.min(64, Math.max(0, stack.stackSize)) + strength) * 100.0F);
    }
}
