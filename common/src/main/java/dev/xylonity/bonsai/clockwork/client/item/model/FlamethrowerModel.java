package dev.xylonity.bonsai.clockwork.client.item.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.item.flamethrower.FlamethrowerItem;
import dev.xylonity.bonsai.clockwork.common.item.sprayer.PotionSprayer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FlamethrowerModel extends GeoModel<FlamethrowerItem> {

    @Override
    public ResourceLocation getModelResource(FlamethrowerItem animatable) {
        return Clockwork.resource("geo/clockwork_flamethrower.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FlamethrowerItem animatable) {
        return Clockwork.resource("textures/item/clockwork_flamethrower.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FlamethrowerItem animatable) {
        return Clockwork.resource("animations/clockwork_flamethrower.animation.json");
    }

}