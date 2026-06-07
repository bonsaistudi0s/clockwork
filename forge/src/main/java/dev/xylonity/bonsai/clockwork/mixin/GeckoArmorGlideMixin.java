package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.gecko.GeckoArmorItem;
import dev.xylonity.bonsai.clockwork.common.item.wings.CustomGlider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GeckoArmorItem.class)
public abstract class GeckoArmorGlideMixin {

    @Unique
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        if ((Object) this instanceof CustomGlider glider) {
            return glider.canGlide(stack, entity);
        }

        return false;
    }

    @Unique
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if ((Object) this instanceof CustomGlider glider) {
            return glider.onGlideTick(stack, entity, flightTicks);
        }

        return false;
    }

}
