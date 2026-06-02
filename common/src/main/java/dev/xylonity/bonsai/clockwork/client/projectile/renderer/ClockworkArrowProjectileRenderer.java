package dev.xylonity.bonsai.clockwork.client.projectile.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xylonity.bonsai.clockwork.client.projectile.model.ClockworkArrowProjectileModel;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.ClockworkArrowProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ClockworkArrowProjectileRenderer extends GeoEntityRenderer<ClockworkArrowProjectile> {

    public ClockworkArrowProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ClockworkArrowProjectileModel());
    }

    @Override
    protected void applyRotations(ClockworkArrowProjectile animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);

        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-(Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot()))));

        float shake = animatable.shakeTime - partialTick;
        if (shake > 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(shake * 3F) * shake));
        }

         poseStack.mulPose(Axis.YP.rotationDegrees(90));
    }

}