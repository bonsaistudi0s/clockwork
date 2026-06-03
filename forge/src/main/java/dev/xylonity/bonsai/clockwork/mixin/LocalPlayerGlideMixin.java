package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.wings.CustomGlider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public class LocalPlayerGlideMixin {

    @Redirect(method = "aiStep",
              at = @At(
                      value = "INVOKE",
                      target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z",
                      remap = false
              ))
    private boolean clockwork$clientGlideCheck(ItemStack stack, LivingEntity entity) {
        if (stack.getItem() instanceof CustomGlider glider) {
            return glider.canGlide(stack, entity);
        }

        return stack.canElytraFly(entity);
    }

}