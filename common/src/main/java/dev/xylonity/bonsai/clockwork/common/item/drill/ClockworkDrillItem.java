package dev.xylonity.bonsai.clockwork.common.item.drill;

import dev.xylonity.bonsai.clockwork.common.entity.tool.ClockworkDrillEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ClockworkDrillItem extends Item {

    public ClockworkDrillItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {

        final Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        final Player player = context.getPlayer();
        if (player != null) {
            final boolean hasSpawned = ClockworkDrillEntity.create(level, context.getClickedPos(), player);
            if (hasSpawned) {
                if (!player.getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }

            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

}