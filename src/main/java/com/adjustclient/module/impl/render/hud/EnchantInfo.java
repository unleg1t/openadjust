package com.adjustclient.module.impl.render.hud;

import net.minecraft.enchantment.Enchantment;
import com.adjustclient.render.font.Fonts;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;
import net.minecraft.util.ChatColor;

public final class EnchantInfo {

    public final ItemStack stack;
    public final List<String> lines;
    public final String durabilityText;
    public int x;
    public int y;

    /** Sort key of the enchantment lines: negated text width (NanoVG), so the widest line comes first. */
    private static final ToDoubleFunction<String> LINE_KEY = line -> {
        return -Fonts.TAHOMA_8.getStringWidth(line);
    };

    public EnchantInfo(ItemStack stack) {
        this.lines = new ArrayList<>();
        this.stack = stack;
        String remaining = Integer.toString(stack.getItem().getMaxDamage() - stack.getItemDamage());
        if (stack.getTagCompound() == null) {
            this.durabilityText = remaining;
            return;
        }
        NBTTagList enchants = stack.getTagCompound().getTagList("ench", 10);
        for (int i = 0; i < enchants.tagCount(); i++) {
            NBTTagCompound compound = enchants.getCompoundTagAt(i);     // out-of-range / non-compound tags give an empty compound
            int id = compound.getShort("id");
            int level = compound.getShort("lvl");
            if (id >= 0 && id < Enchantment.enchantmentsList.length) {
                Enchantment enchantment = Enchantment.enchantmentsList[id];
                if (enchantment != null) {
                    this.lines.add(format(enchantment, level));
                }
            }
        }
        this.durabilityText = stack.getTagCompound().getByte("Unbreakable") != 0 ? "Inf" : remaining;
        this.lines.sort(Comparator.comparingDouble(LINE_KEY));
    }

    /** True when the stack is damageable, damaged and not Unbreakable, i.e. when the durability text should be drawn. */
    public boolean isDamaged() {
        if (this.stack.getItem().getMaxDamage() < 1) {
            return false;
        }
        if (this.stack.getTagCompound() != null && this.stack.getTagCompound().getByte("Unbreakable") != 0) {
            return false;
        }
        if (this.stack.getItemDamage() < 1) {
            return false;
        }
        return this.stack.getItem().getMaxDamage() >= 1
                && (this.stack.getTagCompound() == null || this.stack.getTagCompound().getByte("Unbreakable") == 0);
    }

    /**
     * Short text of one enchantment: translated "name level" with colour codes stripped; one-level enchantments give the first four
     * letters, others the first three letters (Projectile Protection "Pro" -> "Prj"), a colour code, a space and the level.
     */
    private static String format(Enchantment enchantment, int level) {
        String name = StatCollector.translateToLocal(enchantment.getName());
        String levelText = StatCollector.translateToLocal("enchantment.level." + level);
        String stripped = StringUtils.stripControlCodes(name + " " + levelText);                 // regex replaceAll("")
        if (enchantment.getMaxLevel() == 1) {
            return stripped.substring(0, 4);
        }
        if (stripped.length() >= 4) {
            String prefix = stripped.substring(0, 3);
            if (enchantment == Enchantment.projectileProtection) {
                prefix = prefix.equalsIgnoreCase("Pro") ? "Prj" : "";
            }
            return prefix + ChatColor.GRAY + " " + level;
        }
        return stripped;
    }
}
