package dev.xylonity.bonsai.clockwork.network.packets.c2s;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.registry.ClockworkSounds;
import dev.xylonity.knightlib.network.PacketCodec;
import dev.xylonity.knightlib.network.PacketType;
import dev.xylonity.knightlib.network.ServerboundPacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

public record ClockworkWingsSoundC2SPacket(int flag) {

    private static final ResourceLocation ID = Clockwork.resource("generic_sound_packet");

    public static final ServerboundPacketType<ClockworkWingsSoundC2SPacket> TYPE =
            PacketType.serverbound(
                    ID,
                    ClockworkWingsSoundC2SPacket.class,
                    PacketCodec.of(ClockworkWingsSoundC2SPacket::encode, ClockworkWingsSoundC2SPacket::decode),
                    (message, player) -> {
                        switch (message.flag) {
                            case 0 -> player.level().playSound(null, player.blockPosition(), ClockworkSounds.CLOCKWORK_WINGS_CLOSE.get(), SoundSource.MASTER, 1, 1);
                            case 1 -> player.level().playSound(null, player.blockPosition(), ClockworkSounds.CLOCKWORK_WINGS_OPEN.get(), SoundSource.MASTER, 1, 1);
                        }
                    }
            );

    private static void encode(ClockworkWingsSoundC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.flag);
    }

    private static ClockworkWingsSoundC2SPacket decode(FriendlyByteBuf buf) {
        return new ClockworkWingsSoundC2SPacket(buf.readInt());
    }

}
