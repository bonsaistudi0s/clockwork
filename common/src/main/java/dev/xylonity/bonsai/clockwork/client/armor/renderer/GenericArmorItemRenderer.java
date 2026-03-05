package dev.xylonity.bonsai.clockwork.client.armor.renderer;

import dev.xylonity.bonsai.clockwork.client.armor.model.GenericArmorItemModel;
import dev.xylonity.bonsai.clockwork.common.item.gecko.GeckoArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class GenericArmorItemRenderer extends GeoArmorRenderer<GeckoArmorItem> {

    public GenericArmorItemRenderer(String resourceKey) {
        super(new GenericArmorItemModel(resourceKey));
    }

}