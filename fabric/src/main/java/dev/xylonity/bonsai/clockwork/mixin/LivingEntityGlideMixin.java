package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.wings.CustomGlider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityGlideMixin {

    @Shadow
    protected int fallFlyTicks;

    @Redirect(
            method = "updateFallFlying",
            at = @At(
                    value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private boolean clockwork$customGliderCheck(ItemStack stack, Item item) {
        if (stack.is(item)) {
            return true;
        }

        return stack.getItem() instanceof CustomGlider;
    }

    @Redirect(
            method = "updateFallFlying",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ElytraItem;isFlyEnabled(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private boolean clockwork$customGliderEnabled(ItemStack stack) {
        if (stack.getItem() instanceof CustomGlider glider) {
            final LivingEntity self = (LivingEntity) (Object) this;
            final boolean canGlide = glider.canGlide(stack, self);
            if (canGlide) {
                glider.onGlideTick(stack, self, fallFlyTicks);
            }

            return canGlide;
        }

        return ElytraItem.isFlyEnabled(stack);
    }

}