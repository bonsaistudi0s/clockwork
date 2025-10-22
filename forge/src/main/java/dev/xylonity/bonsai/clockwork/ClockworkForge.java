package dev.xylonity.bonsai.clockwork;

import dev.xylonity.bonsai.clockwork.common.CommonProxy;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.proxy.IProxy;
import dev.xylonity.knightlib.config.ConfigComposer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(Clockwork.MOD_ID)
public class ClockworkForge {

    public ClockworkForge() {

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        if (FMLLoader.getDist().isClient()) {
            try {
                Class<?> cls = Class.forName("dev.xylonity.bonsai.clockwork.client.ClientProxy");
                Clockwork.PROXY = (IProxy) cls.getDeclaredConstructor().newInstance();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        else {
            Clockwork.PROXY = new CommonProxy();
        }

        ConfigComposer.registerConfig(ClockworkConfig.class, eventBus);

        Clockwork.init();
    }

}