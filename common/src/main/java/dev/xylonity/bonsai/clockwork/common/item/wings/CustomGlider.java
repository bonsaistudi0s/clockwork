package dev.xylonity.bonsai.clockwork.common.item.wings;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

public interface CustomGlider {

    boolean canGlide(ItemStack stack, LivingEntity entity);

    default boolean onGlideTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (!entity.level().isClientSide) {
            int next = flightTicks + 1;
            if (next % 10 == 0) {
                if (next % 20 == 0) {
                    stack.hurtAndBreak(1, entity,
                            livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.CHEST));
                }

                entity.gameEvent(GameEvent.ELYTRA_GLIDE);
            }

        }

        return true;
    }

}