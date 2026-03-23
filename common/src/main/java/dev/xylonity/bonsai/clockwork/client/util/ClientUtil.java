package dev.xylonity.bonsai.clockwork.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ClientUtil {

    public static ItemStack getCurrentStack() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            final ItemStack mainHand = minecraft.player.getMainHandItem();
            if (mainHand.getItem() instanceof CrossbowItem) {
                return mainHand;
            }

            final ItemStack offHand = minecraft.player.getOffhandItem();
            if (offHand.getItem() instanceof CrossbowItem) {
                return offHand;
            }

        }

        return null;
    }

    public static void applyStaticTransform(ItemDisplayContext context, PoseStack pose) {
        switch (context) {
            case GUI -> pose.translate(0.5, 0.5, 0);
            case GROUND -> pose.translate(0.5, 0.5, 0.5);
            default -> { // FIXED
                pose.translate(0.5, 0.5, 0);
                pose.scale(0.75f, 0.75f, 0.75f);
            }

        }

    }

    public static boolean isStaticContext(ItemDisplayContext context) {
        return context == ItemDisplayContext.GUI || context == ItemDisplayContext.GROUND || context == ItemDisplayContext.FIXED;
    }

    public static float exponentialDecay(float current, float target, float factor) {
        return current + (target - current) * factor;
    }

    public static boolean isActivelyPulling(ItemStack stack, LocalPlayer player) {
        return player != null && player.isUsingItem() && player.getMainHandItem() == stack && !CrossbowItem.isCharged(stack);
    }

}
