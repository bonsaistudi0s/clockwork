package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.knightlib.KnightLib;
import dev.xylonity.knightlib.api.registrar.ResourceDispatcher;
import dev.xylonity.knightlib.api.registrar.ResourceEntry;
import dev.xylonity.knightlib.api.registrar.ResourceRegistry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public class ClockworkParticles {

    public static final ResourceRegistry<ParticleType<?>> PARTICLES = ResourceDispatcher.create(BuiltInRegistries.PARTICLE_TYPE, Clockwork.MOD_ID);

    public static final ResourceEntry<SimpleParticleType> POTION_SPRAY = PARTICLES.register("potion_spray", KnightLib.PLATFORM.createParticle(true));

}
