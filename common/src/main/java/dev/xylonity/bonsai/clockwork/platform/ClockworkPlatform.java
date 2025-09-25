package dev.xylonity.bonsai.clockwork.platform;

import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public interface ClockworkPlatform {
    <T extends Item> Supplier<T> makeItem(Item.Properties properties, ClockworkItems.ItemType itemType);
    <T extends Item> Supplier<T> makeArmorItem(Item.Properties properties, ArmorMaterials armorMaterial, ClockworkItems.ItemType armorType, ArmorItem.Type type, String id);
}