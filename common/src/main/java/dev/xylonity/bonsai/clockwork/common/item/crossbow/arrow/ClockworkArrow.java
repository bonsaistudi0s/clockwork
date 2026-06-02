package dev.xylonity.bonsai.clockwork.common.item.crossbow.arrow;

import dev.xylonity.bonsai.clockwork.common.entity.projectile.ClockworkArrowProjectile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClockworkArrow extends ArrowItem {

    public ClockworkArrow(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter, ItemStack weapon) {
        return new ClockworkArrowProjectile(level, shooter);
    }

}
