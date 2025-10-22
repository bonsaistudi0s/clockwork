package dev.xylonity.bonsai.clockwork.client.entity.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.passive.BrokenDragonflyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BrokenDragonflyModel extends GeoModel<BrokenDragonflyEntity> {

    @Override
    public ResourceLocation getModelResource(BrokenDragonflyEntity dragonflyEntity) {
        if (dragonflyEntity.getActivatedTimer() <= BrokenDragonflyEntity.ANIMATION_ACTIVATE_TICKS) {
            return Clockwork.resource("geo/dragonfly.geo.json");
        }

        return Clockwork.resource("geo/broken_dragonfly.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BrokenDragonflyEntity dragonflyEntity) {
        if (dragonflyEntity.getActivatedTimer() <= BrokenDragonflyEntity.ANIMATION_ACTIVATE_TICKS) {
            return Clockwork.resource("textures/entity/dragonfly.png");
        }

        return Clockwork.resource("textures/entity/broken_dragonfly.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BrokenDragonflyEntity dragonflyEntity) {
        return Clockwork.resource("animations/broken_dragonfly.animation.json");
    }

}
