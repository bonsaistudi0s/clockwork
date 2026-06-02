package dev.xylonity.bonsai.clockwork;

import dev.xylonity.bonsai.clockwork.client.ClientProxy;
import dev.xylonity.bonsai.clockwork.common.CommonProxy;
import dev.xylonity.bonsai.clockwork.common.event.ClockworkServerEvents;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.registry.ClockworkPackets;
import dev.xylonity.bonsai.clockwork.registry.ClockworkPersistentSounds;
import dev.xylonity.knightlib.api.config.ConfigComposer;
import dev.xylonity.knightlib.api.event.KnightLibEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Clockwork.MOD_ID)
public class ClockworkNeoForge {

    public ClockworkNeoForge(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        // Common package proxy registration
        Clockwork.PROXY = dist == Dist.CLIENT ? new ClientProxy() : new CommonProxy();

        // Config registrar
        ConfigComposer.registerConfig(Clockwork.MOD_ID, ClockworkConfig.class);

        // Registering all packets for both sides
        ClockworkPackets.registerAll();

        // Event registrar
        KnightLibEvents.SERVER.register(ClockworkServerEvents.class);
        Clockwork.PROXY.registerClientEvents();

        ClockworkPersistentSounds.init();

        // Common package differ
        Clockwork.init();
    }

}
