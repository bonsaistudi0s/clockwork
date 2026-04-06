package dev.xylonity.bonsai.clockwork.client.item.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.util.ClientUtil;
import dev.xylonity.bonsai.clockwork.common.item.crossbow.BarrelCrossbow;
import dev.xylonity.bonsai.clockwork.mixin.CrossbowItemAccessor;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import dev.xylonity.knightlib.api.util.KnightLibEasings;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

import java.util.List;

public class BarrelCrossbowModel extends GeoModel<BarrelCrossbow> {

    @Override
    public ResourceLocation getModelResource(BarrelCrossbow animatable) {
        return Clockwork.resource("geo/barrel_crossbow.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BarrelCrossbow animatable) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return Clockwork.resource("textures/item/clockwork_crossbow_arrow.png");
        }

        final ItemStack stack = ClientUtil.getCurrentStack();
        if (stack == null) {
            return Clockwork.resource("textures/item/clockwork_crossbow_arrow.png");
        }

        ItemStack projectileToLoad = null;
        if (CrossbowItem.isCharged(stack)) {
            List<ItemStack> projectiles = CrossbowItemAccessor.clockwork$getChargedProjectiles(stack);
            if (!projectiles.isEmpty()) {
                projectileToLoad = projectiles.get(0);
            }

        }
        else if (minecraft.player.isUsingItem() && minecraft.player.getUseItem().getItem() instanceof BarrelCrossbow) {
            projectileToLoad = minecraft.player.getProjectile(stack);
        }

        if (projectileToLoad != null && !projectileToLoad.isEmpty()) {
            if (projectileToLoad.is(ClockworkItems.CLOCKWORK_ARROW.get())) {
                return Clockwork.resource("textures/item/clockwork_crossbow_clockworkarrow.png");
            }
            else if (projectileToLoad.is(Items.FIREWORK_ROCKET)) {
                return Clockwork.resource("textures/item/clockwork_crossbow_firework.png");
            }
            else if (projectileToLoad.is(Items.ARROW)) {
                return Clockwork.resource("textures/item/clockwork_crossbow_arrow.png");
            }

        }

        return Clockwork.resource("textures/item/clockwork_crossbow_arrow.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BarrelCrossbow animatable) {
        return Clockwork.resource("animations/barrel_crossbow.animation.json");
    }

    @Override
    public void setCustomAnimations(BarrelCrossbow animatable, long instanceId, AnimationState<BarrelCrossbow> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        final ItemStack stack = animationState.getData(DataTickets.ITEMSTACK);
        if (stack == null || stack.isEmpty()) {
            return;
        }

        final CoreGeoBone barrel = this.getAnimationProcessor().getBone("barrel");
        if (barrel == null) return;

        final BarrelCrossbow.Phase phase = BarrelCrossbow.getPhase(stack);
        final int spins = BarrelCrossbow.getBarrelSpins(stack);
        final float partialTick = animationState.getPartialTick();

        float totalAngle;

        if (phase == BarrelCrossbow.Phase.LOADING) {
            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }

            final long now = minecraft.player.level().getGameTime();
            final long start = stack.getOrCreateTag().getLong(BarrelCrossbow.NBT_LOAD_START_TICK);
            final int duration = BarrelCrossbow.getChargeDurationTicks(stack);

            float progress = (now + partialTick - start) / (float) duration;
            progress = Mth.clamp(progress, 0f, 1f);
            progress = KnightLibEasings.EASE_IN_OUT_SINE.apply(progress);

            totalAngle = (spins + progress) * Mth.PI;
        }
        else if (phase == BarrelCrossbow.Phase.SHOOTING || phase == BarrelCrossbow.Phase.LOADED) {
            // Holds at the last completed rotation
            totalAngle = spins * Mth.PI;

        }
        else {
            // Idle
            return;
        }

        barrel.setRotZ(-totalAngle);
    }

}