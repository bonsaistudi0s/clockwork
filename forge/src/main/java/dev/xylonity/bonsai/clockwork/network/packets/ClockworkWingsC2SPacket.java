package dev.xylonity.bonsai.clockwork.network.packets;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.item.ClockworkWings;
import dev.xylonity.knightlib.network.PacketCodec;
import dev.xylonity.knightlib.network.PacketType;
import dev.xylonity.knightlib.network.ServerboundPacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

public record ClockworkWingsC2SPacket() {

    private static final ResourceLocation ID = new ResourceLocation(Clockwork.MOD_ID, "clockwork_wings_boost");

    public static final ServerboundPacketType<ClockworkWingsC2SPacket> TYPE =
            PacketType.serverbound(
                    ID,
                    ClockworkWingsC2SPacket.class,
                    PacketCodec.of(ClockworkWingsC2SPacket::encode, ClockworkWingsC2SPacket::decode),
                    (message, player) -> {
                        if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ClockworkWings) {
                            ClockworkWings.spawnBoostEntity(player.level(), player);
                        }
                    }
            );

    private static void encode(ClockworkWingsC2SPacket packet, FriendlyByteBuf buf) {
        ;;
    }

    private static ClockworkWingsC2SPacket decode(FriendlyByteBuf buf) {
        return new ClockworkWingsC2SPacket();
    }

}
