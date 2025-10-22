package dev.xylonity.bonsai.clockwork.client.entity.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.passive.DragonflyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DragonflyModel extends GeoModel<DragonflyEntity> {

    @Override
    public ResourceLocation getModelResource(DragonflyEntity dragonflyEntity) {
        return Clockwork.resource("geo/dragonfly.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DragonflyEntity dragonflyEntity) {
        return Clockwork.resource("textures/entity/dragonfly.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DragonflyEntity dragonflyEntity) {
        return Clockwork.resource("animations/dragonfly.animation.json");
    }

}
