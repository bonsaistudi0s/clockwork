package dev.xylonity.bonsai.clockwork.client.projectile.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.ClockworkArrowProjectile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ClockworkArrowProjectileModel extends GeoModel<ClockworkArrowProjectile> {

    @Override
    public ResourceLocation getModelResource(ClockworkArrowProjectile arrowProjectile) {
        return Clockwork.resource("geo/clockwork_arrow.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ClockworkArrowProjectile arrowProjectile) {
        return Clockwork.resource("textures/entity/clockwork_arrow.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ClockworkArrowProjectile arrowProjectile) {
        return Clockwork.resource("animations/clockwork_arrow.animation.json");
    }

}
