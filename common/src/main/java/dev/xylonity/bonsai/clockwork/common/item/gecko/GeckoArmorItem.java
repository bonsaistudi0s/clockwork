package dev.xylonity.bonsai.clockwork.common.item.gecko;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class GeckoArmorItem extends ArmorItem implements GeoItem {

    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public GeckoArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    protected abstract Object createGeckoRenderer();

    /**
     * Dead code, stub to satisfy the fabric compiler
     */
    public void createRenderer(Consumer<Object> consumer) {
        ;;
    }

    /**
     * Dead code, stub to satisfy the fabric compiler
     */
    public Supplier<Object> getRenderProvider() {
        return null;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

}