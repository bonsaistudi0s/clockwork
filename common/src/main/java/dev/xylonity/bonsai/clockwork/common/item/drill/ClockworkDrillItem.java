package dev.xylonity.bonsai.clockwork.common.item.drill;

import dev.xylonity.bonsai.clockwork.common.entity.tool.ClockworkDrillEntity;
import dev.xylonity.bonsai.clockwork.common.util.StackNbt;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ClockworkDrillItem extends Item {

    public ClockworkDrillItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getBlocksMined(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getRemainingDurability(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(getRemainingDurability(stack) / 3.0f, 1.0f, 1.0f);
    }

    private static float getRemainingDurability(ItemStack stack) {
        return 1.0f - getBlocksMined(stack) / (float) ClockworkConfig.DRILL_BLOCKS_UNTIL_BROKEN;
    }

    private static int getBlocksMined(ItemStack stack) {
        return Mth.clamp(StackNbt.tag(stack).getInt("BlocksMined"), 0, ClockworkConfig.DRILL_BLOCKS_UNTIL_BROKEN);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {

        final Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        final Player player = context.getPlayer();
        if (player != null) {
            final ItemStack stack = context.getItemInHand();
            final CompoundTag itemTag = StackNbt.has(stack) ? StackNbt.tag(stack) : null;

            final boolean hasSpawned = ClockworkDrillEntity.create(level, context.getClickedPos(), player, itemTag);
            if (hasSpawned) {
                if (!player.getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }

                return InteractionResult.SUCCESS;
            }

        }

        return InteractionResult.PASS;
    }

}
