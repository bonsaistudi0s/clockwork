package dev.xylonity.bonsai.clockwork.client.projectile.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.knightlib.common.entity.BaseProjectile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GenericProjectileModel extends GeoModel<BaseProjectile> {

    @Override
    public ResourceLocation getModelResource(BaseProjectile baseProjectile) {
        return new ResourceLocation(Clockwork.MOD_ID, "geo/generic.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BaseProjectile baseProjectile) {
        return new ResourceLocation(Clockwork.MOD_ID, "textures/entity/generic.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BaseProjectile baseProjectile) {
        return new ResourceLocation(Clockwork.MOD_ID, "animations/generic.animation.json");
    }

}
