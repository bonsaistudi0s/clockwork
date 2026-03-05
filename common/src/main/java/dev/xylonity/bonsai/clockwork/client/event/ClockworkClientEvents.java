package dev.xylonity.bonsai.clockwork.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.entity.renderer.BrokenDragonflyRenderer;
import dev.xylonity.bonsai.clockwork.client.entity.renderer.DragonflyRenderer;
import dev.xylonity.bonsai.clockwork.client.particle.PotionSprayParticle;
import dev.xylonity.bonsai.clockwork.client.projectile.renderer.ClockworkArrowProjectileRenderer;
import dev.xylonity.bonsai.clockwork.client.projectile.renderer.GenericProjectileRenderer;
import dev.xylonity.bonsai.clockwork.common.entity.passive.DragonflyEntity;
import dev.xylonity.bonsai.clockwork.common.item.wings.ClockworkWings;
import dev.xylonity.bonsai.clockwork.network.packets.c2s.ClockworkWingsC2SPacket;
import dev.xylonity.bonsai.clockwork.network.packets.c2s.DragonflyAscendKeyC2SPacket;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.bonsai.clockwork.registry.ClockworkParticles;
import dev.xylonity.knightlib.api.event.RegisterEvent;
import dev.xylonity.knightlib.api.event.impl.client.*;
import dev.xylonity.knightlib.api.event.impl.interop.TickPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class ClockworkClientEvents {

    @RegisterEvent
    public static void registerEntityRenderers(final EntityRendererRegistrationEvent event) {
        event.register(ClockworkEntities.DRAGONFLY.get(), DragonflyRenderer::new);
        event.register(ClockworkEntities.BROKEN_DRAGONFLY.get(), BrokenDragonflyRenderer::new);

        event.register(ClockworkEntities.CLOCKWORK_WINGS_BOOST_PROJECTILE.get(), GenericProjectileRenderer::new);
        event.register(ClockworkEntities.POTION_SPRAY_TRIGGER_PROJECTILE.get(), GenericProjectileRenderer::new);

        event.register(ClockworkEntities.CLOCKWORK_ARROW_PROJECTILE.get(), ClockworkArrowProjectileRenderer::new);
    }

    @RegisterEvent
    public static void registerParticleProviders(final ParticleProviderRegistrationEvent event) {
        event.register(ClockworkParticles.POTION_SPRAY.get(), PotionSprayParticle.Provider::new);
    }

    @RegisterEvent
    public static void registerAdditionalModels(final AdditionalModelsRegistrationEvent event) {
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

    @RegisterEvent
    public static void onKeyInput(final ClientKeyInputEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (event.getKey() == GLFW.GLFW_KEY_SPACE && event.getAction() == GLFW.GLFW_PRESS) {
                if (Minecraft.getInstance().screen instanceof ChatScreen) return;
                if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ClockworkWings) {
                    Clockwork.NETWORK.sendToServer(new ClockworkWingsC2SPacket());
                }
            }
        }

    }

    @RegisterEvent
    public static void onClientTick(final ClientTickEvent event) {
        if (event.getPhase() == TickPhase.END) {
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_SPACE)) {
                if (Minecraft.getInstance().screen instanceof ChatScreen) return;
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    if (player.getVehicle() instanceof DragonflyEntity e && e.getControllingPassenger() == player) {
                        Clockwork.NETWORK.sendToServer(new DragonflyAscendKeyC2SPacket(true));
                    }
                }
            }

        }
    }

}
