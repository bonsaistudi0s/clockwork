package dev.xylonity.bonsai.clockwork.mixin;

import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(CrossbowItem.class)
public interface CrossbowItemAccessor {

    @Invoker("getChargedProjectiles")
    static List<ItemStack> clockwork$getChargedProjectiles(ItemStack crossbowStack) {
        throw new AssertionError();
    }

}