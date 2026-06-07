package dev.xylonity.bonsai.clockwork.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.xylonity.bonsai.clockwork.common.item.wings.CustomGlider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerGlideMixin {

    @WrapOperation(
            method = "tryToStartFallFlying",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private boolean clockwork$tryStartCheck(ItemStack stack, Item item, Operation<Boolean> original) {
        if (original.call(stack, item)) {
            return true;
        }

        return stack.getItem() instanceof CustomGlider;
    }

    @WrapOperation(
            method = "tryToStartFallFlying",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ElytraItem;isFlyEnabled(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private boolean clockwork$tryStartEnabled(ItemStack stack, Operation<Boolean> original) {
        if (stack.getItem() instanceof CustomGlider glider) {
            return glider.canGlide(stack, (LivingEntity) (Object) this);
        }

        return original.call(stack);
    }

}