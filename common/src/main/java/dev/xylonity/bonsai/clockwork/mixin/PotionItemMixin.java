package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.sprayer.PotionSprayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * When a partially-sprayed potion is drunk, scales all effect durations by the remaining spray ratio so the player gets proportionally less benefit
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
        final List<MobEffectInstance> original = PotionUtils.getMobEffects(stack);
        return PotionSprayer.scaleEffectsBySprayRemaining(stack, original);
    }

}