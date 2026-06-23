package dev.xylonity.bonsai.clockwork.client.entity.model;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.tool.ClockworkDrillEntity;
import dev.xylonity.knightlib.api.util.KnightLibEasings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class ClockworkDrillModel extends GeoModel<ClockworkDrillEntity> {

    private static final ResourceLocation[] FRAMES = {
            Clockwork.resource("textures/entity/clockwork_drill_0.png"),
            Clockwork.resource("textures/entity/clockwork_drill_1.png"),
            Clockwork.resource("textures/entity/clockwork_drill_2.png"),
            Clockwork.resource("textures/entity/clockwork_drill_3.png")
    };

    @Override
    public ResourceLocation getModelResource(ClockworkDrillEntity entity) {
        return Clockwork.resource("geo/clockwork_drill.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ClockworkDrillEntity entity) {
        return FRAMES[(entity.tickCount / 4) % FRAMES.length];
    }

    @Override
    public ResourceLocation getAnimationResource(ClockworkDrillEntity entity) {
        return Clockwork.resource("animations/clockwork_drill.animation.json");
    }

    @Override
    public void setCustomAnimations(ClockworkDrillEntity entity, long instanceId, AnimationState<ClockworkDrillEntity> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        final float partialTick = animationState.getPartialTick();

        final CoreGeoBone drill1 = this.getAnimationProcessor().getBone("drill_1");
        if (drill1 != null) {
            float rawTilt = Mth.lerp(partialTick, entity.prevDrillTilt, entity.drillTilt);
            float easedTilt = KnightLibEasings.EASE_IN_OUT_SINE.apply(rawTilt);
            drill1.setRotX(easedTilt * 30f * Mth.DEG_TO_RAD);
        }

        final CoreGeoBone drill2 = this.getAnimationProcessor().getBone("drill_2");
        if (drill2 != null) {
            float spinAngle = Mth.lerp(partialTick, entity.prevDrillSpinAngle, entity.drillSpinAngle);
            drill2.setRotZ(spinAngle);
        }
    }

}