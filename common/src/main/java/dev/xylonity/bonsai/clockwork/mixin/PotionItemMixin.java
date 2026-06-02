package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.sprayer.PotionSprayer;
import dev.xylonity.bonsai.clockwork.common.util.StackNbt;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * When a partially-sprayed potion is drunk, scales all effect durations by the remaining spray ratio so the player gets proportionally less benefit
 */
@Mixin(PotionItem.class)
public abstract class PotionItemMixin {

    @Redirect(
            method = "finishUsingItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/alchemy/PotionContents;forEachEffect(Ljava/util/function/Consumer;)V"
            )
    )
    private void clockwork$scaleDrinkEffects(PotionContents contents, Consumer<MobEffectInstance> consumer, ItemStack stack) {
        if (!StackNbt.contains(stack, PotionSprayer.NBT_SPRAY_TICKS)) {
            contents.forEachEffect(consumer);
            return;
        }

        final List<MobEffectInstance> original = new ArrayList<>();
        contents.getAllEffects().forEach(original::add);

        for (MobEffectInstance scaled : PotionSprayer.scaleEffectsBySprayRemaining(stack, original)) {
            consumer.accept(scaled);
        }

    }

    @Redirect(
            method = "appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/alchemy/PotionContents;addPotionTooltip(Ljava/util/function/Consumer;FF)V"
            )
    )
    private void clockwork$scaleTooltip(PotionContents contents, Consumer<Component> adder, float durationFactor, float ticksPerSecond,
                                        ItemStack stack, Item.TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
        if (!StackNbt.contains(stack, PotionSprayer.NBT_SPRAY_TICKS)) {
            contents.addPotionTooltip(adder, durationFactor, ticksPerSecond);
            return;
        }

        final List<MobEffectInstance> original = new ArrayList<>();
        contents.getAllEffects().forEach(original::add);

        PotionContents.addPotionTooltip(PotionSprayer.scaleEffectsBySprayRemaining(stack, original), adder, durationFactor, ticksPerSecond);
    }

}