package dev.xylonity.bonsai.clockwork.common.item.flamethrower;

import dev.xylonity.bonsai.clockwork.client.particle.FlamethrowerParticleData;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Fast wrapper to handle first person flamethrower particles (the particle spawn location depends on the camera state)
 */
public final class FlamethrowerParticleHelper {

    public static void trySpawnFirstPersonParticles(Player player) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != player) {
            return;
        }
        if (minecraft.options.getCameraType() != CameraType.FIRST_PERSON) {
            return;
        }

        final Vec3 forward = player.getLookAngle().normalize();
        final float yawRad = player.getYRot() * Mth.DEG_TO_RAD;
        final Vec3 horizontalForward = new Vec3(-Mth.sin(yawRad), 0, Mth.cos(yawRad));
        final Vec3 right = horizontalForward.cross(new Vec3(0, 1, 0)).normalize();
        final Vec3 up = right.cross(forward).normalize();

        final double handSign = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? 1.0 : -1.0;
        final Vec3 spawnPosition = player.getEyePosition(1.0f)
                .add(right.scale(0.8 * handSign))
                .add(forward.scale(0.6))
                .add(up.scale(-0.35));

        final Vec3 baseVelocity = forward.scale(FlamethrowerItem.PARTICLE_BASE_SPEED);
        for (int i = 0; i < FlamethrowerItem.PARTICLE_COUNT; i++) {
            player.level().addParticle(
                    new FlamethrowerParticleData(
                            (float) baseVelocity.x, (float) baseVelocity.y, (float) baseVelocity.z,
                            player.getId(),
                            true
                    ),
                    spawnPosition.x + spread(player),
                    spawnPosition.y + spread(player),
                    spawnPosition.z + spread(player),
                    0, 0, 0
            );

        }

    }

    private static double spread(Player player) {
        return (player.level().random.nextDouble() - 0.5) * 0.1;
    }

}