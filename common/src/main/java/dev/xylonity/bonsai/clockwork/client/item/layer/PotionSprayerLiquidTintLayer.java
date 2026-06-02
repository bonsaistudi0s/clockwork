package dev.xylonity.bonsai.clockwork.client.item.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xylonity.bonsai.clockwork.client.item.renderer.PotionSprayerRenderer;
import dev.xylonity.bonsai.clockwork.common.item.sprayer.PotionSprayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import static dev.xylonity.bonsai.clockwork.client.item.renderer.PotionSprayerRenderer.LIQUID_TEXTURE;
import static dev.xylonity.bonsai.clockwork.client.item.renderer.PotionSprayerRenderer.isLiquidBone;

public class PotionSprayerLiquidTintLayer extends GeoRenderLayer<PotionSprayer> {

    private final PotionSprayerRenderer potionSprayerRenderer;

    public PotionSprayerLiquidTintLayer(GeoItemRenderer<PotionSprayer> renderer) {
        super(renderer);
        this.potionSprayerRenderer = (PotionSprayerRenderer) renderer;
    }

    @Override
    public void render(PoseStack poseStack, PotionSprayer animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        final RenderType liquidType = RenderType.entityTranslucent(LIQUID_TEXTURE);
        final VertexConsumer liquidBuffer = bufferSource.getBuffer(liquidType);

        final int colour = 0xFF000000 | (potionSprayerRenderer.getPotionColor() & 0x00FFFFFF);

        for (final GeoBone topBone : bakedModel.topLevelBones()) {
            renderLiquidRecursive(poseStack, animatable, topBone, liquidType, bufferSource, liquidBuffer, partialTick, packedLight, packedOverlay, colour);
        }

    }

    private void renderLiquidRecursive(PoseStack poseStack, PotionSprayer animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (isLiquidBone(bone) && !bone.isHidden()) {
            getRenderer().renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, colour);
            return;
        }

        for (GeoBone child : bone.getChildBones()) {
            renderLiquidRecursive(poseStack, animatable, child, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay, colour);
        }

    }

}