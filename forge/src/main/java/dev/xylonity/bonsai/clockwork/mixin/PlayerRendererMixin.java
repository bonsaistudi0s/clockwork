package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.BarrelCrossbow;
import dev.xylonity.bonsai.clockwork.common.item.PotionSprayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forces the CROSSBOW_HOLD animation when using certain items
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(
            method = "getArmPose(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void clockwork$poseMutator(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        if (player.getUsedItemHand() != hand || player.getUseItemRemainingTicks() <= 0) return;

        ItemStack inHand = player.getItemInHand(hand);
        if (!(inHand.getItem() instanceof PotionSprayer) && !(inHand.getItem() instanceof BarrelCrossbow)) return;

        cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
    }

}
