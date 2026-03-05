package dev.xylonity.bonsai.clockwork.client.armor.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.item.gecko.GeckoArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GenericArmorItemModel extends GeoModel<GeckoArmorItem> {

    private final String resourceKey;

    public GenericArmorItemModel(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    @Override
    public ResourceLocation getModelResource(GeckoArmorItem animatable) {
        return Clockwork.resource("geo/" + resourceKey + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeckoArmorItem animatable) {
        return Clockwork.resource("textures/armor/" + resourceKey + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(GeckoArmorItem animatable) {
        return Clockwork.resource("animations/" + resourceKey + ".animation.json");
    }

}