package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.knightlib.api.registrar.ResourceDispatcher;
import dev.xylonity.knightlib.api.registrar.ResourceRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ClockworkCreativeTabs {

    public static final ResourceRegistry<CreativeModeTab> CREATIVE_MODE_TABS = ResourceDispatcher.create(BuiltInRegistries.CREATIVE_MODE_TAB, Clockwork.MOD_ID);

    public static final Supplier<CreativeModeTab> CLOCKWORK_TAB =
            CREATIVE_MODE_TABS.register("clockwork_creative_tab",
                    () -> Clockwork.PLATFORM.creativeTabBuilder()
                            .icon(() -> new ItemStack(ClockworkItems.CLOCKWORK_GEAR.get()))
                            .title(Component.translatable("creativetab.clockwork.title"))
                            .displayItems((display, output) -> {
                                output.accept(ClockworkItems.CLOCKWORK_GEAR.get());
                                output.accept(ClockworkItems.CROSSBOW_BARREL.get());
                                output.accept(ClockworkItems.CLOCKWORK_ARROW.get());
                                output.accept(ClockworkItems.CLOCKWORK_WINGS.get());
                                output.accept(ClockworkItems.BARREL_CROSSBOW.get());
                                output.accept(ClockworkItems.SCOPE_CROSSBOW.get());
                                output.accept(ClockworkItems.CLOCKWORK_POTION_SPRAYER.get());
                            })
                            .build());

}
