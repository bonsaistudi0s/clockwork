package dev.xylonity.bonsai.clockwork.client.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.item.model.PotionSprayerModel;
import dev.xylonity.bonsai.clockwork.client.util.ClientSwayUtil;
import dev.xylonity.bonsai.clockwork.common.item.PotionSprayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PotionSprayerRenderer extends GeoItemRenderer<PotionSprayer> {

    private ItemDisplayContext lastTransform = ItemDisplayContext.NONE;
    private final ClientSwayUtil swayUtil;

    public PotionSprayerRenderer() {
        super(new PotionSprayerModel());
        this.swayUtil = new ClientSwayUtil();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buf, int light, int overlay) {

        this.lastTransform = ctx;

        if (ctx == ItemDisplayContext.GUI || ctx == ItemDisplayContext.GROUND || ctx == ItemDisplayContext.FIXED) {
            Minecraft minecraft = Minecraft.getInstance();

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

            minecraft.getItemRenderer().render(stack, ctx, false, pose, buf, light, overlay, minecraft.getModelManager().getModel(new ModelResourceLocation(Clockwork.MOD_ID, "clockwork_potion_sprayer_2d", "inventory")));

            pose.popPose();

            return;
        }

        super.renderByItem(stack, ctx, pose, buf, light, overlay);
    }

    @Override
    public void preRender(PoseStack poseStack, PotionSprayer animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        swayUtil.applyFirstPersonSway(poseStack, lastTransform, partialTick);
    }

}