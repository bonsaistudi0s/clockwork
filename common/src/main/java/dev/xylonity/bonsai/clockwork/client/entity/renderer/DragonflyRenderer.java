package dev.xylonity.bonsai.clockwork.client.entity.renderer;

import dev.xylonity.bonsai.clockwork.client.entity.model.DragonflyModel;
import dev.xylonity.bonsai.clockwork.common.entity.custom.DragonflyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DragonflyRenderer extends GeoEntityRenderer<DragonflyEntity> {

    public DragonflyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DragonflyModel());
    }

}
