package dev.xylonity.bonsai.clockwork.client.item.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.util.ClientUtil;
import dev.xylonity.bonsai.clockwork.common.item.crossbow.BarrelCrossbow;
import dev.xylonity.bonsai.clockwork.mixin.CrossbowItemAccessor;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

}