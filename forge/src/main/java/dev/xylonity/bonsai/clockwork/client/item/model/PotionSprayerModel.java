package dev.xylonity.bonsai.clockwork.client.item.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.item.BarrelCrossbow;
import dev.xylonity.bonsai.clockwork.common.item.PotionSprayer;
import dev.xylonity.bonsai.clockwork.mixin.CrossbowItemAccessor;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import software.bernie.geckolib.model.GeoModel;

import java.util.List;

public class PotionSprayerModel extends GeoModel<PotionSprayer> {

    @Override
    public ResourceLocation getModelResource(PotionSprayer animatable) {
        return Clockwork.resource("geo/clockwork_potion_sprayer.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PotionSprayer animatable) {
        return Clockwork.resource("textures/item/clockwork_potion_sprayer.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PotionSprayer animatable) {
        return Clockwork.resource("animations/clockwork_potion_sprayer.animation.json");
    }

}