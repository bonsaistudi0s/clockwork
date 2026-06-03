package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.knightlib.api.entity.spawn.SpawnConfig;
import dev.xylonity.knightlib.api.spawn.KnightLibEntityBiomeSpawns;
import net.minecraft.world.entity.MobCategory;

public class ClockworkEntitySpawns {

    private static final String FALLBACK = "30, 1, 1, minecraft:jungle, minecraft:old_growth_spruce_taiga, minecraft:old_growth_pine_taiga";

    public static void init() {
        final SpawnConfig brokenDragonfly = parse(ClockworkConfig.BROKEN_DRAGONFLY_SPAWN, FALLBACK);
        if (brokenDragonfly.weight > 0) {
            KnightLibEntityBiomeSpawns.builder(ClockworkEntities.BROKEN_DRAGONFLY, MobCategory.CREATURE)
                    .spawnRate(brokenDragonfly.weight, brokenDragonfly.minCount, brokenDragonfly.maxCount)
                    .biomeFilter(brokenDragonfly::matches)
                    .submit();
        }

    }

    private static SpawnConfig parse(String configLine, String fallback) {
        try {
            return SpawnConfig.parse(configLine);
        }
        catch (Exception exception) {
            Clockwork.LOGGER.error("[Clockwork] Invalid spawn config \"{}\", falling back to defaults", configLine, exception);
            return SpawnConfig.parse(fallback);
        }

    }

}
