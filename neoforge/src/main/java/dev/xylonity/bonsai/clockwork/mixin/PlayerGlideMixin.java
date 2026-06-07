package dev.xylonity.bonsai.clockwork.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.xylonity.bonsai.clockwork.common.item.wings.CustomGlider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerGlideMixin {

    @WrapOperation(method = "tryToStartFallFlying",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z"
            ),
            remap = false)
    private boolean clockwork$tryStartGlide(ItemStack stack, LivingEntity entity, Operation<Boolean> original) {
        if (stack.getItem() instanceof CustomGlider glider) {
            return glider.canGlide(stack, entity);
        }

        return original.call(stack, entity);
    }

}