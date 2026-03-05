package dev.xylonity.bonsai.clockwork;

import dev.xylonity.bonsai.clockwork.common.CommonProxy;
import dev.xylonity.bonsai.clockwork.common.event.ClockworkServerEvents;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.registry.ClockworkPackets;
import dev.xylonity.knightlib.api.config.ConfigComposer;
import dev.xylonity.knightlib.api.event.KnightLibEvents;
import net.fabricmc.api.ModInitializer;

public class ClockworkFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        Clockwork.PROXY = new CommonProxy();

        // Config registrar
        ConfigComposer.registerConfig(Clockwork.MOD_ID, ClockworkConfig.class);

        // Event registrar
        KnightLibEvents.SERVER.register(ClockworkServerEvents.class);

        ClockworkPackets.registerC2S();

        Clockwork.init();
    }

}
