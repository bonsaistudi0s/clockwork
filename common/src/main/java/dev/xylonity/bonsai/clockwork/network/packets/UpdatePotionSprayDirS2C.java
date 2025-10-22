package dev.xylonity.bonsai.clockwork.network.packets;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.knightlib.network.ClientboundPacketType;
import dev.xylonity.knightlib.network.PacketCodec;
import dev.xylonity.knightlib.network.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record UpdatePotionSprayDirS2C(float x, float y, float z) {

    private static final ResourceLocation ID = new ResourceLocation(Clockwork.MOD_ID, "update_potion_spray_dir_s2c");

    public static final ClientboundPacketType<UpdatePotionSprayDirS2C> TYPE =
            PacketType.clientbound(
                    ID,
                    UpdatePotionSprayDirS2C.class,
                    PacketCodec.of(UpdatePotionSprayDirS2C::encode, UpdatePotionSprayDirS2C::decode),
                    message -> {
                        //PotionSprayParticle.setDefaultVelocity(message.x, message.y, message.z);
                    }
            );

    private static void encode(UpdatePotionSprayDirS2C packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.x);
        buf.writeFloat(packet.y);
        buf.writeFloat(packet.z);
    }

    private static UpdatePotionSprayDirS2C decode(FriendlyByteBuf buf) {
        return new UpdatePotionSprayDirS2C(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

}
