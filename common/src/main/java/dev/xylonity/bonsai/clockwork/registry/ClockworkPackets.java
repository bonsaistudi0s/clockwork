package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.network.packets.c2s.ClockworkWingsC2SPacket;
import dev.xylonity.bonsai.clockwork.network.packets.c2s.DragonflyAscendKeyC2SPacket;
import dev.xylonity.bonsai.clockwork.network.packets.c2s.GenericSoundC2SPacket;
import dev.xylonity.bonsai.clockwork.network.packets.s2c.UpdatePotionSprayDirS2C;

public class ClockworkPackets {

    public static void registerAll() {
        registerS2C();
        registerC2S();
    }

    public static void registerS2C() {
        Clockwork.NETWORK.register(UpdatePotionSprayDirS2C.TYPE);
    }

    public static void registerC2S() {
        Clockwork.NETWORK.register(GenericSoundC2SPacket.TYPE);
        Clockwork.NETWORK.register(DragonflyAscendKeyC2SPacket.TYPE);
        Clockwork.NETWORK.register(ClockworkWingsC2SPacket.TYPE);
    }

}
