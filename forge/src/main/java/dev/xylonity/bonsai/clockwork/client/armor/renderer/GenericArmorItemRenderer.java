package dev.xylonity.bonsai.clockwork.client.armor.renderer;

import dev.xylonity.bonsai.clockwork.client.armor.model.GenericArmorItemModel;
import dev.xylonity.bonsai.clockwork.common.item.generic.GenericGeckoArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class GenericArmorItemRenderer extends GeoArmorRenderer<GenericGeckoArmorItem> {

    public GenericArmorItemRenderer(String resourceKey) {
        super(new GenericArmorItemModel(resourceKey));
    }

}