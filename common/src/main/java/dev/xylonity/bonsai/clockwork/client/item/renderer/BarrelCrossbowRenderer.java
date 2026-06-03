package dev.xylonity.bonsai.clockwork.client.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.item.model.BarrelCrossbowModel;
import dev.xylonity.bonsai.clockwork.client.util.ClientSwayUtil;
import dev.xylonity.bonsai.clockwork.client.util.ClientUtil;
import dev.xylonity.bonsai.clockwork.common.item.crossbow.BarrelCrossbow;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BarrelCrossbowRenderer extends GeoItemRenderer<BarrelCrossbow> {

    private static final String MODEL_STANDBY = "barrel_crossbow_standby";
    private static final String MODEL_ARROW = "barrel_crossbow_arrow";
    private static final String MODEL_FIREWORK = "barrel_crossbow_firework";
    private static final String MODEL_CLOCKWORK_ARROW  = "barrel_crossbow_clockworkarrow";
    private static final String MODEL_PULLING_0 = "barrel_crossbow_pulling_0";
    private static final String MODEL_PULLING_1 = "barrel_crossbow_pulling_1";
    private static final String MODEL_PULLING_2 = "barrel_crossbow_pulling_2";

    private static final float PULL_THRESHOLD_STAGE_1 = 0.25f;
    private static final float PULL_THRESHOLD_STAGE_2 = 0.45f;
    private static final float PULL_THRESHOLD_LOADED = 0.55f;

    private ItemDisplayContext lastTransform = ItemDisplayContext.NONE;
    private final ClientSwayUtil swayUtil;

    public BarrelCrossbowRenderer() {
        super(new BarrelCrossbowModel());
        this.swayUtil = new ClientSwayUtil();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buf, int light, int overlay) {
        lastTransform = context;

        if (ClientUtil.isStaticContext(context)) {
            renderStaticModel(stack, context, pose, buf, light, overlay);
            return;
        }

        super.renderByItem(stack, context, pose, buf, light, overlay);
    }

    @Override
    public void preRender(PoseStack poseStack, BarrelCrossbow animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        swayUtil.applyFirstPersonSway(poseStack, lastTransform, partialTick);
    }

    private void renderStaticModel(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buf, int light, int overlay) {
        final Minecraft minecraft = Minecraft.getInstance();

        pose.pushPose();
        ClientUtil.applyStaticTransform(ctx, pose);

        final BakedModel model = getStaticModel(stack, minecraft);
        minecraft.getItemRenderer().render(stack, ctx, false, pose, buf, light, overlay, model);

        pose.popPose();
    }

    private BakedModel getStaticModel(ItemStack stack, Minecraft minecraft) {
        final String modelName = resolveModelName(stack, minecraft.player);
        BakedModel bakedModel = ClientUtil.extraItemModel(modelName);

        if (bakedModel == minecraft.getModelManager().getMissingModel()) {
            bakedModel = ClientUtil.extraItemModel(MODEL_STANDBY);
        }

        return bakedModel;
    }

    private String resolveModelName(ItemStack stack, LocalPlayer player) {
        if (CrossbowItem.isCharged(stack)) {
            return modelForLoadedProjectile(stack);
        }
        if (ClientUtil.isActivelyPulling(stack, player)) {
            return modelForPullProgress(stack, player);
        }

        return MODEL_STANDBY;
    }

    private String modelForLoadedProjectile(ItemStack stack) {
        final ChargedProjectiles charged = stack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        if (charged.contains(Items.FIREWORK_ROCKET)) {
            return MODEL_FIREWORK;
        }
        if (charged.contains(ClockworkItems.CLOCKWORK_ARROW.get())) {
            return MODEL_CLOCKWORK_ARROW;
        }

        return MODEL_ARROW;
    }

    private String modelForPullProgress(ItemStack stack, LocalPlayer player) {
        final float progress = BarrelCrossbow.getLoadProgress(stack, player.level().getGameTime());

        if (progress >= PULL_THRESHOLD_LOADED) {
            return modelForProjectilePreview(stack, player);
        }
        if (progress >= PULL_THRESHOLD_STAGE_2) {
            return MODEL_PULLING_2;
        }
        if (progress >= PULL_THRESHOLD_STAGE_1) {
            return MODEL_PULLING_1;
        }

        return MODEL_PULLING_0;
    }

    private String modelForProjectilePreview(ItemStack stack, LocalPlayer player) {
        final ItemStack projectile = player.getProjectile(stack);

        if (!projectile.isEmpty()) {
            if (projectile.is(ClockworkItems.CLOCKWORK_ARROW.get())) {
                return MODEL_CLOCKWORK_ARROW;
            }
            if (projectile.is(Items.FIREWORK_ROCKET)) {
                return MODEL_FIREWORK;
            }

        }

        return MODEL_ARROW;
    }

}