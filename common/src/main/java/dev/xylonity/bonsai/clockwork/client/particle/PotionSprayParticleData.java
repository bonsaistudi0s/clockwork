package dev.xylonity.bonsai.clockwork.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.xylonity.bonsai.clockwork.registry.ClockworkParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record PotionSprayParticleData(
        float velX,
        float velY,
        float velZ,
        int color
) implements ParticleOptions {

    public static final Codec<PotionSprayParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("velX").forGetter(data -> data.velX),
            Codec.FLOAT.fieldOf("velY").forGetter(data -> data.velY),
            Codec.FLOAT.fieldOf("velZ").forGetter(data -> data.velZ),
            Codec.INT.fieldOf("color").forGetter(data -> data.color)
    ).apply(instance, PotionSprayParticleData::new));

    public static final Deserializer<PotionSprayParticleData> DESERIALIZER =
            new Deserializer<>() {
                @Override
                public PotionSprayParticleData fromCommand(ParticleType<PotionSprayParticleData> type, StringReader reader) throws CommandSyntaxException {
                    reader.expect(' ');
                    final float velX = reader.readFloat();
                    reader.expect(' ');
                    final float velY = reader.readFloat();
                    reader.expect(' ');
                    final float velZ = reader.readFloat();
                    reader.expect(' ');
                    final int color = reader.readInt();
                    return new PotionSprayParticleData(velX, velY, velZ, color);
                }

                @Override
                public PotionSprayParticleData fromNetwork(ParticleType<PotionSprayParticleData> type, FriendlyByteBuf buf) {
                    return new PotionSprayParticleData(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readInt());
                }

            };

    @Override
    public @NotNull ParticleType<?> getType() {
        return ClockworkParticles.POTION_SPRAY.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(velX);
        buf.writeFloat(velY);
        buf.writeFloat(velZ);
        buf.writeInt(color);
    }

    @Override
    public @NotNull String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %d", BuiltInRegistries.PARTICLE_TYPE.getKey(getType()), velX, velY, velZ, color);
    }

}