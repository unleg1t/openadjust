package com.adjustclient.modules.impl.player.inventory.item;
import com.adjustclient.modules.impl.player.inventory.item.type.*;
import com.adjustclient.modules.impl.player.inventory.item.filter.SimpleItemFilter;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Items;
public class ItemTypes {
    public static final List<ItemType> ALL = new ArrayList<>();
    static {
        ALL.add(new BlockItemType());
        ALL.add(new FoodItemType((Item) Items.golden_apple));
        ALL.add(new SwordItemType());
        ALL.add(new ToolItemType(ToolItemType.ToolKind.PICKAXE));
        ALL.add(new ToolItemType(ToolItemType.ToolKind.AXE));
        ALL.add(new ToolItemType(ToolItemType.ToolKind.SPADE));
        EnderPearlItemType pearls = new EnderPearlItemType();
        pearls.keepFilter = new SimpleItemFilter(32);
        ALL.add(pearls);
        BucketItemType bucket = new BucketItemType();
        bucket.keepFilter = new com.adjustclient.modules.impl.player.inventory.item.filter.BestItemFilter(bucket);
        ALL.add(bucket);
        ALL.add(new BowItemType());
        ALL.add(new ArmorItemType(0));
        ALL.add(new ArmorItemType(1));
        ALL.add(new ArmorItemType(2));
        ALL.add(new ArmorItemType(3));
        ALL.add(new FishingRodItemType());
        ArrowItemType arrows = new ArrowItemType();
        arrows.keepFilter = new SimpleItemFilter(64);
        ALL.add(arrows);
        ALL.add(new PotionItemType(((Potion) Potion.heal).id));
        ALL.add(new PotionItemType(((Potion) Potion.healthBoost).id));
        ALL.add(new PotionItemType(((Potion) Potion.absorption).id));
        ALL.add(new PotionItemType(((Potion) Potion.saturation).id));
        ALL.add(new PotionItemType(((Potion) Potion.resistance).id));
        PotionItemType limited = new PotionItemType(((Potion) Potion.fireResistance).id);
        limited.keepFilter = new SimpleItemFilter(2);
        ALL.add(limited);
        ALL.add(new PotionItemType(((Potion) Potion.regeneration).id));
        ALL.add(new PotionItemType(((Potion) Potion.jump).id));
        ALL.add(new PotionItemType(((Potion) Potion.moveSpeed).id));
        ALL.add(new PotionItemType(((Potion) Potion.damageBoost).id));
    }
}
