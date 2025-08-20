package dev.xylonity.bonsai.clockwork;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

public class ClockworkFabric implements ModInitializer, ClientModInitializer {
    
    @Override
    public void onInitialize() {
        Clockwork.init();
    }

    @Override
    public void onInitializeClient() {

    }

}
