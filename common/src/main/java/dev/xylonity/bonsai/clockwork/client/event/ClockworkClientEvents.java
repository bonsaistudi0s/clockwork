package dev.xylonity.bonsai.clockwork.client.event;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.entity.renderer.BrokenDragonflyRenderer;
import dev.xylonity.bonsai.clockwork.client.entity.renderer.DragonflyRenderer;
import dev.xylonity.bonsai.clockwork.client.entity.renderer.ClockworkDrillRenderer;
import dev.xylonity.bonsai.clockwork.client.particle.FlamethrowerParticle;
import dev.xylonity.bonsai.clockwork.client.particle.PotionSprayParticle;
import dev.xylonity.bonsai.clockwork.client.projectile.renderer.ClockworkArrowProjectileRenderer;
import dev.xylonity.bonsai.clockwork.client.projectile.renderer.GenericProjectileRenderer;
import dev.xylonity.bonsai.clockwork.client.screen.ClockworkDrillScreen;
import dev.xylonity.bonsai.clockwork.common.item.wings.ClockworkWings;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.network.packets.c2s.ClockworkWingsFlapC2SPacket;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.bonsai.clockwork.registry.ClockworkMenus;
import dev.xylonity.bonsai.clockwork.registry.ClockworkParticles;
import dev.xylonity.knightlib.api.event.RegisterEvent;
import dev.xylonity.knightlib.api.event.impl.client.*;
import dev.xylonity.knightlib.api.event.impl.interop.TickPhase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ClockworkClientEvents {

    private static boolean wasJumpDown = false;
    private static int flapCooldown = 0;
    private static int glideStartTick = -1;
    private static boolean wasGliding = false;

    @RegisterEvent
    public static void registerEntityRenderers(final EntityRendererRegistrationEvent event) {
        event.register(ClockworkEntities.DRAGONFLY.get(), DragonflyRenderer::new);
        event.register(ClockworkEntities.BROKEN_DRAGONFLY.get(), BrokenDragonflyRenderer::new);

        event.register(ClockworkEntities.CLOCKWORK_WINGS_BOOST_PROJECTILE.get(), GenericProjectileRenderer::new);
        event.register(ClockworkEntities.POTION_SPRAY_TRIGGER_PROJECTILE.get(), GenericProjectileRenderer::new);
        event.register(ClockworkEntities.FLAMETHROWER_TRIGGER_PROJECTILE.get(), GenericProjectileRenderer::new);

        event.register(ClockworkEntities.CLOCKWORK_ARROW_PROJECTILE.get(), ClockworkArrowProjectileRenderer::new);

        event.register(ClockworkEntities.CLOCKWORK_DRILL.get(), ClockworkDrillRenderer::new);
    }

    @RegisterEvent
    public static void registerParticleProviders(final ParticleProviderRegistrationEvent event) {
        event.register(ClockworkParticles.POTION_SPRAY.get(), PotionSprayParticle.Provider::new);
        event.register(ClockworkParticles.FLAMETHROWER_PARTICLE.get(), FlamethrowerParticle.Provider::new);
    }

    @RegisterEvent
    public static void registerMenuScreens(final MenuScreenRegistrationEvent event) {
        event.register(ClockworkMenus.DRILL_MENU.get(), ClockworkDrillScreen::new);
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

        event.register(new ModelResourceLocation(Clockwork.MOD_ID, "clockwork_flamethrower_2d", "inventory"));
    }

    @RegisterEvent
    public static void onKeyInput(final ClientKeyInputEvent event) {

    }

    @RegisterEvent
    public static void onClientTick(final ClientPlayerTickEvent event) {
        if (event.getPhase() == TickPhase.END) {
            if (event.getClient().level == null) {
                return;
            }

            final boolean gliding = event.getPlayer().isFallFlying();
            if (gliding && !wasGliding) {
                glideStartTick = event.getPlayer().tickCount;
            }
            wasGliding = gliding;

            final boolean jumpDown = event.getClient().options.keyJump.isDown();
            final boolean justPressed = jumpDown && !wasJumpDown;
            wasJumpDown = jumpDown;

            if (flapCooldown > 0) {
                flapCooldown--;
            }

            if (!justPressed || flapCooldown > 0) {
                return;
            }

            final ItemStack chest = event.getPlayer().getItemBySlot(EquipmentSlot.CHEST);
            if (!(chest.getItem() instanceof ClockworkWings)) {
                return;
            }

            final boolean isFalling = !event.getPlayer().onGround() && !gliding && event.getPlayer().getDeltaMovement().y < -0.5;

            if (!gliding && !isFalling) {
                return;
            }

            if (gliding && glideStartTick >= 0 && event.getPlayer().tickCount - glideStartTick < 10) {
                return;
            }

            flapCooldown = ClockworkConfig.CLOCKWORK_WINGS_BOOST_COOLDOWN_TICKS;
            chest.getOrCreateTag().putInt("FlapTick", event.getPlayer().tickCount);
            Clockwork.NETWORK.sendToServer(new ClockworkWingsFlapC2SPacket());
        }

    }

}
