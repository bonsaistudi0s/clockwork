package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.wings.CustomGlider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityGlideMixin {

    @Redirect(method = "updateFallFlying",
              at = @At(
                      value = "INVOKE",
                      target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z",
                      remap = false
              ))
    private boolean clockwork$canGlide(ItemStack stack, LivingEntity entity) {
        if (stack.getItem() instanceof CustomGlider glider) {
            return glider.canGlide(stack, entity);
        }

        return stack.canElytraFly(entity);
    }

    @Redirect(method = "updateFallFlying",
              at = @At(
                      value = "INVOKE",
                      target = "Lnet/minecraft/world/item/ItemStack;elytraFlightTick(Lnet/minecraft/world/entity/LivingEntity;I)Z",
                      remap = false
              ))
    private boolean clockwork$glideTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (stack.getItem() instanceof CustomGlider glider) {
            return glider.onGlideTick(stack, entity, flightTicks);
        }

        return stack.elytraFlightTick(entity, flightTicks);
    }

}