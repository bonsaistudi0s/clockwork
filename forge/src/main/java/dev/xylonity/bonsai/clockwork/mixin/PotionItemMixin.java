package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.PotionSprayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

/**
 * Adjusts the duration of the potion based on the generic spray ticks reduction tag
 */
@Mixin(PotionItem.class)
public abstract class PotionItemMixin {

    @Redirect(
            method = "finishUsingItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/alchemy/PotionUtils;getMobEffects(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"
            )
    )
    private List<MobEffectInstance> clockwork$applyRemainingDuration(ItemStack stack) {
        List<MobEffectInstance> originalEffects = PotionUtils.getMobEffects(stack);

        // If there is no timer, forces vanilla container
        if (!stack.hasTag() || !stack.getTag().contains(PotionSprayer.NBT_SPRAY_TICKS)) {
            return originalEffects;
        }

        // If there is not time remaining, doesn't apply effects
        int leftTicks = Math.max(0, stack.getTag().getInt(PotionSprayer.NBT_SPRAY_TICKS));
        if (leftTicks <= 0) {
            return List.of();
        }

        // Takes into account the duration reduction from the potion sprayer
        int baseMax = 0;
        for (MobEffectInstance effect : originalEffects) {
            baseMax = Math.max(baseMax, effect.getDuration());
        }

        if (baseMax <= 0) return originalEffects;

        List<MobEffectInstance> adjustedEffects = new ArrayList<>(originalEffects.size());
        for (MobEffectInstance effect : originalEffects) {
            int newDuration = (int) Math.floor(effect.getDuration() * Math.min(1.0, (double) leftTicks / (double) baseMax));

            // Avoiding duration-zero effects (that may lead to client-sided problems)
            if (newDuration <= 0) {
                continue;
            }

            adjustedEffects.add(new MobEffectInstance(
                    effect.getEffect(),
                    newDuration,
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.isVisible(),
                    effect.showIcon()
            ));
        }

        return adjustedEffects;
    }

}
