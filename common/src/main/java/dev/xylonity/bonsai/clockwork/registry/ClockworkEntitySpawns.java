package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.knightlib.api.spawn.KnightLibEntityBiomeSpawns;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biomes;

public class ClockworkEntitySpawns {

    public static void init() {
        KnightLibEntityBiomeSpawns.builder(ClockworkEntities.BROKEN_DRAGONFLY, MobCategory.CREATURE)
                .spawnRate(30, 1, 1)
                .biomeFilter(biomeHolder -> biomeHolder.is(Biomes.JUNGLE) || biomeHolder.is(Biomes.OLD_GROWTH_SPRUCE_TAIGA) || biomeHolder.is(Biomes.OLD_GROWTH_PINE_TAIGA))
                .submit();

    }

}
