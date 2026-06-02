package dev.xylonity.bonsai.clockwork.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.xylonity.bonsai.clockwork.registry.ClockworkParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record PotionSprayParticleData(
        float velX,
        float velY,
        float velZ,
        int color
) implements ParticleOptions {

    public static final MapCodec<PotionSprayParticleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("velX").forGetter(data -> data.velX),
            Codec.FLOAT.fieldOf("velY").forGetter(data -> data.velY),
            Codec.FLOAT.fieldOf("velZ").forGetter(data -> data.velZ),
            Codec.INT.fieldOf("color").forGetter(data -> data.color)
    ).apply(instance, PotionSprayParticleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PotionSprayParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, PotionSprayParticleData::velX,
            ByteBufCodecs.FLOAT, PotionSprayParticleData::velY,
            ByteBufCodecs.FLOAT, PotionSprayParticleData::velZ,
            ByteBufCodecs.INT, PotionSprayParticleData::color,
            PotionSprayParticleData::new
    );

    @Override
    public @NotNull ParticleType<?> getType() {
        return ClockworkParticles.POTION_SPRAY.get();
    }

}
