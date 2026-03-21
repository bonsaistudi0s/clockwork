package dev.xylonity.bonsai.clockwork.client.projectile.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.ClockworkArrowProjectile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ClockworkArrowProjectileModel extends GeoModel<ClockworkArrowProjectile> {

    @Override
    public ResourceLocation getModelResource(ClockworkArrowProjectile arrowProjectile) {
        return new ResourceLocation(Clockwork.MOD_ID, "geo/clockwork_arrow.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ClockworkArrowProjectile arrowProjectile) {
        return new ResourceLocation(Clockwork.MOD_ID, "textures/entity/clockwork_arrow.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ClockworkArrowProjectile arrowProjectile) {
        return new ResourceLocation(Clockwork.MOD_ID, "animations/clockwork_arrow.animation.json");
    }

}
