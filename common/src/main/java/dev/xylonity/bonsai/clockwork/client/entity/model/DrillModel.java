package dev.xylonity.bonsai.clockwork.client.entity.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.tool.ClockworkDrillEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DrillModel extends GeoModel<ClockworkDrillEntity> {

    @Override
    public ResourceLocation getModelResource(ClockworkDrillEntity entity) {
        return new ResourceLocation(Clockwork.MOD_ID, "geo/clockwork_drill.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ClockworkDrillEntity entity) {
        return new ResourceLocation(Clockwork.MOD_ID, "textures/entity/clockwork_drill.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ClockworkDrillEntity entity) {
        return new ResourceLocation(Clockwork.MOD_ID, "animations/clockwork_drill.animation.json");
    }
}