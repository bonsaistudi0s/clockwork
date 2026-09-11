package dev.xylonity.bonsai.clockwork.client.projectile.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.GenericProjectile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GenericProjectileModel<T extends GenericProjectile> extends GeoModel<T> {

    @Override
    public ResourceLocation getModelResource(GenericProjectile baseProjectile) {
        return Clockwork.resource("geo/generic.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GenericProjectile baseProjectile) {
        return Clockwork.resource("textures/entity/generic.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GenericProjectile baseProjectile) {
        return Clockwork.resource("animations/generic.animation.json");
    }

}
