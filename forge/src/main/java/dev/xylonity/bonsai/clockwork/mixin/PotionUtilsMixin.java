package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.PotionSprayer;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Adjusts the potion duration within a generic tag, keeping the original timer value
 */
@Mixin(PotionUtils.class)
public abstract class PotionUtilsMixin {

    @Inject(
            method = "addPotionTooltip(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;F)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void clockwork$adjustTooltip(ItemStack stack, List<Component> tooltips, float durationFactor, CallbackInfo ci) {
        if (!(stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION))) return;

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(PotionSprayer.NBT_SPRAY_TICKS)) return;

        List<MobEffectInstance> original = PotionUtils.getAllEffects(stack.getTag());
        if (original.isEmpty()) return;

        // Base duration value
        int baseMax = 0;
        for (MobEffectInstance effect : original) {
            baseMax = Math.max(baseMax, effect.getDuration());
        }

        if (baseMax <= 0) return;

        // Scaling every single effect individually
        List<MobEffectInstance> adjustedEffects = new ArrayList<>(original.size());
        for (MobEffectInstance effect : original) {
            adjustedEffects.add(new MobEffectInstance(
                    effect.getEffect(),
                    Math.max(0, (int) Math.floor(effect.getDuration() * Math.min(1.0, (double) Math.max(0, tag.getInt(PotionSprayer.NBT_SPRAY_TICKS)) / (double) baseMax))),
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.isVisible(),
                    effect.showIcon()
            ));

        }

        PotionUtils.addPotionTooltip(adjustedEffects, tooltips, durationFactor);
        ci.cancel();
    }

}
