package dev.xylonity.bonsai.clockwork.common.event;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.custom.DragonflyEntity;
import dev.xylonity.bonsai.clockwork.common.item.ClockworkWings;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ClockworkServerEvents {

    @Mod.EventBusSubscriber(modid = Clockwork.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClockworkServerModBus {

        @SubscribeEvent
        public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
            event.put(ClockworkEntities.DRAGONFLY.get(), DragonflyEntity.setAttributes().build());
        }

    }

    @Mod.EventBusSubscriber(modid = Clockwork.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClockworkServerGameBus {

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            // Since the elytra flying properties are hardcoded, I cap the flying speed of the clockwork wings here
            if (event.phase == TickEvent.Phase.END) {

                Player player = event.player;
                if (!player.isFallFlying()) return;
                if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ClockworkWings)) return;

                Vec3 deltaMovement = player.getDeltaMovement();

                deltaMovement = new Vec3(deltaMovement.x * ClockworkConfig.CLOCKWORK_WINGS_DEFAULT_VELOCITY, deltaMovement.y, deltaMovement.z * ClockworkConfig.CLOCKWORK_WINGS_DEFAULT_VELOCITY);

                if (deltaMovement.y < 0.0) {
                    deltaMovement = deltaMovement.add(0.0, -ClockworkConfig.CLOCKWORK_WINGS_DEFAULT_SINK, 0.0);
                }

                player.setDeltaMovement(deltaMovement);
                player.hasImpulse = true;
            }

        }

    }

}
