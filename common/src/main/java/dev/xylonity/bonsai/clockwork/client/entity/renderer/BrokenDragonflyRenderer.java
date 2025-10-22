package dev.xylonity.bonsai.clockwork.client.entity.renderer;

import dev.xylonity.bonsai.clockwork.client.entity.model.BrokenDragonflyModel;
import dev.xylonity.bonsai.clockwork.common.entity.passive.BrokenDragonflyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BrokenDragonflyRenderer extends GeoEntityRenderer<BrokenDragonflyEntity> {

    public BrokenDragonflyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BrokenDragonflyModel());
    }

    @Override
    protected float getDeathMaxRotation(BrokenDragonflyEntity animatable) {
        return 0;
    }

}
