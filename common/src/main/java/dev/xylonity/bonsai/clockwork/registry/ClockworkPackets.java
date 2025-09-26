package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.network.packets.DragonflyAscendKeyC2SPacket;
import dev.xylonity.bonsai.clockwork.network.packets.GenericSoundC2SPacket;
import dev.xylonity.knightlib.api.network.Network;

public class ClockworkPackets {

    public static void register() {
        registerC2S();
        registerS2C();
    }

    public static void registerS2C() {

    }

    public static void registerC2S() {
        Network.register(DragonflyAscendKeyC2SPacket.TYPE);
        Network.register(GenericSoundC2SPacket.TYPE);
    }

}
