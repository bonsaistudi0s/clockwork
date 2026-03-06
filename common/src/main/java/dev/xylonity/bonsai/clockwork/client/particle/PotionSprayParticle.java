package dev.xylonity.bonsai.clockwork.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xylonity.knightlib.api.util.KnightLibUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class PotionSprayParticle extends TextureSheetParticle {

    private final SpriteSet spritesset;

    private int frameIndex = 0;
    private boolean landed = false;
    private int landedFrameTick = 0;
    private int airFrameTick = 0;

    private static final int AIR_FRAME_DURATION_TICKS = 3;
    private static final int GROUND_FRAME_DURATION_TICKS = 3;
    private static final int MAX_FRAME_INDEX = 7;

    public PotionSprayParticle(ClientLevel world, double x, double y, double z, SpriteSet sprites, double velX, double velY, double velZ) {
        super(world, x, y, z);
        this.spritesset = sprites;

        this.quadSize = new Random().nextFloat(0.25f, 0.80f);
        this.alpha = 0.0F;

        this.gravity = 0.55f;
        this.hasPhysics = true;
        this.friction = 0.98F;

        this.lifetime = 600;

        this.xd = velX;
        this.yd = velY;
        this.zd = velZ;

        setFrame(0);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {
        super.render(buffer, camera, partialTicks);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age < 4) {
            this.alpha = (float) this.age / 5.0F;
        }
        else {
            this.alpha = 1.0F;
        }

        if (!landed) {
            if (frameIndex < 3) {
                airFrameTick++;
                if (airFrameTick >= AIR_FRAME_DURATION_TICKS) {
                    setFrame(frameIndex + 1);
                    airFrameTick = 0;
                }

            }

            if (this.onGround) {
                landed = true;
                landedFrameTick = 0;
                setFrame(4);
            }

        }
        else {
            landedFrameTick++;
            if (frameIndex < 7) {
                if (landedFrameTick >= GROUND_FRAME_DURATION_TICKS) {
                    setFrame(frameIndex + 1);
                    landedFrameTick = 0;
                }

            }
            else {
                if (landedFrameTick >= GROUND_FRAME_DURATION_TICKS) {
                    this.remove();
                    return;
                }

            }

            this.xd *= 0.85;
            this.zd *= 0.85;
            this.yd *= 0.5;
        }

        this.xd *= 0.98;
        this.yd *= 0.98;
        this.zd *= 0.98;
    }

    private void setFrame(int index) {
        this.frameIndex = clamp(index, 0, MAX_FRAME_INDEX);
        this.setSprite(this.spritesset.get(this.frameIndex, MAX_FRAME_INDEX + 1));
    }

    private static int clamp(int value, int low, int high) {
        return (value < low) ? low : (Math.min(value, high));
    }

    public static class Provider implements ParticleProvider<PotionSprayParticleData> {

        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(@NotNull PotionSprayParticleData data, @NotNull ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {

            final Vec3 direction = KnightLibUtil.randomVectorInCone(
                    new Vec3(data.velX(), data.velY(), data.velZ()), 45.0, new Random()
            );

            final double offset = 0.8 + new Random().nextDouble() * 0.4;
            final PotionSprayParticle particle = new PotionSprayParticle(
                    level, x, y, z, this.sprites,
                    direction.x * offset, direction.y * offset, direction.z * offset
            );

            particle.rCol = ((data.color() >> 16) & 0xFF) / 255f;
            particle.gCol = ((data.color() >> 8) & 0xFF) / 255f;
            particle.bCol = ((data.color()) & 0xFF) / 255f;

            return particle;
        }

    }

}