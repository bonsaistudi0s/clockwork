package dev.xylonity.bonsai.clockwork.platform;

import dev.xylonity.bonsai.clockwork.common.item.ClockworkWings;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class ClockworkPlatformForge implements ClockworkPlatform {

    @Override
    public <T extends Item> Supplier<T> makeItem(Item.Properties properties, ClockworkItems.ItemType itemType) {
        return switch (itemType) {
            default -> () -> (T) new Item(properties);
        };
    }

    public <T extends Item> Supplier<T> makeArmorItem(Item.Properties properties, ArmorMaterials armorMaterial, ClockworkItems.ItemType itemType, ArmorItem.Type type, String id) {
        return switch (itemType) {
            case CLOCKWORK_WINGS -> () -> (T) new ClockworkWings(properties, armorMaterial, type, id);
        };
    }

}