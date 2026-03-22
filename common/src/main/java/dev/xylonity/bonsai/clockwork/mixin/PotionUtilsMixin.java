package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.sprayer.PotionSprayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Adjusts potion tooltips to show the reduced duration when a potion has been partially consumed by the sprayer
 */
@Mixin(PotionUtils.class)
public abstract class PotionUtilsMixin {

    @Inject(
            method = "addPotionTooltip(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;F)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void clockwork$adjustTooltip(ItemStack stack, List<Component> tooltips, float durationFactor, CallbackInfo ci) {
        if (!clockwork$isPotionItem(stack)) {
            return;
        }

        final CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(PotionSprayer.NBT_SPRAY_TICKS)) {
            return;
        }

        final List<MobEffectInstance> original = PotionUtils.getAllEffects(tag);
        if (original.isEmpty()) {
            return;
        }

        final List<MobEffectInstance> scaledEffects = PotionSprayer.scaleEffectsBySprayRemaining(stack, original);

        // Reinvokes the tooltip method with the scaled effects (the list overload, not ItemStack itself)
        PotionUtils.addPotionTooltip(scaledEffects, tooltips, durationFactor);

        ci.cancel();
    }

    @Unique
    private static boolean clockwork$isPotionItem(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }

}