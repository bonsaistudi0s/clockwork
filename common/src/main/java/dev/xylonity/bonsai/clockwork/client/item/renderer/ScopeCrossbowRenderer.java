package dev.xylonity.bonsai.clockwork.client.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.item.model.ScopeCrossbowModel;
import dev.xylonity.bonsai.clockwork.client.util.ClientSwayUtil;
import dev.xylonity.bonsai.clockwork.client.util.ClientUtil;
import dev.xylonity.bonsai.clockwork.common.item.crossbow.ScopeCrossbow;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ScopeCrossbowRenderer extends GeoItemRenderer<ScopeCrossbow> {

    private static final String MODEL_STANDBY = "scope_crossbow_standby";
    private static final String MODEL_ARROW = "scope_crossbow_arrow";
    private static final String MODEL_FIREWORK = "scope_crossbow_firework";
    private static final String MODEL_CLOCKWORK_ARROW = "scope_crossbow_clockworkarrow";
    private static final String MODEL_PULLING_0 = "scope_crossbow_pulling_0";
    private static final String MODEL_PULLING_1 = "scope_crossbow_pulling_1";
    private static final String MODEL_PULLING_2 = "scope_crossbow_pulling_2";

    private static final float PULL_THRESHOLD_MID = 0.58f;
    private static final float PULL_THRESHOLD_FULL = 1.0f;

    private ItemDisplayContext lastTransform = ItemDisplayContext.NONE;
    private final ClientSwayUtil swayUtil;

    public ScopeCrossbowRenderer() {
        super(new ScopeCrossbowModel());
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
    public void preRender(PoseStack poseStack, ScopeCrossbow animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        swayUtil.applyFirstPersonSway(poseStack, lastTransform, partialTick);
    }

    private void renderStaticModel(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buf, int light, int overlay) {
        final Minecraft minecraft = Minecraft.getInstance();

        pose.pushPose();
        ClientUtil.applyStaticTransform(context, pose);

        final BakedModel bakedModel = resolveStaticModel(stack, minecraft);
        minecraft.getItemRenderer().render(stack, context, false, pose, buf, light, overlay, bakedModel);

        pose.popPose();
    }

    private BakedModel resolveStaticModel(ItemStack stack, Minecraft minecraft) {
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
        final float progress = getPullProgress(stack, player);
        if (progress >= PULL_THRESHOLD_FULL) {
            return MODEL_PULLING_2;
        }
        if (progress >= PULL_THRESHOLD_MID) {
            return MODEL_PULLING_1;
        }

        return MODEL_PULLING_0;
    }

    private float getPullProgress(ItemStack stack, LocalPlayer player) {
        final int useDuration = stack.getUseDuration(player);
        final int remaining = player.getUseItemRemainingTicks();
        final int chargeDuration = ScopeCrossbow.getChargeDurationTicks(stack);

        if (chargeDuration <= 0) {
            return 0.0f;
        }

        final float progress = (float) (useDuration - remaining) / (float) chargeDuration;
        return Mth.clamp(progress, 0.0f, 1.0f);
    }

}