package dev.xylonity.bonsai.clockwork.client.armor.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.item.generic.GenericGeckoArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GenericArmorItemModel extends GeoModel<GenericGeckoArmorItem> {

    private final String resourceKey;

    public GenericArmorItemModel(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    @Override
    public ResourceLocation getModelResource(GenericGeckoArmorItem animatable) {
        return new ResourceLocation(Clockwork.MOD_ID, "geo/" + resourceKey + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GenericGeckoArmorItem animatable) {
        return new ResourceLocation(Clockwork.MOD_ID, "textures/armor/" + resourceKey + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(GenericGeckoArmorItem animatable) {
        return new ResourceLocation(Clockwork.MOD_ID, "animations/" + resourceKey + ".animation.json");
    }

}