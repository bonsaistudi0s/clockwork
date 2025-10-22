package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.ScopeCrossbow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Handles scoping FOV mutation when using the scope crossbow
 */
@SuppressWarnings("all")
@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "isScoping", at = @At("HEAD"), cancellable = true)
    private void bonsai$scopeWithScopeCrossbow(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.isUsingItem()) return;

        ItemStack used = self.getUseItem();
        if (used.getItem() instanceof ScopeCrossbow && CrossbowItem.isCharged(used)) {
            cir.setReturnValue(true);
        }

    }

}