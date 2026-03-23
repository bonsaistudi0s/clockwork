package dev.xylonity.bonsai.clockwork.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Utility class to apply a dynamic sway effect to 3D rendered items. Every raw input (head rotation, movement, fall
 * velocity) is fed through an exponential decay smoothing pass so that transitions never snap
 *
 * @author Xylonity
 */
public class ClientSwayUtil {

    private static final double BASE_OFFSET_X = 1.5 / 16d;
    private static final double BASE_OFFSET_Y = -0.25 / 16d;
    private static final float BASE_PITCH = 2f;
    private static final float BASE_YAW = -9.5f;
    private static final float LEFT_HAND_YAW = 19f;

    private static final float HEAD_SENSITIVITY = 0.55f;
    private static final float HEAD_SPRINT_BOOST = 1.15f;
    private static final float HEAD_ROLL_FACTOR = 0.30f;
    private static final float HEAD_SMOOTH_FACTOR = 0.25f;

    private static final float BOB_SCALE_WALK = 0.50f;
    private static final float BOB_SCALE_SPRINT = 0.90f;
    private static final float BOB_SMOOTH_FACTOR = 0.15f;

    private static final float FALL_SENSITIVITY_WALK = 45f;
    private static final float FALL_SENSITIVITY_SPRINT = 60f;
    private static final float FALL_DEADZONE = 0.08f;
    private static final float FALL_SMOOTH_AIR = 0.12f;
    private static final float FALL_SMOOTH_GROUND = 0.18f;

    private float smoothYaw = 0f;
    private float smoothPitch = 0f;
    private float smoothRoll = 0f;

    private float fallSway = 0f;
    private float prevFallSway = 0f;
    private int lastTick = -1;

    private float smoothBobIntensity = 0f;

    /**
     * Applies sway mutations to an item rendered in first person
     */
    public void applyFirstPersonSway(PoseStack poseStack, ItemDisplayContext context, float partialTick) {
        if (!isFirstPerson(context)) {
            return;
        }

        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        final boolean leftHand = (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
        final int handSign = leftHand ? -1 : 1;

        applyBaseTransform(poseStack, leftHand);
        applyHeadSway(poseStack, player, partialTick);

        if (Minecraft.getInstance().options.bobView().get()) {
            applyMovementBobbing(poseStack, player, partialTick, handSign);
        }

        applyFallSway(poseStack, player, partialTick);
    }

    private static void applyBaseTransform(PoseStack pose, boolean leftHand) {
        pose.translate(BASE_OFFSET_X, BASE_OFFSET_Y, 0.0);
        pose.mulPose(Axis.XP.rotationDegrees(BASE_PITCH));
        pose.mulPose(Axis.YP.rotationDegrees(BASE_YAW));
        if (leftHand) {
            pose.mulPose(Axis.YP.rotationDegrees(LEFT_HAND_YAW));
        }

    }

    /**
     * Instead of applying the raw delta between head and bob rotations, I feed the target values through an exponential decay filter so
     * that sudden mouse flicks produce a cool smooth weapon lag effect.
     */
    private void applyHeadSway(PoseStack pose, LocalPlayer player, float partialTick) {
        final float bobPitch = Mth.rotLerp(partialTick, player.xBobO, player.xBob);
        final float headPitch = Mth.rotLerp(partialTick, player.xRotO, player.getXRot());
        final float rawPitch = headPitch - bobPitch;

        final float bobYaw = Mth.rotLerp(partialTick, player.yBobO, player.yBob);
        final float headYaw = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
        final float rawYaw = headYaw - bobYaw;

        final float sprintMultiplier = player.isSprinting() ? HEAD_SPRINT_BOOST : 1.0f;
        final float sensitivity = HEAD_SENSITIVITY * sprintMultiplier;

        final float targetYaw = rawYaw * sensitivity;
        final float targetPitch = rawPitch * sensitivity;
        final float targetRoll = rawYaw * HEAD_ROLL_FACTOR * sensitivity;

        // Smooth towards targets
        smoothYaw = ClientUtil.exponentialDecay(smoothYaw, targetYaw, HEAD_SMOOTH_FACTOR);
        smoothPitch = ClientUtil.exponentialDecay(smoothPitch, targetPitch, HEAD_SMOOTH_FACTOR);
        smoothRoll = ClientUtil.exponentialDecay(smoothRoll, targetRoll, HEAD_SMOOTH_FACTOR);

        pose.mulPose(Axis.YP.rotationDegrees(smoothYaw));
        pose.mulPose(Axis.XP.rotationDegrees(smoothPitch));
        pose.mulPose(Axis.ZP.rotationDegrees(smoothRoll));
    }

    /**
     * The vanilla's bob value can jump when the player starts or stops moving, so I smooth the intensity multiplier
     * so the bobbing fades in/out smoothly instead of popping
     */
    private void applyMovementBobbing(PoseStack pose, LocalPlayer player, float partialTick, int handSign) {
        final float deltaWalk = player.walkDist - player.walkDistO;
        final float walk = -(player.walkDist + deltaWalk * partialTick);
        final float rawBob = Mth.lerp(partialTick, player.oBob, player.bob);

        // Smoothing the bob intensity so start/stop transitions are gradual
        smoothBobIntensity = ClientUtil.exponentialDecay(smoothBobIntensity, rawBob, BOB_SMOOTH_FACTOR);

        final float bobScale = player.isSprinting() ? BOB_SCALE_SPRINT : BOB_SCALE_WALK;
        final float bob = smoothBobIntensity;
        final float piWalk = walk * (float) Math.PI;

        final float cosComponent = Math.abs(Mth.cos(piWalk - 0.2f) * bob);
        final float sinComponent = Mth.sin(piWalk) * bob;

        pose.mulPose(Axis.XP.rotationDegrees(-cosComponent * 3.5f * bobScale));
        pose.mulPose(Axis.ZP.rotationDegrees(-sinComponent * 2.0f * bobScale));
        pose.translate(
                -sinComponent * 0.15f,
                Math.abs(Mth.cos(piWalk) * bob) * 0.12f,
                0.0
        );

    }

    /**
     * Tracks the player's vertical velocity and converts it into a pitch rotation
     */
    private void applyFallSway(PoseStack pose, LocalPlayer player, float partialTick) {
        final int tick = player.tickCount;
        if (tick != lastTick) {
            lastTick = tick;
            prevFallSway = fallSway;

            final float deltaY = (float) Mth.clamp(player.yo - player.getY(), -1.0, 1.0);

            // Attenuates when looking straight down/up (less noticeable there)
            final float pitchNow = Mth.rotLerp(1.0f, player.xRotO, player.getXRot());
            final float pitchAttenuation = 1.0f - (Math.abs(pitchNow) / 90.0f) * 0.5f;

            final float sensitivity = player.isSprinting() ? FALL_SENSITIVITY_SPRINT : FALL_SENSITIVITY_WALK;
            float target = deltaY * sensitivity * pitchAttenuation;

            // Yes
            if (Math.abs(target) < FALL_DEADZONE) {
                target = 0f;
            }

            final float smoothRate = player.onGround() ? FALL_SMOOTH_GROUND : FALL_SMOOTH_AIR;
            fallSway = ClientUtil.exponentialDecay(fallSway, target, smoothRate);
        }

        final float interpolated = Mth.lerp(partialTick, prevFallSway, fallSway);
        pose.mulPose(Axis.XP.rotationDegrees(interpolated));
    }

    private boolean isFirstPerson(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }

}