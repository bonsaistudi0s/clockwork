package dev.xylonity.bonsai.clockwork.network.packets.c2s;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.ClockworkWingsBoostProjectile;
import dev.xylonity.bonsai.clockwork.common.item.wings.ClockworkWings;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.knightlib.network.PacketCodec;
import dev.xylonity.knightlib.network.PacketType;
import dev.xylonity.knightlib.network.ServerboundPacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;

public record ClockworkWingsFlapC2SPacket() {

    private static final ResourceLocation ID = Clockwork.resource("clockwork_wings_boost");

    public static final ServerboundPacketType<ClockworkWingsFlapC2SPacket> TYPE =
            PacketType.serverbound(
                    ID,
                    ClockworkWingsFlapC2SPacket.class,
                    PacketCodec.of(ClockworkWingsFlapC2SPacket::encode, ClockworkWingsFlapC2SPacket::decode),
                    (message, player) -> {
                        final ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                        if (!(chest.getItem() instanceof ClockworkWings wings)) {
                            return;
                        }

                        if (!player.isFallFlying()) {
                            return;
                        }

                        ClockworkWings.getAnimState(player.getId()).flapTick = player.tickCount;

                        final Level level = player.level();
                        final ClockworkWingsBoostProjectile boostProjectile = ClockworkEntities.CLOCKWORK_WINGS_BOOST_PROJECTILE.get().create(level);
                        if (boostProjectile != null) {
                            boostProjectile.setAttachedEntityUUID(player.getUUID());
                            level.addFreshEntity(boostProjectile);
                        }

                        chest.hurtAndBreak(5, player,
                                livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.CHEST));
                    }

            );

    private static void encode(ClockworkWingsFlapC2SPacket packet, FriendlyByteBuf buf) {
        ;;
    }

    private static ClockworkWingsFlapC2SPacket decode(FriendlyByteBuf buf) {
        return new ClockworkWingsFlapC2SPacket();
    }

}
