package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.crossbow.BarrelCrossbow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Setting the charge duration doesn't work correctly for this crossbow for some reason, so a mixin is done
 */
@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {

   @Inject(method = "getChargeDuration", at = @At("HEAD"), cancellable = true)
   private static void clockwork$barrelCustomCharge(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
       if (stack.getItem() instanceof BarrelCrossbow) {
           int index = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack);
           if (index < 0) {
               index = 0;
           }
           if (index > 3) {
               index = 3;
           }

           final int[] ticks = new int[] {
                   Math.round(0.25f * 20f),
                   Math.round(0.21f * 20f),
                   Math.round(0.18f * 20f),
                   Math.round(0.14f * 20f)
           };

           cir.setReturnValue(ticks[index]);
       }

   }

}