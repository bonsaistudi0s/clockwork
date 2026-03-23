package dev.xylonity.bonsai.clockwork.client.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.item.model.FlamethrowerModel;
import dev.xylonity.bonsai.clockwork.client.item.model.PotionSprayerModel;
import dev.xylonity.bonsai.clockwork.client.util.ClientSwayUtil;
import dev.xylonity.bonsai.clockwork.client.util.ClientUtil;
import dev.xylonity.bonsai.clockwork.common.item.flamethrower.FlamethrowerItem;
import dev.xylonity.bonsai.clockwork.common.item.sprayer.PotionSprayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class FlamethrowerRenderer extends GeoItemRenderer<FlamethrowerItem> {

    private ItemDisplayContext lastTransform = ItemDisplayContext.NONE;
    private final ClientSwayUtil swayUtil;

    public FlamethrowerRenderer() {
        super(new FlamethrowerModel());
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
    public void preRender(PoseStack poseStack, FlamethrowerItem animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        swayUtil.applyFirstPersonSway(poseStack, lastTransform, partialTick);
    }

    private void renderStaticModel(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buf, int light, int overlay) {
        final Minecraft minecraft = Minecraft.getInstance();

        pose.pushPose();
        ClientUtil.applyStaticTransform(ctx, pose);

        final ModelResourceLocation modelResourceLocation = new ModelResourceLocation(Clockwork.MOD_ID, "clockwork_flamethrower_2d", "inventory");
        minecraft.getItemRenderer().render(stack, ctx, false, pose, buf, light, overlay, minecraft.getModelManager().getModel(modelResourceLocation));

        pose.popPose();
    }

}