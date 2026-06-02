package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.wings.CustomGlider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public class LocalPlayerGlideMixin {

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private boolean clockwork$clientGlideCheck(ItemStack stack, Item item) {
        if (stack.is(item)) {
            return true;
        }

        return stack.getItem() instanceof CustomGlider;
    }

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ElytraItem;isFlyEnabled(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private boolean clockwork$clientGlideEnabled(ItemStack stack) {
        if (stack.getItem() instanceof CustomGlider glider) {
            return glider.canGlide(stack, (LocalPlayer) (Object) this);
        }

        return ElytraItem.isFlyEnabled(stack);
    }

}