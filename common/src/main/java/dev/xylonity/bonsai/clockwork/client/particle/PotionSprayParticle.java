package dev.xylonity.bonsai.clockwork.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xylonity.knightlib.api.util.KnightLibUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
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

    private static double defaultVelocityX = 0;
    private static double defaultVelocityY = 0;
    private static double defaultVelocityZ = 0;
    private static int rgb;

    public static void setDefaultVelocityAndColor(double vx, double vy, double vz, int color) {
        defaultVelocityX = vx;
        defaultVelocityY = vy;
        defaultVelocityZ = vz;
        rgb = color;
    }

    public PotionSprayParticle(ClientLevel world, double x, double y, double z, SpriteSet sprites, double velX, double velY, double velZ) {
        super(world, x, y, z);
        this.spritesset = sprites;

        this.quadSize = new Random().nextFloat(0.25f, 0.80f);
        this.alpha = 0.0F;

        int color = rgb;
        this.rCol = ((color >> 16) & 0xFF) / 255f;
        this.gCol = ((color >> 8) & 0xFF) / 255f;
        this.bCol = ((color) & 0xFF) / 255f;

        this.gravity = 0.55f;
        this.hasPhysics = true;
        this.friction = 0.98F;

        this.lifetime = 600;

        if (velX == 0 && velY == 0 && velZ == 0) {
            this.xd = defaultVelocityX;
            this.yd = defaultVelocityY;
            this.zd = defaultVelocityZ;
        }
        else {
            this.xd = velX;
            this.yd = velY;
            this.zd = velZ;
        }

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

    private static int clamp(int v, int lo, int hi) {
        return (v < lo) ? lo : (Math.min(v, hi));
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            if (dx == 0 && dy == 0 && dz == 0) {
                Vec3 v = KnightLibUtil.randomVectorInCone(new Vec3(defaultVelocityX, defaultVelocityY, defaultVelocityZ), 45.0, new Random());
                double f = 0.8 + new Random().nextDouble() * 0.4;
                dx = v.x * f; dy = v.y * f; dz = v.z * f;
            }

            PotionSprayParticle p = new PotionSprayParticle(level, x, y, z, this.sprites, dx, dy, dz);
            p.setPos(x, y, z);

            return p;
        }
    }

}
