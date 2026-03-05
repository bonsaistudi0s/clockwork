package dev.xylonity.bonsai.clockwork;

import dev.xylonity.bonsai.clockwork.client.ClientProxy;
import dev.xylonity.bonsai.clockwork.common.CommonProxy;
import dev.xylonity.bonsai.clockwork.common.event.ClockworkServerEvents;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.registry.ClockworkPackets;
import dev.xylonity.bonsai.clockwork.registry.ClockworkPersistentSounds;
import dev.xylonity.knightlib.api.config.ConfigComposer;
import dev.xylonity.knightlib.api.event.KnightLibEvents;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Clockwork.MOD_ID)
public class ClockworkForge {

    public ClockworkForge() {
        // Common package proxy registration
        Clockwork.PROXY = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

        final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

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