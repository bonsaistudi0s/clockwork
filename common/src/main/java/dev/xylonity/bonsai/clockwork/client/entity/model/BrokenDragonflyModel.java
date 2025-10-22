package dev.xylonity.bonsai.clockwork.client.entity.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.passive.BrokenDragonflyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BrokenDragonflyModel extends GeoModel<BrokenDragonflyEntity> {

    @Override
    public ResourceLocation getModelResource(BrokenDragonflyEntity dragonflyEntity) {
        return new ResourceLocation(Clockwork.MOD_ID, "geo/broken_dragonfly.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BrokenDragonflyEntity dragonflyEntity) {
        if (dragonflyEntity.getActivatedTimer() <= BrokenDragonflyEntity.ANIMATION_ACTIVATE_TICKS) {
            return new ResourceLocation(Clockwork.MOD_ID, "textures/entity/dragonfly.png");
        }

        return new ResourceLocation(Clockwork.MOD_ID, "textures/entity/broken_dragonfly.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BrokenDragonflyEntity dragonflyEntity) {
        return new ResourceLocation(Clockwork.MOD_ID, "animations/broken_dragonfly.animation.json");
    }

}
