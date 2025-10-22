package dev.xylonity.bonsai.clockwork.platform;

import dev.xylonity.bonsai.clockwork.common.item.BarrelCrossbow;
import dev.xylonity.bonsai.clockwork.common.item.ClockworkWings;
import dev.xylonity.bonsai.clockwork.common.item.PotionSprayer;
import dev.xylonity.bonsai.clockwork.common.item.ScopeCrossbow;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class ClockworkPlatformForge implements ClockworkPlatform {

    @Override
    public <T extends Item> Supplier<T> makeItem(Item.Properties properties, ClockworkItems.ItemType itemType) {
        return switch (itemType) {
            case BARREL_CROSSBOW -> () -> (T) new BarrelCrossbow(properties.stacksTo(1).durability(396));
            case SCOPE_CROSSBOW -> () -> (T) new ScopeCrossbow(properties.stacksTo(1).durability(396));
            case POTION_SPRAYER -> () -> (T) new PotionSprayer(properties.stacksTo(1).durability(396));
            default -> () -> (T) new Item(properties);
        };
    }

    public <T extends Item> Supplier<T> makeArmorItem(Item.Properties properties, ArmorMaterials armorMaterial, ClockworkItems.ItemType itemType, ArmorItem.Type type, String id) {
        return switch (itemType) {
            default -> // Clockwork Wings
                    () -> (T) new ClockworkWings(properties, armorMaterial, type, id);
        };
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder();
    }

}