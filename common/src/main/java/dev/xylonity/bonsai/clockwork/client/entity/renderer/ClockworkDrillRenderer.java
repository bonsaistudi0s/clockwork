package dev.xylonity.bonsai.clockwork.client.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xylonity.bonsai.clockwork.client.entity.model.ClockworkDrillModel;
import dev.xylonity.bonsai.clockwork.common.entity.tool.ClockworkDrillEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ClockworkDrillRenderer extends GeoEntityRenderer<ClockworkDrillEntity> {

    public ClockworkDrillRenderer(EntityRendererProvider.Context context) {
        super(context, new ClockworkDrillModel());
        this.shadowRadius = 0.4f;
    }

    @Override
    protected void applyRotations(ClockworkDrillEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot())));
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick);
    }

}