package dev.xylonity.bonsai.clockwork.client.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.xylonity.bonsai.clockwork.registry.ClockworkParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record FlamethrowerParticleData(
        float vx,
        float vy,
        float vz,
        int shooterId,
        boolean clientSpawned
) implements ParticleOptions {

    public static final MapCodec<FlamethrowerParticleData> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    com.mojang.serialization.Codec.FLOAT.fieldOf("vx").forGetter(d -> d.vx),
                    com.mojang.serialization.Codec.FLOAT.fieldOf("vy").forGetter(d -> d.vy),
                    com.mojang.serialization.Codec.FLOAT.fieldOf("vz").forGetter(d -> d.vz),
                    com.mojang.serialization.Codec.INT.fieldOf("shooter_id").forGetter(d -> d.shooterId)
            ).apply(instance, (vx, vy, vz, id) -> new FlamethrowerParticleData(vx, vy, vz, id, false))
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FlamethrowerParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, FlamethrowerParticleData::vx,
            ByteBufCodecs.FLOAT, FlamethrowerParticleData::vy,
            ByteBufCodecs.FLOAT, FlamethrowerParticleData::vz,
            ByteBufCodecs.VAR_INT, FlamethrowerParticleData::shooterId,
            (vx, vy, vz, id) -> new FlamethrowerParticleData(vx, vy, vz, id, false)
    );

    public FlamethrowerParticleData(float vx, float vy, float vz) {
        this(vx, vy, vz, -1, false);
    }

    public FlamethrowerParticleData(float vx, float vy, float vz, int shooterId) {
        this(vx, vy, vz, shooterId, false);
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return ClockworkParticles.FLAMETHROWER_PARTICLE.get();
    }

}
