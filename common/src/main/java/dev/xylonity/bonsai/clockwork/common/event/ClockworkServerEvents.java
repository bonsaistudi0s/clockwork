package dev.xylonity.bonsai.clockwork.common.event;

import dev.xylonity.bonsai.clockwork.common.entity.passive.BrokenDragonflyEntity;
import dev.xylonity.bonsai.clockwork.common.entity.passive.DragonflyEntity;
import dev.xylonity.bonsai.clockwork.common.item.wings.ClockworkWings;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.knightlib.api.event.RegisterEvent;
import dev.xylonity.knightlib.api.event.impl.interop.TickPhase;
import dev.xylonity.knightlib.api.event.impl.server.EntityAttributeRegistrationEvent;
import dev.xylonity.knightlib.api.event.impl.server.ServerPlayerTickEvent;
import dev.xylonity.knightlib.api.event.impl.server.SpawnPlacementRegistrationEvent;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class ClockworkServerEvents {

    @RegisterEvent
    public static void registerEntityAttributes(final EntityAttributeRegistrationEvent event) {
        event.register(ClockworkEntities.DRAGONFLY.get(), DragonflyEntity::setAttributes);
        event.register(ClockworkEntities.BROKEN_DRAGONFLY.get(), BrokenDragonflyEntity::setAttributes);
    }

    @RegisterEvent
    public static void registerSpawnPlacements(final SpawnPlacementRegistrationEvent event) {
        event.register(ClockworkEntities.BROKEN_DRAGONFLY.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkMobSpawnRules);
    }

    @RegisterEvent
    public static void onPlayerTick(final ServerPlayerTickEvent event) {
        if (event.getPhase() == TickPhase.END) {
            final Player player = event.getPlayer();
            if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ClockworkWings)) {
                return;
            }

            Vec3 movement = player.getDeltaMovement();
            if (player.isFallFlying()) {
                movement = new Vec3(movement.x * ClockworkConfig.CLOCKWORK_WINGS_DEFAULT_VELOCITY, movement.y, movement.z * ClockworkConfig.CLOCKWORK_WINGS_DEFAULT_VELOCITY);
                if (movement.y < 0.0) {
                    movement = movement.add(0.0, -ClockworkConfig.CLOCKWORK_WINGS_DEFAULT_SINK, 0.0);
                }

                player.setDeltaMovement(movement);
                player.hurtMarked = true;
            }
            else {
                if (player.getDeltaMovement().y < -0.85) {
                    player.fallDistance = Math.max(0, player.fallDistance - 0.3f);
                }
            }

        }

    }

}
