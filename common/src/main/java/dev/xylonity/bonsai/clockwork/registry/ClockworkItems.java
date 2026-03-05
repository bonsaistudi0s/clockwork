package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.item.crossbow.BarrelCrossbow;
import dev.xylonity.bonsai.clockwork.common.item.crossbow.ScopeCrossbow;
import dev.xylonity.bonsai.clockwork.common.item.crossbow.arrow.ClockworkArrow;
import dev.xylonity.bonsai.clockwork.common.item.sprayer.PotionSprayer;
import dev.xylonity.bonsai.clockwork.common.item.wings.ClockworkWings;
import dev.xylonity.knightlib.api.registrar.ResourceDispatcher;
import dev.xylonity.knightlib.api.registrar.ResourceEntry;
import dev.xylonity.knightlib.api.registrar.ResourceRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;

public class ClockworkItems {

    public static final ResourceRegistry<Item> ITEMS = ResourceDispatcher.create(BuiltInRegistries.ITEM, Clockwork.MOD_ID);

    public static final ResourceEntry<Item> CLOCKWORK_GEAR = ITEMS.register("clockwork_gear", () -> new Item(new Item.Properties()));
    public static final ResourceEntry<Item> CROSSBOW_BARREL = ITEMS.register("crossbow_barrel", () -> new Item(new Item.Properties()));
    public static final ResourceEntry<Item> CLOCKWORK_ARROW = ITEMS.register("clockwork_arrow", () -> new ClockworkArrow(new Item.Properties()));

    public static final ResourceEntry<Item> BARREL_CROSSBOW = ITEMS.register("barrel_crossbow", () -> new BarrelCrossbow(new Item.Properties()));
    public static final ResourceEntry<Item> SCOPE_CROSSBOW = ITEMS.register("scope_crossbow", () -> new ScopeCrossbow(new Item.Properties()));
    public static final ResourceEntry<Item> CLOCKWORK_WINGS = ITEMS.register("clockwork_wings", () -> new ClockworkWings(new Item.Properties().stacksTo(1), ArmorMaterials.LEATHER, ArmorItem.Type.CHESTPLATE));
    public static final ResourceEntry<Item> CLOCKWORK_POTION_SPRAYER = ITEMS.register("clockwork_potion_sprayer", () -> new PotionSprayer(new Item.Properties()));

}
