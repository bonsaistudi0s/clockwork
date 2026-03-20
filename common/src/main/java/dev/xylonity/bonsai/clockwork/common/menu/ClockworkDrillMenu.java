package dev.xylonity.bonsai.clockwork.common.menu;

import dev.xylonity.bonsai.clockwork.registry.ClockworkMenus;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ClockworkDrillMenu extends AbstractContainerMenu {

    private final Container drillInventory;
    private final ContainerData drillData;

    public ClockworkDrillMenu(int containerId, Inventory playerInventory, Container drillInventory, ContainerData containerData) {
        super(ClockworkMenus.DRILL_MENU.get(), containerId);
        this.drillInventory = drillInventory;
        this.drillData = containerData;
        addDataSlots(drillData);

        // Drill slots
        for (int i = 0; i < 4; i++) {
            addSlot(new Slot(drillInventory, i, 53 + i * 18, 18) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }

            });

        }

        // Inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 74 + row * 18));
            }

        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 132));
        }

    }

    public ClockworkDrillMenu(int containerId, Inventory playerInventory, Container drillInventory) {
        this(containerId, playerInventory, drillInventory, new SimpleContainerData(2));
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        final Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            final ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < 4) {
                if (!moveItemStackTo(stack, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }

            }
            else {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }
            else {
                slot.setChanged();
            }

        }

        return result;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return drillInventory.stillValid(player);
    }

    public int getBlocksMined() {
        return drillData.get(0);
    }

    public int getMaxBlocks() {
        return drillData.get(1);
    }

}