package dev.xylonity.bonsai.clockwork.common.entity.projectile.trigger;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class FlamethrowerTriggerProjectile extends GenericTriggerProjectile {

    public FlamethrowerTriggerProjectile(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            final List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, new AABB(
                    getX() - 1, getY() - 0.5, getZ() - 1,
                    getX() + 1, getY() + 0.5, getZ() + 1
            ));

            // Ignites nearby living entities
            for (final LivingEntity entity : entities) {
                if (entity.equals(getOwner()) && tickCount < 10) {
                    continue;
                }

                entity.setSecondsOnFire(level().random.nextInt(7) + 1);
            }

            // Places fire on block contact and then discards
            if (onGround() || horizontalCollision) {
                final BlockPos blockPos = blockPosition();
                if (level().isEmptyBlock(blockPos)) {
                    level().setBlockAndUpdate(blockPos, BaseFireBlock.getState(level(), blockPos));
                }

                this.discard();
                return;
            }

        }

        // Straight movement
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    @Override
    protected int baseLifetime() {
        return 25;
    }

}
