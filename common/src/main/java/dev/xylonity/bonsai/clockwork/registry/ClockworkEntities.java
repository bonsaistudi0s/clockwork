package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.passive.BrokenDragonflyEntity;
import dev.xylonity.bonsai.clockwork.common.entity.passive.DragonflyEntity;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.ClockworkArrowProjectile;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.ClockworkWingsBoostProjectile;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.trigger.FlamethrowerTriggerProjectile;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.trigger.PotionSprayTriggerProjectile;
import dev.xylonity.bonsai.clockwork.common.entity.tool.ClockworkDrillEntity;
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
    public static final ResourceEntry<EntityType<BrokenDragonflyEntity>> BROKEN_DRAGONFLY;

    public static final ResourceEntry<EntityType<ClockworkWingsBoostProjectile>> CLOCKWORK_WINGS_BOOST_PROJECTILE;
    public static final ResourceEntry<EntityType<ClockworkArrowProjectile>> CLOCKWORK_ARROW_PROJECTILE;

    public static final ResourceEntry<EntityType<PotionSprayTriggerProjectile>> POTION_SPRAY_TRIGGER_PROJECTILE;
    public static final ResourceEntry<EntityType<FlamethrowerTriggerProjectile>> FLAMETHROWER_TRIGGER_PROJECTILE;

    public static final ResourceEntry<EntityType<ClockworkDrillEntity>> CLOCKWORK_DRILL;

    static {
        DRAGONFLY = ENTITIES.registerEntity("dragonfly", DragonflyEntity::new, MobCategory.CREATURE, 1f, 1f);

        BROKEN_DRAGONFLY = ENTITIES.registerEntity("broken_dragonfly", BrokenDragonflyEntity::new, MobCategory.CREATURE, 1f, 1f);

        CLOCKWORK_WINGS_BOOST_PROJECTILE = ENTITIES.registerEntity("clockwork_wings_boost_projectile", ClockworkWingsBoostProjectile::new, MobCategory.MISC, 0.1f, 0.1f, List.of(EntityType.Builder::noSummon));
        CLOCKWORK_ARROW_PROJECTILE = ENTITIES.registerEntity("clockwork_arrow_projectile", ClockworkArrowProjectile::new, MobCategory.MISC, 0.4f, 0.4f, List.of(EntityType.Builder::noSummon));

        POTION_SPRAY_TRIGGER_PROJECTILE = ENTITIES.registerEntity("potion_spray_trigger_projectile", PotionSprayTriggerProjectile::new, MobCategory.MISC, 0.6f, 0.6f, List.of(EntityType.Builder::noSummon));
        FLAMETHROWER_TRIGGER_PROJECTILE = ENTITIES.registerEntity("flamethrower_trigger_projectile", FlamethrowerTriggerProjectile::new, MobCategory.MISC, 0.6f, 0.6f, List.of(EntityType.Builder::noSummon, EntityType.Builder::fireImmune));

        CLOCKWORK_DRILL = ENTITIES.registerEntity("clockwork_drill", ClockworkDrillEntity::new, MobCategory.MISC, 0.7f, 0.7f);
    }

}
