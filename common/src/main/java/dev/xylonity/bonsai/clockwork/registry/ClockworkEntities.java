package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.custom.DragonflyEntity;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.ClockworkWingsBoostProjectile;
import dev.xylonity.knightlib.api.registrar.ResourceDispatcher;
import dev.xylonity.knightlib.api.registrar.ResourceEntry;
import dev.xylonity.knightlib.api.registrar.ResourceRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.List;

public class ClockworkEntities {

    public static final ResourceRegistry<EntityType<?>> ENTITIES = ResourceDispatcher.create(BuiltInRegistries.ENTITY_TYPE, Clockwork.MOD_ID);

    public static final ResourceEntry<EntityType<DragonflyEntity>> DRAGONFLY;
    public static final ResourceEntry<EntityType<ClockworkWingsBoostProjectile>> CLOCKWORK_WINGS_BOOST_PROJECTILE;

    static {
        DRAGONFLY = ENTITIES.registerEntity("dragonfly", DragonflyEntity::new, MobCategory.CREATURE, 1f, 1f);
        CLOCKWORK_WINGS_BOOST_PROJECTILE = ENTITIES.registerEntity("clockwork_wings_boost_projectile", ClockworkWingsBoostProjectile::new, MobCategory.MISC, 0.1f, 0.1f, List.of(EntityType.Builder::noSummon));
    }

}
