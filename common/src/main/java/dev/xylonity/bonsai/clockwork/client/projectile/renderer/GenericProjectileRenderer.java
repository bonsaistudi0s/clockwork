package dev.xylonity.bonsai.clockwork.client.projectile.renderer;

import dev.xylonity.bonsai.clockwork.client.projectile.model.GenericProjectileModel;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.GenericProjectile;
import dev.xylonity.knightlib.common.entity.AbstractProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GenericProjectileRenderer<T extends GenericProjectile> extends GeoEntityRenderer<T> {

    public GenericProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GenericProjectileModel<>());
    }

}
