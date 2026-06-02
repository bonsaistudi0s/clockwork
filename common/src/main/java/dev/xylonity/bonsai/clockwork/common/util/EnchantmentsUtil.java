package dev.xylonity.bonsai.clockwork.common.util;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class EnchantmentsUtil {

    private EnchantmentsUtil() {
        ;;
    }

    public static int level(ItemStack stack, ResourceKey<Enchantment> key) {
        final ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (final Holder<Enchantment> holder : enchantments.keySet()) {
            if (holder.is(key)) {
                return enchantments.getLevel(holder);
            }

        }

        return 0;
    }

}
