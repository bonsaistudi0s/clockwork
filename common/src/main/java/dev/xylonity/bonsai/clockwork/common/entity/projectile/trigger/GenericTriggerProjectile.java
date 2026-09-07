package dev.xylonity.bonsai.clockwork.common.entity.projectile.trigger;

import dev.xylonity.bonsai.clockwork.common.entity.projectile.GenericProjectile;
import dev.xylonity.knightlib.common.entity.AbstractProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class GenericTriggerProjectile extends GenericProjectile {

    public GenericTriggerProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void playerTouch(@NotNull Player pEntity) {
        ;;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        ;;
    }

    @Override
    protected int baseLifetime() {
        return 100;
    }

}