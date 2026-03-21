package dev.xylonity.bonsai.clockwork.network.packets.c2s;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.registry.ClockworkSounds;
import dev.xylonity.knightlib.network.PacketCodec;
import dev.xylonity.knightlib.network.PacketType;
import dev.xylonity.knightlib.network.ServerboundPacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

public record GenericSoundC2SPacket(int flag) {

    private static final ResourceLocation ID = Clockwork.resource("generic_sound_packet");

    public static final ServerboundPacketType<GenericSoundC2SPacket> TYPE =
            PacketType.serverbound(
                    ID,
                    GenericSoundC2SPacket.class,
                    PacketCodec.of(GenericSoundC2SPacket::encode, GenericSoundC2SPacket::decode),
                    (message, player) -> {
                        switch (message.flag) {
                            case 0 -> player.level().playSound(null, player.blockPosition(), ClockworkSounds.CLOCKWORK_WINGS_CLOSE.get(), SoundSource.MASTER, 1, 1);
                            case 1 -> player.level().playSound(null, player.blockPosition(), ClockworkSounds.CLOCKWORK_WINGS_OPEN.get(), SoundSource.MASTER, 1, 1);
                            case 2 -> player.level().playSound(null, player.blockPosition(), ClockworkSounds.CLOCKWORK_WINGS_FLAP.get(), SoundSource.MASTER, 1, 1);
                        }
                    }
            );

    private static void encode(GenericSoundC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.flag);
    }

    private static GenericSoundC2SPacket decode(FriendlyByteBuf buf) {
        return new GenericSoundC2SPacket(buf.readInt());
    }

}
