package dev.xylonity.bonsai.clockwork.client.item.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.item.sprayer.PotionSprayer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

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