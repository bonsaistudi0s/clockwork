package dev.xylonity.bonsai.clockwork.common.biome;

import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public class ClockworkMobSpawns {

    public static void addBiomeSpawns(Holder<Biome> biomeHolder, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (biomeHolder.is(Biomes.JUNGLE) || biomeHolder.is(Biomes.OLD_GROWTH_SPRUCE_TAIGA) || biomeHolder.is(Biomes.OLD_GROWTH_PINE_TAIGA)) {
            builder.getMobSpawnSettings().getSpawner(MobCategory.CREATURE).add(new MobSpawnSettings.SpawnerData(ClockworkEntities.BROKEN_DRAGONFLY.get(), 30, 1, 1));
        }

    }

}
