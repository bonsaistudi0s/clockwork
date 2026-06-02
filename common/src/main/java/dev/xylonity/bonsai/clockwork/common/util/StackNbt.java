package dev.xylonity.bonsai.clockwork.common.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Consumer;

public final class StackNbt {

    private StackNbt() {
        ;;
    }

    /**
     * Returns a copy of this stack's custom data tag
     */
    public static CompoundTag tag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    /**
     * Returns whether this stack has a custom data component holding the given key
     */
    public static boolean contains(ItemStack stack, String key) {
        final CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains(key);
    }

    /**
     * Returns whether this stack has any non-empty custom data
     */
    public static boolean has(ItemStack stack) {
        final CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    }

    public static void update(ItemStack stack, Consumer<CompoundTag> consumer) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, consumer);
    }

    public static void remove(ItemStack stack, String key) {
        if (contains(stack, key)) {
            update(stack, tag -> tag.remove(key));
        }

    }

}
