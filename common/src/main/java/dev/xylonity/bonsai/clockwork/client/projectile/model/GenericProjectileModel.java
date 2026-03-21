package dev.xylonity.bonsai.clockwork.client.projectile.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.knightlib.common.entity.AbstractProjectile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GenericProjectileModel<T extends AbstractProjectile> extends GeoModel<T> {

    @Override
    public ResourceLocation getModelResource(AbstractProjectile baseProjectile) {
        return new ResourceLocation(Clockwork.MOD_ID, "geo/generic.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AbstractProjectile baseProjectile) {
        return new ResourceLocation(Clockwork.MOD_ID, "textures/entity/generic.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AbstractProjectile baseProjectile) {
        return new ResourceLocation(Clockwork.MOD_ID, "animations/generic.animation.json");
    }

}
