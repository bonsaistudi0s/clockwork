package dev.xylonity.bonsai.clockwork;

import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.network.packets.ClockworkWingsC2SPacket;
import dev.xylonity.bonsai.clockwork.registry.ClockworkPackets;
import dev.xylonity.knightlib.api.network.Network;
import dev.xylonity.knightlib.config.ConfigComposer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Clockwork.MOD_ID)
public class ClockworkForge {

    public ClockworkForge() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ConfigComposer.registerConfig(ClockworkConfig.class, eventBus);

        ClockworkPackets.register();
        Network.register(ClockworkWingsC2SPacket.TYPE);

        Clockwork.init();
    }

}