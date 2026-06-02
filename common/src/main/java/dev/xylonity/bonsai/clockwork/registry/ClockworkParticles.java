package dev.xylonity.bonsai.clockwork.registry;

import com.mojang.serialization.MapCodec;
import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.particle.FlamethrowerParticleData;
import dev.xylonity.bonsai.clockwork.client.particle.PotionSprayParticleData;
import dev.xylonity.knightlib.api.registrar.ResourceDispatcher;
import dev.xylonity.knightlib.api.registrar.ResourceEntry;
import dev.xylonity.knightlib.api.registrar.ResourceRegistry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ClockworkParticles {

    public static final ResourceRegistry<ParticleType<?>> PARTICLES = ResourceDispatcher.create(BuiltInRegistries.PARTICLE_TYPE, Clockwork.MOD_ID);

    public static final ResourceEntry<ParticleType<PotionSprayParticleData>> POTION_SPRAY =
            PARTICLES.register("potion_spray", () -> new ParticleType<>(false) {
                @Override
                public MapCodec<PotionSprayParticleData> codec() {
                    return PotionSprayParticleData.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, PotionSprayParticleData> streamCodec() {
                    return PotionSprayParticleData.STREAM_CODEC;
                }

            });

    public static final ResourceEntry<ParticleType<FlamethrowerParticleData>> FLAMETHROWER_PARTICLE =
            PARTICLES.register("flamethrower", () -> new ParticleType<>(false) {
                @Override
                public MapCodec<FlamethrowerParticleData> codec() {
                    return FlamethrowerParticleData.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, FlamethrowerParticleData> streamCodec() {
                    return FlamethrowerParticleData.STREAM_CODEC;
                }

            });

}
