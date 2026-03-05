package dev.xylonity.bonsai.clockwork;

import dev.xylonity.bonsai.clockwork.client.ClientProxy;
import dev.xylonity.bonsai.clockwork.client.event.ClockworkClientEvents;
import dev.xylonity.bonsai.clockwork.registry.ClockworkPackets;
import dev.xylonity.bonsai.clockwork.registry.ClockworkPersistentSounds;
import dev.xylonity.knightlib.api.event.KnightLibEvents;
import net.fabricmc.api.ClientModInitializer;

public class ClockworkFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        Clockwork.PROXY = new ClientProxy();

        // Event registrar
        KnightLibEvents.CLIENT.register(ClockworkClientEvents.class);

        ClockworkPersistentSounds.init();

        ClockworkPackets.registerS2C();
    }

}
