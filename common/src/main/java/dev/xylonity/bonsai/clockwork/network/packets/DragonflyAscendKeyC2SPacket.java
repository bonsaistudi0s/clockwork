package dev.xylonity.bonsai.clockwork.network.packets;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.passive.DragonflyEntity;
import dev.xylonity.knightlib.network.PacketCodec;
import dev.xylonity.knightlib.network.PacketType;
import dev.xylonity.knightlib.network.ServerboundPacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record DragonflyAscendKeyC2SPacket(boolean ascending) {

    private static final ResourceLocation ID = new ResourceLocation(Clockwork.MOD_ID, "dragonfly_ascend_key");

    public static final ServerboundPacketType<DragonflyAscendKeyC2SPacket> TYPE =
            PacketType.serverbound(
                    ID,
                    DragonflyAscendKeyC2SPacket.class,
                    PacketCodec.of(DragonflyAscendKeyC2SPacket::encode, DragonflyAscendKeyC2SPacket::decode),
                    (msg, player) -> {
                        if (player.getVehicle() instanceof DragonflyEntity e && e.getControllingPassenger() == player) {
                            if (e.getState() != 1) e.setState(1);
                            e.setAscending(msg.ascending);
                        }
                    }
            );

    private static void encode(DragonflyAscendKeyC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.ascending());
    }

    private static DragonflyAscendKeyC2SPacket decode(FriendlyByteBuf buf) {
        return new DragonflyAscendKeyC2SPacket(buf.readBoolean());
    }
}