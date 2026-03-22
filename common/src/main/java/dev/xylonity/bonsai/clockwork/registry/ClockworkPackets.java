package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.network.packets.c2s.ClockworkWingsFlapC2SPacket;
import dev.xylonity.bonsai.clockwork.network.packets.c2s.DragonflyAscendKeyC2SPacket;
import dev.xylonity.bonsai.clockwork.network.packets.c2s.ClockworkWingsSoundC2SPacket;

public class ClockworkPackets {

    public static void registerAll() {
        registerS2C();
        registerC2S();
    }

    public static void registerS2C() {
        ;;
    }

    public static void registerC2S() {
        Clockwork.NETWORK.register(ClockworkWingsSoundC2SPacket.TYPE);
        Clockwork.NETWORK.register(DragonflyAscendKeyC2SPacket.TYPE);
        Clockwork.NETWORK.register(ClockworkWingsFlapC2SPacket.TYPE);
    }

}
