package dev.xylonity.bonsai.clockwork.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Utility class to apply a dynamic sway effect to 3D render items.
 * The code is broken down exactly into all the subchains of calls to the corresponding
 * methods (and fields) so that it is readable and can be used in other contexts
 * @author Xylonity
 */
public class ClientSwayUtil {

    private float fallSway = 0f;
    private float prevFallSway = 0f;
    private int lastTick = -1;

    /**
     * Applies sway mutations to an item (in FPP)
     */
    public void applyFirstPersonSway(PoseStack poseStack, ItemDisplayContext context, float partialTick) {
        if (!isFirstPerson(context)) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        boolean leftHand = (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
        int handSign = leftHand ? -1 : 1;

        // Base position and initial rotations (thus applying the mutation to the inverted Y)
        poseStack.translate(1.5 / 16.0, -0.25 / 16.0, 0.0);
        poseStack.mulPose(Axis.XP.rotationDegrees(2.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(-9.5f));
        if (leftHand) {
            poseStack.mulPose(Axis.YP.rotationDegrees(19.0f));
        }

        applyHeadSway(poseStack, player, partialTick, handSign);

        if (Minecraft.getInstance().options.bobView().get()) {
            applyMovementBobbing(poseStack, player, partialTick, handSign);
        }

        applyFallSway(poseStack, player, partialTick);
    }

    /**
     * Applies sway based on the head movement (depending on the azimuth rotations)
     */
    private void applyHeadSway(PoseStack poseStack, LocalPlayer player, float partialTick, int handSign) {
        float bobPitch = Mth.rotLerp(partialTick, player.xBobO, player.xBob);
        float headPitch = Mth.rotLerp(partialTick, player.xRotO, player.getXRot());
        float swayPitch = headPitch - bobPitch;

        float bobYaw = Mth.rotLerp(partialTick, player.yBobO, player.yBob);
        float headYaw = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
        float swayYaw = headYaw - bobYaw;

        float sprintBoost = player.isSprinting() ? 1.15f : 1.0f;
        float sensitivity = 0.60f * sprintBoost;
        float rollFromYaw = swayYaw * 0.35f;

        poseStack.mulPose(Axis.YP.rotationDegrees(swayYaw * sensitivity * handSign));
        poseStack.mulPose(Axis.XP.rotationDegrees(swayPitch * sensitivity));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rollFromYaw * sensitivity * handSign));
    }

    /**
     * Applies a bobbing effect while moving
     */
    private void applyMovementBobbing(PoseStack poseStack, LocalPlayer player, float partialTick, int handSign) {
        float deltaWalk = player.walkDist - player.walkDistO;
        float walk = -(player.walkDist + deltaWalk * partialTick);
        float bob = Mth.lerp(partialTick, player.oBob, player.bob);
        float bobScale = player.isSprinting() ? 0.90f : 0.50f;

        poseStack.mulPose(Axis.XP.rotationDegrees(-(Math.abs(Mth.cos(walk * (float) Math.PI - 0.2F) * bob) * 3.5F) * bobScale));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-(Mth.sin(walk * (float)Math.PI) * bob * 2.0F) * bobScale * handSign));
        poseStack.translate(
                -(Mth.sin(walk * (float) Math.PI) * bob * 0.15F) * handSign,
                Math.abs(Mth.cos(walk * (float) Math.PI) * bob) * 0.12F,
                0.0D
        );
    }

    /**
     * Applies a minimal sway when falling or jumping
     */
    private void applyFallSway(PoseStack poseStack, LocalPlayer player, float partialTick) {
        int tick = player.tickCount;
        if (tick != lastTick) {
            lastTick = tick;
            prevFallSway = fallSway;

            float deltaY = (float) Mth.clamp((player.yo - player.getY()), -1.0, 1.0);
            float pitchNow = Mth.rotLerp(1.0f, player.xRotO, player.getXRot());
            float pitchFactor = 1.0f - (Math.abs(pitchNow) / 90.0f) * 0.5f;

            float sensitivityDegrees = (player.isSprinting() ? 60f : 45f);
            float target = deltaY * sensitivityDegrees * pitchFactor;

            if (Math.abs(target) < 0.15f) {
                target = 0f;
            }

            fallSway = Mth.approach(fallSway, target, player.onGround() ? 6.0f : 10.0f);
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, prevFallSway, fallSway)));
    }

    private static boolean isFirstPerson(ItemDisplayContext ctx) {
        return ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }

    public void reset() {
        fallSway = 0f;
        prevFallSway = 0f;
        lastTick = -1;
    }

}