package dev.xylonity.bonsai.clockwork.client.screen;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.common.menu.ClockworkDrillMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class ClockworkDrillScreen extends AbstractContainerScreen<ClockworkDrillMenu> {

    private static final ResourceLocation TEXTURE = Clockwork.resource("textures/gui/clockwork_drill_ui.png");

    public ClockworkDrillScreen(ClockworkDrillMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 156;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        // Durability bar
        final int blocksMined = menu.getBlocksMined();
        final int maxBlockCount = menu.getMaxBlocks();
        final int barWidth = maxBlockCount > 0 ? 100 - (blocksMined * 100 / maxBlockCount) : 100;
        graphics.blit(TEXTURE, leftPos + 38, topPos + 51, 0, 156, barWidth, 6, 256, 256);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
    }

}