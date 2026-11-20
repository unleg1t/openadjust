package com.adjustclient.utilities;
import java.util.*;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.potion.PotionEffect;
public final class ItemStackBuilder {
    public Item item;
    public Object field8;
    public Object field12;
    public Object2IntOpenHashMap<Enchantment> enchantments = new Object2IntOpenHashMap<>();
    public Object2IntOpenHashMap<Enchantment> storedEnchantments = new Object2IntOpenHashMap<>();
    public List<PotionEffect> potionEffects = new ArrayList<>();
    public Map<String, NBTBase> tags = new HashMap<>();
    public int count = 1;
    public int damage;
    public int hideFlags = -1;
    public int field44;
    public Object field48;
    public ItemStack build() {
        ItemStack stack = new ItemStack(item, count, Math.max(0, damage));
        for (var entry : enchantments.object2IntEntrySet())
            if (entry.getKey() != null) stack.addEnchantment(entry.getKey(), entry.getIntValue());
        if (!storedEnchantments.isEmpty()) {
            NBTTagList list = new NBTTagList();
            for (var entry : storedEnchantments.object2IntEntrySet()) {
                if (entry.getKey() == null) continue;
                NBTTagCompound tag = new NBTTagCompound();
                tag.setShort("id", (short) entry.getKey().effectId);
                tag.setShort("lvl", (short) entry.getIntValue());
                list.appendTag(tag);
            }
            if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setTag("StoredEnchantments", list);
        }
        if (!potionEffects.isEmpty()) {
            NBTTagList list = new NBTTagList();
            for (PotionEffect effect : potionEffects) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Id", (byte) effect.getPotionID());
                tag.setByte("Amplifier", (byte) effect.getAmplifier());
                tag.setInteger("Duration", effect.getDuration());
                tag.setBoolean("Ambient", effect.getIsAmbient());
                tag.setBoolean("ShowParticles", effect.getIsShowParticles());
                list.appendTag(tag);
            }
            if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setTag("CustomPotionEffects", list);
        }
        if (hideFlags != -1) {
            if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setInteger("HideFlags", hideFlags);
        }
        for (String key : tags.keySet()) {
            if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setTag(key, tags.get(key));
        }
        return stack;
    }
}
