package dev.xylonity.bonsai.clockwork.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

public class ClientUtil {

    public static ItemStack getCurrentStack() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            ItemStack mainHand = minecraft.player.getMainHandItem();
            if (mainHand.getItem() instanceof CrossbowItem) {
                return mainHand;
            }

            ItemStack offHand = minecraft.player.getOffhandItem();
            if (offHand.getItem() instanceof CrossbowItem) {
                return offHand;
            }
        }

        return null;
    }

}
