package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.knightlib.api.registrar.ResourceDispatcher;
import dev.xylonity.knightlib.api.registrar.ResourceEntry;
import dev.xylonity.knightlib.api.registrar.ResourceRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;

public class ClockworkItems {

    public static final ResourceRegistry<Item> ITEMS = ResourceDispatcher.create(BuiltInRegistries.ITEM, Clockwork.MOD_ID);

    public static final ResourceEntry<Item> CLOCKWORK_WINGS = ITEMS.register("clockwork_wings", Clockwork.PLATFORM.makeArmorItem(new Item.Properties().stacksTo(1), ArmorMaterials.LEATHER, ItemType.CLOCKWORK_WINGS, ArmorItem.Type.CHESTPLATE, "clockwork_wings"));

    public enum ItemType {
        CLOCKWORK_WINGS
    }

}
