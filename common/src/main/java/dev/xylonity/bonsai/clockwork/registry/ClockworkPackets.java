package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.network.packets.DragonflyAscendKeyC2SPacket;
import dev.xylonity.bonsai.clockwork.network.packets.GenericSoundC2SPacket;

public class ClockworkPackets {

    public static void registerAll() {
        registerS2C();
        registerC2S();
    }

    public static void registerS2C() {

    }

    public static void registerC2S() {
        Clockwork.NETWORK.register(GenericSoundC2SPacket.TYPE);
        Clockwork.NETWORK.register(DragonflyAscendKeyC2SPacket.TYPE);
    }

}
