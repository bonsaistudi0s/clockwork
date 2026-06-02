package dev.xylonity.bonsai.clockwork.common.item.gecko;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.function.Consumer;

public abstract class GeckoArmorItem extends ArmorItem implements GeoItem {

    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public GeckoArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    protected abstract Object createGeckoRenderer();

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @Override
            @SuppressWarnings("unchecked")
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(T livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<T> original) {
                if (this.renderer == null) {
                    this.renderer = (GeoArmorRenderer<GeckoArmorItem>) createGeckoRenderer();
                }

                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);

                return this.renderer;
            }

        });
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

}