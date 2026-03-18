package dev.xylonity.bonsai.clockwork.registry;

import com.mojang.serialization.Codec;
import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.particle.FlamethrowerParticleData;
import dev.xylonity.bonsai.clockwork.client.particle.PotionSprayParticleData;
import dev.xylonity.knightlib.api.registrar.ResourceDispatcher;
import dev.xylonity.knightlib.api.registrar.ResourceEntry;
import dev.xylonity.knightlib.api.registrar.ResourceRegistry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public class ClockworkParticles {

    public static final ResourceRegistry<ParticleType<?>> PARTICLES = ResourceDispatcher.create(BuiltInRegistries.PARTICLE_TYPE, Clockwork.MOD_ID);

    public static final ResourceEntry<ParticleType<PotionSprayParticleData>> POTION_SPRAY =
            PARTICLES.register("potion_spray", () -> new ParticleType<>(false, PotionSprayParticleData.DESERIALIZER) {
                @Override
                public Codec<PotionSprayParticleData> codec() {
                    return PotionSprayParticleData.CODEC;
                }

            });

    public static final ResourceEntry<ParticleType<FlamethrowerParticleData>> FLAMETHROWER_PARTICLE =
            PARTICLES.register("flamethrower", () -> new ParticleType<>(false, FlamethrowerParticleData.DESERIALIZER) {
                @Override
                public Codec<FlamethrowerParticleData> codec() {
                    return FlamethrowerParticleData.CODEC;
                }

            });

}
