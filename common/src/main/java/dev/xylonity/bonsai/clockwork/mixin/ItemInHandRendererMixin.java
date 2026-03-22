package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.crossbow.BarrelCrossbow;
import dev.xylonity.bonsai.clockwork.common.item.flamethrower.FlamethrowerItem;
import dev.xylonity.bonsai.clockwork.common.item.sprayer.PotionSprayer;
import dev.xylonity.bonsai.clockwork.common.item.crossbow.ScopeCrossbow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mutates vanilla animations and resets the hand position on certain cases, like when shooting with the barrel crossbow, to avoid the vertical sway
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Shadow
    private float mainHandHeight;

    @Shadow
    private float oMainHandHeight;

    @Shadow
    private float offHandHeight;

    @Shadow
    private float oOffHandHeight;

    @Shadow
    private ItemStack mainHandItem;

    @Shadow
    private ItemStack offHandItem;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Redirect(
            method = "renderArmWithItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/CrossbowItem;isCharged(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private boolean clockwork$noChargedPose(ItemStack stack) {
        if (clockwork$isCorrectItem(stack)) {
            return false;
        }

        return CrossbowItem.isCharged(stack);
    }

    @Inject(
            method = "itemUsed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void clockwork$dontDropHandWhenUsed(InteractionHand hand, CallbackInfo ci) {
        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        final ItemStack stack = (hand == InteractionHand.MAIN_HAND) ? player.getMainHandItem() : player.getOffhandItem();
        if (clockwork$isCorrectItem(stack)) {
            ci.cancel();
        }

    }

    @Redirect(
            method = "renderArmWithItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z")
    )
    private boolean clockwork$fakeNotUsingForScope(AbstractClientPlayer instance) {
        if (instance.isUsingItem()) {
            final ItemStack used = instance.getUseItem();
            if (clockwork$isCorrectItem(used)) {
                return false;
            }

        }

        return instance.isUsingItem();
    }

    @Redirect(
            method = "renderArmWithItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUseItemRemainingTicks()I")
    )
    private int clockwork$zeroUseTicksForScope(AbstractClientPlayer instance) {
        if (instance.isUsingItem()) {
            final ItemStack used = instance.getUseItem();
            if (clockwork$isCorrectItem(used)) {
                return 0;
            }

        }

        return instance.getUseItemRemainingTicks();
    }

    /**
     * Replicates vanilla's logic
     */
    @Inject(
            method = "tick()V",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void clockwork$cancelVerticalSwayAnimation(CallbackInfo ci) {
        final LocalPlayer localPlayer = this.minecraft.player;
        if (localPlayer != null) {
            final ItemStack itemStack = localPlayer.getMainHandItem();
            final ItemStack itemStack2 = localPlayer.getOffhandItem();

            final boolean mainIsSpecial = clockwork$isCorrectItem(itemStack) && !(itemStack.getItem() instanceof ScopeCrossbow);
            final boolean offIsSpecial = clockwork$isCorrectItem(itemStack2) && !(itemStack2.getItem() instanceof ScopeCrossbow);

            if (mainIsSpecial || offIsSpecial) {
                this.oMainHandHeight = this.mainHandHeight;
                this.oOffHandHeight = this.offHandHeight;
                if (ItemStack.matches(this.mainHandItem, itemStack)) {
                    this.mainHandItem = itemStack;
                }

                if (ItemStack.matches(this.offHandItem, itemStack2)) {
                    this.offHandItem = itemStack2;
                }

                if (localPlayer.isHandsBusy()) {
                    this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4F, 0.0F, 1.0F);
                    this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4F, 0.0F, 1.0F);
                }
                else {
                    final float attackStrengthScale = localPlayer.getAttackStrengthScale(1f);
                    if (this.mainHandItem != itemStack) {
                        this.mainHandItem = itemStack;
                    }
                    if (this.offHandItem != itemStack2) {
                        this.offHandItem = itemStack2;
                    }

                    this.mainHandHeight += Mth.clamp((attackStrengthScale * attackStrengthScale * attackStrengthScale) - this.mainHandHeight, -0.4F, 0.4F);
                    this.offHandHeight += Mth.clamp(1 - this.offHandHeight, -0.4F, 0.4F);
                }

                if (this.mainHandHeight < 0.1F) {
                    this.mainHandItem = itemStack;
                }

                if (this.offHandHeight < 0.1F) {
                    this.offHandItem = itemStack2;
                }

                ci.cancel();
            }

        }

    }

    @Unique
    public boolean clockwork$isCorrectItem(final ItemStack stack) {
        return stack.getItem() instanceof ScopeCrossbow || stack.getItem() instanceof BarrelCrossbow ||
                stack.getItem() instanceof PotionSprayer || stack.getItem() instanceof FlamethrowerItem;
    }

}