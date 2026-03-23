package dev.xylonity.bonsai.clockwork.client.particle;

import dev.xylonity.knightlib.api.util.KnightLibUtil;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class FlamethrowerParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected FlamethrowerParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;

        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        this.lifetime = 20 + random.nextInt(20);
        this.quadSize = 0.10f + random.nextFloat() * 0.15f;
        this.gravity = -0.01f;
        this.hasPhysics = false;

        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();

        this.quadSize *= 0.99f;

        this.gCol *= 0.96f;
        this.bCol *= 0.90f;

        if (sprites != null) {
            this.setSpriteFromAge(sprites);
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    public static class Provider implements ParticleProvider<FlamethrowerParticleData> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(@NotNull FlamethrowerParticleData data, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {

            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.player.getId() == data.shooterId() && minecraft.options.getCameraType() == CameraType.FIRST_PERSON && !data.clientSpawned()) {
                return null;
            }

            final Vec3 direction = KnightLibUtil.randomVectorInCone(
                    new Vec3(data.vx(), data.vy(), data.vz()), 40, new Random()
            );

            final double offset = 0.8 + new Random().nextDouble() * 0.4;
            return new FlamethrowerParticle(level, x, y, z,
                    direction.x * offset, direction.y * offset, direction.z * offset, sprites);
        }

    }

}