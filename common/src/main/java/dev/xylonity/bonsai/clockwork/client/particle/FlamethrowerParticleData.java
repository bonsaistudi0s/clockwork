package dev.xylonity.bonsai.clockwork.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.xylonity.bonsai.clockwork.registry.ClockworkParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record FlamethrowerParticleData(
        float vx,
        float vy,
        float vz,
        int shooterId,
        boolean clientSpawned
) implements ParticleOptions {

    public static final Codec<FlamethrowerParticleData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("vx").forGetter(d -> d.vx),
                    Codec.FLOAT.fieldOf("vy").forGetter(d -> d.vy),
                    Codec.FLOAT.fieldOf("vz").forGetter(d -> d.vz),
                    Codec.INT.fieldOf("shooter_id").forGetter(d -> d.shooterId)
            ).apply(instance, (vx, vy, vz, id) -> new FlamethrowerParticleData(vx, vy, vz, id, false))
    );

    @SuppressWarnings("deprecation")
    public static final Deserializer<FlamethrowerParticleData> DESERIALIZER = new Deserializer<>() {
        @Override
        public @NotNull FlamethrowerParticleData fromCommand(@NotNull ParticleType<FlamethrowerParticleData> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float vx = reader.readFloat();
            reader.expect(' ');
            float vy = reader.readFloat();
            reader.expect(' ');
            float vz = reader.readFloat();
            reader.expect(' ');
            int id = reader.readInt();
            return new FlamethrowerParticleData(vx, vy, vz, id, false);
        }

        @Override
        public @NotNull FlamethrowerParticleData fromNetwork(@NotNull ParticleType<FlamethrowerParticleData> type, FriendlyByteBuf buf) {
            return new FlamethrowerParticleData(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readVarInt(), false);
        }

    };

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

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(vx);
        buf.writeFloat(vy);
        buf.writeFloat(vz);
        buf.writeVarInt(shooterId);
    }

    @Override
    public @NotNull String writeToString() {
        return String.format("%s %.4f %.4f %.4f %d", ClockworkParticles.FLAMETHROWER_PARTICLE.getId(), vx, vy, vz, shooterId);
    }

}