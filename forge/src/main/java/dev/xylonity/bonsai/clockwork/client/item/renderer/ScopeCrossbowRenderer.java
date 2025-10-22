package dev.xylonity.bonsai.clockwork.client.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.item.model.ScopeCrossbowModel;
import dev.xylonity.bonsai.clockwork.client.util.ClientSwayUtil;
import dev.xylonity.bonsai.clockwork.common.item.ScopeCrossbow;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ScopeCrossbowRenderer extends GeoItemRenderer<ScopeCrossbow> {

    private ItemDisplayContext lastTransform = ItemDisplayContext.NONE;
    private final ClientSwayUtil swayUtil;

    public ScopeCrossbowRenderer() {
        super(new ScopeCrossbowModel());
        this.swayUtil = new ClientSwayUtil();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buf, int light, int overlay) {

        this.lastTransform = ctx;

        if (ctx == ItemDisplayContext.GUI || ctx == ItemDisplayContext.GROUND || ctx == ItemDisplayContext.FIXED) {
            Minecraft mc = Minecraft.getInstance();

            pose.pushPose();

            if (ctx == ItemDisplayContext.GUI) {
                pose.translate(0.5, 0.5, 0);
            }
            else if (ctx == ItemDisplayContext.GROUND) {
                pose.translate(0.5, 0.5, 0.5);
            }
            else {
                pose.translate(0.5, 0.5, 0);
                pose.scale(0.75f, 0.75f, 0.75f);
            }

            ModelResourceLocation modelRl = new ModelResourceLocation(Clockwork.MOD_ID, getModelForState(stack, mc.player), "inventory");
            BakedModel model = mc.getModelManager().getModel(modelRl);

            if (model == mc.getModelManager().getMissingModel()) {
                modelRl = new ModelResourceLocation(Clockwork.MOD_ID, "scope_crossbow_standby", "inventory");
                model = mc.getModelManager().getModel(modelRl);
            }

            mc.getItemRenderer().render(stack, ctx, false, pose, buf, light, overlay, model);

            pose.popPose();

            return;
        }

        super.renderByItem(stack, ctx, pose, buf, light, overlay);
    }

    private String getModelForState(ItemStack stack, LocalPlayer player) {
        if (CrossbowItem.isCharged(stack)) {
            if (CrossbowItem.containsChargedProjectile(stack, Items.FIREWORK_ROCKET)) {
                return "scope_crossbow_firework";
            }
            else if (CrossbowItem.containsChargedProjectile(stack, ClockworkItems.CLOCKWORK_ARROW.get())) {
                return "scope_crossbow_clockworkarrow";
            }

            return "scope_crossbow_arrow";
        }

        if (player != null && player.isUsingItem() && player.getMainHandItem() == stack && !CrossbowItem.isCharged(stack)) {

            float pullProgress = getPullProgress(stack, player);

            if (pullProgress >= 1.0f) {
                return "scope_crossbow_pulling_2";
            }
            else if (pullProgress >= 0.58f) {
                return "scope_crossbow_pulling_1";
            }
            else {
                return "scope_crossbow_pulling_0";
            }

        }

        return "scope_crossbow_standby";
    }

    private float getPullProgress(ItemStack stack, LocalPlayer player) {
        int useDuration = stack.getUseDuration();
        int remaining = player.getUseItemRemainingTicks();
        int chargeDuration = ScopeCrossbow.getChargeDurationTicks(stack);

        if (chargeDuration <= 0) {
            return 0.0f;
        }

        return Math.min(Math.max((float)(useDuration - remaining) / (float) chargeDuration, 0.0f), 1.0f);
    }

    @Override
    public void preRender(PoseStack poseStack, ScopeCrossbow animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        swayUtil.applyFirstPersonSway(poseStack, lastTransform, partialTick);
    }

}