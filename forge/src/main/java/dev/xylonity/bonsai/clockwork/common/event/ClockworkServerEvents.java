package dev.xylonity.bonsai.clockwork.common.event;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.passive.BrokenDragonflyEntity;
import dev.xylonity.bonsai.clockwork.common.entity.passive.DragonflyEntity;
import dev.xylonity.bonsai.clockwork.common.item.ClockworkWings;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.network.packets.ClockworkWingsC2SPacket;
import dev.xylonity.bonsai.clockwork.network.packets.DragonflyAscendKeyC2SPacket;
import dev.xylonity.bonsai.clockwork.network.packets.GenericSoundC2SPacket;
import dev.xylonity.bonsai.clockwork.network.packets.PotionSprayerParticlesC2SPacket;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.knightlib.api.network.Network;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ClockworkServerEvents {

    @Mod.EventBusSubscriber(modid = Clockwork.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClockworkServerModBus {

        @SubscribeEvent
        public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
            event.put(ClockworkEntities.DRAGONFLY.get(), DragonflyEntity.setAttributes().build());
            event.put(ClockworkEntities.BROKEN_DRAGONFLY.get(), BrokenDragonflyEntity.setAttributes().build());
        }

        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                Network.register(ClockworkWingsC2SPacket.TYPE);
                Network.register(DragonflyAscendKeyC2SPacket.TYPE);
                Network.register(GenericSoundC2SPacket.TYPE);
                Network.register(PotionSprayerParticlesC2SPacket.TYPE);
            });
        }

    }

    @Mod.EventBusSubscriber(modid = Clockwork.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClockworkServerGameBus {

        /**
         * Handles clockwork wings automatic position sink
         */
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {

                Player player = event.player;
                if (!player.isFallFlying()) return;
                if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ClockworkWings)) return;

                Vec3 movement = player.getDeltaMovement();

                movement = new Vec3(movement.x * ClockworkConfig.CLOCKWORK_WINGS_DEFAULT_VELOCITY, movement.y, movement.z * ClockworkConfig.CLOCKWORK_WINGS_DEFAULT_VELOCITY);

                if (movement.y < 0.0) {
                    movement = movement.add(0.0, -ClockworkConfig.CLOCKWORK_WINGS_DEFAULT_SINK, 0.0);
                }

                player.setDeltaMovement(movement);
                player.hasImpulse = true;
            }

        }

    }

}
