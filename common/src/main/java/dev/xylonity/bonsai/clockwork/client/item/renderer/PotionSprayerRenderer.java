package dev.xylonity.bonsai.clockwork.client.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.item.model.PotionSprayerModel;
import dev.xylonity.bonsai.clockwork.client.item.layer.PotionSprayerLiquidTintLayer;
import dev.xylonity.bonsai.clockwork.client.util.ClientSwayUtil;
import dev.xylonity.bonsai.clockwork.client.util.ClientUtil;
import dev.xylonity.bonsai.clockwork.common.item.sprayer.PotionSprayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PotionSprayerRenderer extends GeoItemRenderer<PotionSprayer> {

    private static final String MODEL_2D = "clockwork_potion_sprayer_2d";
    public static final ResourceLocation LIQUID_TEXTURE = Clockwork.resource("textures/item/clockwork_potion_sprayer_liquid.png");

    private static final String BONE_LIQUID_1 = "liquid_1";
    private static final String BONE_LIQUID_2 = "liquid_2";

    private static final float LIQUID_1_PORTION = 0.8f;
    private static final float LIQUID_2_PORTION = 0.2f;

    private static final float LIQUID_1_HALF_HEIGHT = 0f;
    private static final float LIQUID_2_HALF_HEIGHT = 0f;

    private static final float FILL_SMOOTH_FACTOR = 0.12f;
    private static final float HIDE_THRESHOLD = 0.005f;

    private ItemDisplayContext lastTransform = ItemDisplayContext.NONE;
    private final ClientSwayUtil swayUtil;

    private float smoothFill = 0f;
    private int potionColor = 0xFFFFFF;

    public PotionSprayerRenderer() {
        super(new PotionSprayerModel());
        this.swayUtil = new ClientSwayUtil();
        addRenderLayer(new PotionSprayerLiquidTintLayer(this));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buf, int light, int overlay) {
        lastTransform = context;

        if (ClientUtil.isStaticContext(context)) {
            renderStaticModel(stack, context, pose, buf, light, overlay);
            return;
        }

        super.renderByItem(stack, context, pose, buf, light, overlay);
    }

    @Override
    public void preRender(PoseStack poseStack, PotionSprayer animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        swayUtil.applyFirstPersonSway(poseStack, lastTransform, partialTick);
        updateLiquidBones(model);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, PotionSprayer animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (isLiquidBone(bone) && !bone.isHidden()) {
            final float r = ((potionColor >> 16) & 0xFF) / 255f;
            final float g = ((potionColor >> 8) & 0xFF) / 255f;
            final float b = (potionColor & 0xFF) / 255f;

            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, r, g, b, alpha);

            return;
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private void updateLiquidBones(BakedGeoModel model) {
        final float targetFill = computeTargetFill();
        smoothFill = ClientUtil.exponentialDecay(smoothFill, targetFill, FILL_SMOOTH_FACTOR);

        if (smoothFill < HIDE_THRESHOLD) {
            smoothFill = 0f;
        }
        if (smoothFill > 1f - HIDE_THRESHOLD) {
            smoothFill = 1f;
        }

        final float fill1 = Mth.clamp((smoothFill - LIQUID_2_PORTION) / LIQUID_1_PORTION, 0f, 1f);
        final float fill2 = Mth.clamp(smoothFill / LIQUID_2_PORTION, 0f, 1f);

        final LocalPlayer player = Minecraft.getInstance().player;
        final PotionSprayer.PotionSlot slot = (player != null) ? PotionSprayer.findFirstPotion(player) : null;
        final boolean hasPotion = slot != null;

        potionColor = hasPotion ? PotionUtils.getColor(slot.stack()) : 0xFFFFFF;

        applyFillToBone(model, BONE_LIQUID_1, hasPotion ? fill1 : 0f, LIQUID_1_HALF_HEIGHT);
        applyFillToBone(model, BONE_LIQUID_2, hasPotion ? fill2 : 0f, LIQUID_2_HALF_HEIGHT);
    }

    private void applyFillToBone(BakedGeoModel model, String boneName, float fill, float halfHeight) {
        model.getBone(boneName).ifPresent(bone -> {
            if (fill <= HIDE_THRESHOLD) {
                bone.setHidden(true);
                bone.setScaleY(0f);
                return;
            }

            bone.setHidden(false);
            bone.setScaleY(fill);

            if (halfHeight > 0f) {
                bone.setPosY(bone.getPosY() - (1f - fill) * halfHeight);
            }

        });

    }

    private float computeTargetFill() {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 0f;
        }

        final PotionSprayer.PotionSlot slot = PotionSprayer.findFirstPotion(player);
        if (slot == null) {
            return 0f;
        }

        final int originalMax = PotionSprayer.getOriginalMax(slot.stack());
        if (originalMax <= 0) {
            return 1f;
        }

        final int remaining = PotionSprayer.getSprayTicks(slot.stack());
        return Mth.clamp((float) remaining / originalMax, 0f, 1f);
    }

    private void renderStaticModel(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buf, int light, int overlay) {
        final Minecraft minecraft = Minecraft.getInstance();

        pose.pushPose();
        ClientUtil.applyStaticTransform(ctx, pose);

        final ModelResourceLocation modelResourceLocation = new ModelResourceLocation(Clockwork.MOD_ID, MODEL_2D, "inventory");
        minecraft.getItemRenderer().render(stack, ctx, false, pose, buf, light, overlay, minecraft.getModelManager().getModel(modelResourceLocation));

        pose.popPose();
    }

    public static boolean isLiquidBone(GeoBone bone) {
        final String name = bone.getName();
        return BONE_LIQUID_1.equals(name) || BONE_LIQUID_2.equals(name);
    }

    public int getPotionColor() {
        return potionColor;
    }

}