package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.entity.tool.ClockworkDrillEntity;
import dev.xylonity.bonsai.clockwork.common.menu.ClockworkDrillMenu;
import dev.xylonity.knightlib.api.registrar.ResourceDispatcher;
import dev.xylonity.knightlib.api.registrar.ResourceEntry;
import dev.xylonity.knightlib.api.registrar.ResourceRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.MenuType;

public class ClockworkMenus {

    public static final ResourceRegistry<MenuType<?>> MENUS = ResourceDispatcher.create(BuiltInRegistries.MENU, Clockwork.MOD_ID);

    public static final ResourceEntry<MenuType<ClockworkDrillMenu>> DRILL_MENU = MENUS.registerMenu("clockwork_drill", (syncId, playerInv, buf) -> {
        final int entityId = buf.readInt();
        final Entity entity = playerInv.player.level().getEntity(entityId);
        if (entity instanceof ClockworkDrillEntity drill) {
            return new ClockworkDrillMenu(syncId, playerInv, drill);
        }

        return new ClockworkDrillMenu(syncId, playerInv, new SimpleContainer(4));
    });

}
