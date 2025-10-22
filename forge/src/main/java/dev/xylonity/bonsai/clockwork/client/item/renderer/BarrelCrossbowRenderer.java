package dev.xylonity.bonsai.clockwork.client.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.item.model.BarrelCrossbowModel;
import dev.xylonity.bonsai.clockwork.client.util.ClientSwayUtil;
import dev.xylonity.bonsai.clockwork.common.item.BarrelCrossbow;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.CrossbowItem;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BarrelCrossbowRenderer extends GeoItemRenderer<BarrelCrossbow> {

    private ItemDisplayContext lastTransform = ItemDisplayContext.NONE;
    private final ClientSwayUtil swayUtil;

    public BarrelCrossbowRenderer() {
        super(new BarrelCrossbowModel());
        this.swayUtil = new ClientSwayUtil();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buf, int light, int overlay) {

        this.lastTransform = ctx;

        if (ctx == ItemDisplayContext.GUI || ctx == ItemDisplayContext.GROUND || ctx == ItemDisplayContext.FIXED) {
            Minecraft minecraft = Minecraft.getInstance();

            pose.pushPose();

            if (ctx == ItemDisplayContext.GUI) {
                pose.translate(0.5, 0.5, 0);
            }
            else if (ctx == ItemDisplayContext.GROUND) {
                pose.translate(0.5, 0.5, 0.5);
            }
            else {
                pose.translate(0.5, 0.5, 0);
                pose.scale(0.75f, 0.75f, 0.75f);
            }

            ModelResourceLocation modelRl = new ModelResourceLocation(Clockwork.MOD_ID, getModelForState(stack, minecraft.player), "inventory");
            BakedModel model = minecraft.getModelManager().getModel(modelRl);

            if (model == minecraft.getModelManager().getMissingModel()) {
                modelRl = new ModelResourceLocation(Clockwork.MOD_ID, "barrel_crossbow_standby", "inventory");
                model = minecraft.getModelManager().getModel(modelRl);
            }

            minecraft.getItemRenderer().render(stack, ctx, false, pose, buf, light, overlay, model);
            pose.popPose();
            return;
        }

        super.renderByItem(stack, ctx, pose, buf, light, overlay);
    }

    private String getModelForState(ItemStack stack, LocalPlayer player) {
        if (CrossbowItem.isCharged(stack)) {
            if (CrossbowItem.containsChargedProjectile(stack, Items.FIREWORK_ROCKET)) {
                return "barrel_crossbow_firework";
            }
            else if (CrossbowItem.containsChargedProjectile(stack, ClockworkItems.CLOCKWORK_ARROW.get())) {
                return "barrel_crossbow_clockworkarrow";
            }

            return "barrel_crossbow_arrow";
        }

        if (player != null && player.isUsingItem() && player.getMainHandItem() == stack && !CrossbowItem.isCharged(stack)) {

            float pullProgress = getPullProgress(stack, player);
            if (pullProgress >= 1.0f) {
                return "barrel_crossbow_pulling_2";
            }
            else if (pullProgress >= 0.58f) {
                return "barrel_crossbow_pulling_1";
            }
            else {
                return "barrel_crossbow_pulling_0";
            }

        }

        return "barrel_crossbow_standby";
    }

    private float getPullProgress(ItemStack stack, LocalPlayer player) {
        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains(BarrelCrossbow.NBT_LOAD_START_TICK)) {
            return 0.0f;
        }

        long startTick = tag.getLong(BarrelCrossbow.NBT_LOAD_START_TICK);
        long currentTick = player.level().getGameTime();
        int chargeDuration = BarrelCrossbow.getChargeDurationTicks(stack);

        if (chargeDuration <= 0) {
            return 0.0f;
        }

        long elapsed = currentTick - startTick;
        float progress = (float) elapsed / (float) chargeDuration;

        return Math.min(Math.max(progress, 0.0f), 1.0f);
    }

    @Override
    public void preRender(PoseStack poseStack, BarrelCrossbow animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        swayUtil.applyFirstPersonSway(poseStack, lastTransform, partialTick);
    }

}
