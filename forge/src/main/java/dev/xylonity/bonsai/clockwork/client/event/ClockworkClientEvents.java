package dev.xylonity.bonsai.clockwork.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.entity.renderer.DragonflyRenderer;
import dev.xylonity.bonsai.clockwork.client.projectile.renderer.GenericProjectileRenderer;
import dev.xylonity.bonsai.clockwork.network.packets.ClockworkWingsC2SPacket;
import dev.xylonity.bonsai.clockwork.network.packets.DragonflyAscendKeyC2SPacket;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.knightlib.api.network.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
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
            EntityRenderers.register(ClockworkEntities.CLOCKWORK_WINGS_BOOST_PROJECTILE.get(), GenericProjectileRenderer::new);
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
            if (Minecraft.getInstance().player != null) {
                if (event.getKey() == GLFW.GLFW_KEY_SPACE && event.getAction() == GLFW.GLFW_PRESS) {
                    Network.sendToServer(new ClockworkWingsC2SPacket());
                }
            }

        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent e) {
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_SPACE)) {
                Network.sendToServer(new DragonflyAscendKeyC2SPacket(true));
            }

        }

    }


}