package dev.xylonity.bonsai.clockwork.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.entity.renderer.BrokenDragonflyRenderer;
import dev.xylonity.bonsai.clockwork.client.entity.renderer.DragonflyRenderer;
import dev.xylonity.bonsai.clockwork.client.particle.PotionSprayParticle;
import dev.xylonity.bonsai.clockwork.client.projectile.renderer.ClockworkArrowProjectileRenderer;
import dev.xylonity.bonsai.clockwork.client.projectile.renderer.GenericProjectileRenderer;
import dev.xylonity.bonsai.clockwork.client.sound.PotionSprayerSoundHandler;
import dev.xylonity.bonsai.clockwork.common.entity.passive.DragonflyEntity;
import dev.xylonity.bonsai.clockwork.common.item.ClockworkWings;
import dev.xylonity.bonsai.clockwork.client.sound.ClientSoundProxy;
import dev.xylonity.bonsai.clockwork.client.sound.handler.custom.DragonflySoundHandler;
import dev.xylonity.bonsai.clockwork.client.sound.Sounds;
import dev.xylonity.bonsai.clockwork.network.packets.ClockworkWingsC2SPacket;
import dev.xylonity.bonsai.clockwork.network.packets.DragonflyAscendKeyC2SPacket;
import dev.xylonity.bonsai.clockwork.network.packets.PotionSprayerParticlesC2SPacket;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.bonsai.clockwork.registry.ClockworkParticles;
import dev.xylonity.knightlib.api.network.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

public class ClockworkClientEvents {

    @Mod.EventBusSubscriber(modid = Clockwork.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClockworkClientModEvents {

        @SubscribeEvent
        public static void registerEntityRenderers(FMLClientSetupEvent event) {
            EntityRenderers.register(ClockworkEntities.DRAGONFLY.get(), DragonflyRenderer::new);

            EntityRenderers.register(ClockworkEntities.BROKEN_DRAGONFLY.get(), BrokenDragonflyRenderer::new);

            EntityRenderers.register(ClockworkEntities.CLOCKWORK_WINGS_BOOST_PROJECTILE.get(), GenericProjectileRenderer::new);
            EntityRenderers.register(ClockworkEntities.POTION_SPRAY_TRIGGER_PROJECTILE.get(), GenericProjectileRenderer::new);

            EntityRenderers.register(ClockworkEntities.CLOCKWORK_ARROW_PROJECTILE.get(), ClockworkArrowProjectileRenderer::new);

            ClientSoundProxy soundProxy = new ClientSoundProxy();
            soundProxy.registerHandler(new DragonflySoundHandler());
            soundProxy.registerHandler(new PotionSprayerSoundHandler());
            Sounds.registerClientProxy(soundProxy);

            Network.register(PotionSprayerParticlesC2SPacket.TYPE);
        }

        @SubscribeEvent
        public static void onModelRegistry(ModelEvent.RegisterAdditional event) {
            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "barrel_crossbow_standby", "inventory"));
            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "barrel_crossbow_pulling_0", "inventory"));
            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "barrel_crossbow_pulling_1", "inventory"));
            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "barrel_crossbow_pulling_2", "inventory"));
            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "barrel_crossbow_arrow", "inventory"));
            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "barrel_crossbow_clockworkarrow", "inventory"));
            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "barrel_crossbow_firework", "inventory"));

            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "scope_crossbow_standby", "inventory"));
            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "scope_crossbow_pulling_0", "inventory"));
            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "scope_crossbow_pulling_1", "inventory"));
            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "scope_crossbow_pulling_2", "inventory"));
            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "scope_crossbow_arrow", "inventory"));
            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "scope_crossbow_clockworkarrow", "inventory"));
            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "scope_crossbow_firework", "inventory"));

            event.register(new ModelResourceLocation(Clockwork.MOD_ID, "clockwork_potion_sprayer_2d", "inventory"));
        }

        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ClockworkParticles.POTION_SPRAY.get(), PotionSprayParticle.Provider::new);
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            //event.register(ClockworkKeyMappings.TOGGLE_DRAGONFLY_FLIGHT);
        }

    }

    @Mod.EventBusSubscriber(modid = Clockwork.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClockworkClientGameEvents {

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                if (event.getKey() == GLFW.GLFW_KEY_SPACE && event.getAction() == GLFW.GLFW_PRESS) {
                    if (Minecraft.getInstance().screen instanceof ChatScreen) return;
                    if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ClockworkWings) {
                        Network.sendToServer(new ClockworkWingsC2SPacket());
                    }
                }

            }

        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_SPACE)) {
                if (Minecraft.getInstance().screen instanceof ChatScreen) return;
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    if (player.getVehicle() instanceof DragonflyEntity e && e.getControllingPassenger() == player) {
                        Network.sendToServer(new DragonflyAscendKeyC2SPacket(true));
                    }
                }

            }

        }

    }


}