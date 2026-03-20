package dev.xylonity.bonsai.clockwork;

import dev.xylonity.bonsai.clockwork.platform.ClockworkPlatform;
import dev.xylonity.bonsai.clockwork.proxy.IProxy;
import dev.xylonity.bonsai.clockwork.registry.*;
import dev.xylonity.knightlib.api.network.Network;
import dev.xylonity.knightlib.api.network.NetworkEndpoint;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class Clockwork {

    public static final String MOD_ID = "clockwork";
    public static final Logger LOGGER = LoggerFactory.getLogger("Clockwork");

    public static IProxy PROXY;

    public static final ClockworkPlatform PLATFORM = ServiceLoader.load(ClockworkPlatform.class).findFirst().orElseThrow();
    public static final NetworkEndpoint NETWORK = Network.endpoint(MOD_ID);

    public static void init() {
        ClockworkItems.ITEMS.init();
        ClockworkEntities.ENTITIES.init();
        ClockworkSounds.SOUNDS.init();
        ClockworkCreativeTabs.CREATIVE_MODE_TABS.init();
        ClockworkParticles.PARTICLES.init();
        ClockworkMenus.MENUS.init();

        ClockworkEntitySpawns.init();
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

}