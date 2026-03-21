package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.wings.CustomGlider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerGlideMixin {

    @Redirect(
            method = "tryToStartFallFlying",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private boolean clockwork$tryStartCheck(ItemStack stack, Item item) {
        if (stack.is(item)) {
            return true;
        }

        return stack.getItem() instanceof CustomGlider;
    }

    @Redirect(
            method = "tryToStartFallFlying",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ElytraItem;isFlyEnabled(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private boolean clockwork$tryStartEnabled(ItemStack stack) {
        if (stack.getItem() instanceof CustomGlider glider) {
            return glider.canGlide(stack, (LivingEntity) (Object) this);
        }

        return ElytraItem.isFlyEnabled(stack);
    }

}