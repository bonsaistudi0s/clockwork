package dev.xylonity.bonsai.clockwork.network.packets;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.trigger.PotionSprayTriggerProjectile;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.bonsai.clockwork.registry.ClockworkParticles;
import dev.xylonity.knightlib.api.util.KnightLibUtil;
import dev.xylonity.knightlib.network.PacketCodec;
import dev.xylonity.knightlib.network.PacketType;
import dev.xylonity.knightlib.network.ServerboundPacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public record PotionSprayerParticlesC2SPacket(double x, double y, double z, float vx, float vy, float vz, ItemStack potion) {

    private static final ResourceLocation ID = new ResourceLocation(Clockwork.MOD_ID, "potion_sprayer_particles_c2s");

    public static final ServerboundPacketType<PotionSprayerParticlesC2SPacket> TYPE =
            PacketType.serverbound(
                    ID,
                    PotionSprayerParticlesC2SPacket.class,
                    PacketCodec.of(PotionSprayerParticlesC2SPacket::encode, PotionSprayerParticlesC2SPacket::decode),
                    (message, player) -> {
                        if (player.level() instanceof ServerLevel level) {
                            //UpdatePotionSprayDirS2C pkt = new UpdatePotionSprayDirS2C(message.vx, message.vy, message.vz);
                            //Network.sendTo(player, UpdatePotionSprayDirS2C.TYPE.base(), pkt);
                            level.sendParticles(ClockworkParticles.POTION_SPRAY.get(), message.x, message.y, message.z, 10, 0, 0, 0, 0);
                            if (level.random.nextFloat() < 0.4) {
                                PotionSprayTriggerProjectile projectile = new PotionSprayTriggerProjectile(ClockworkEntities.POTION_SPRAY_TRIGGER_PROJECTILE.get(), level, message.potion);
                                projectile.setPos(message.x, message.y, message.z);
                                projectile.setDeltaMovement(KnightLibUtil.randomVectorInCone(new Vec3(message.vx, message.vy, message.vz), 40, new Random()).scale(0.65 * (0.8 + new Random().nextDouble() * 0.4)));
                                projectile.setOwner(player);
                                level.addFreshEntity(projectile);
                            }
                        }
                    }
            );

    private static void encode(PotionSprayerParticlesC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
        buf.writeFloat(packet.vx);
        buf.writeFloat(packet.vy);
        buf.writeFloat(packet.vz);
        buf.writeItemStack(packet.potion, true);
    }

    private static PotionSprayerParticlesC2SPacket decode(FriendlyByteBuf buf) {
        return new PotionSprayerParticlesC2SPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readItem());
    }

}
