package dev.xylonity.bonsai.clockwork.common.item.wings;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.xylonity.bonsai.clockwork.client.armor.renderer.GenericArmorItemRenderer;
import dev.xylonity.bonsai.clockwork.common.item.gecko.GeckoArmorItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class ClockworkWings extends GeckoArmorItem implements CustomGlider {

    private static final RawAnimation CLOSED = RawAnimation.begin().thenPlay("closed");
    private static final RawAnimation DIVING = RawAnimation.begin().thenPlay("diving");
    private static final RawAnimation GLIDING = RawAnimation.begin().thenPlay("gliding");
    private static final RawAnimation FALLING = RawAnimation.begin().thenPlay("falling");
    private static final RawAnimation LAND = RawAnimation.begin().thenPlay("land");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("close");
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation FLAP = RawAnimation.begin().thenPlay("flap");

    public ClockworkWings(Properties properties, ArmorMaterial material, Type type) {
        super(material, type, properties);
    }

    @Override
    protected Object createGeckoRenderer() {
        return new GenericArmorItemRenderer("clockwork_wings");
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return ImmutableMultimap.of();
    }

    @Override
    public boolean canGlide(ItemStack stack, LivingEntity entity) {
        return stack.getDamageValue() < stack.getMaxDamage() - 1;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 2, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> event) {
        final Entity rawEntity = event.getData(DataTickets.ENTITY);
        if (!(rawEntity instanceof Player player)) {
            return PlayState.CONTINUE;
        }

        final ItemStack stack = event.getData(DataTickets.ITEMSTACK);
        if (stack.isEmpty()) {
            return PlayState.CONTINUE;
        }

        final boolean isGliding = player.isFallFlying();
        final boolean isFalling = !player.onGround() && !isGliding && player.getDeltaMovement().y < -0.5;
        final boolean isDiving = isGliding && player.getDeltaMovement().y < -0.8 && player.getXRot() > 42f;

        final CompoundTag tag = stack.getOrCreateTag();

        // To prevent the wings from going to the default model position for some reason on the first rendering tick
        if (!tag.getBoolean("AnimInit")) {
            tag.putBoolean("AnimInit", true);
            event.getController().transitionLength(0);
            event.setAndContinue(CLOSED);
            return PlayState.CONTINUE;
        }

        event.getController().transitionLength(2);

        final boolean wasOnGround = tag.getBoolean("WasOnGround");
        final boolean wasGliding = tag.getBoolean("WasGliding");
        final boolean onGround = player.onGround();

        // 1 falling, 2 gliding/diving
        if (!onGround) {
            int airborne = tag.getInt("AirborneState");
            if (isGliding || isDiving) {
                airborne = 2;
            }
            else if (isFalling && airborne < 2) {
                airborne = 1;
            }

            tag.putInt("AirborneState", airborne);
        }

        // Landing
        if (onGround && !wasOnGround) {
            tag.putBoolean("WasOnGround", true);
            tag.putInt("LandTick", player.tickCount);
            tag.putInt("LandedFrom", tag.getInt("AirborneState"));
            tag.putInt("AirborneState", 0);
        }
        else if (!onGround) {
            tag.putBoolean("WasOnGround", false);
        }

        // Glide stop (mid-air or on ground)
        if (wasGliding && !isGliding) {
            tag.putInt("CloseTick", player.tickCount);
        }

        if (isGliding && !wasGliding) {
            tag.putInt("OpenTick", player.tickCount);
        }
        tag.putBoolean("WasGliding", isGliding);

        final int closeTick = tag.getInt("CloseTick");
        final boolean playingClose = closeTick > 0 && player.tickCount - closeTick < 12;

        final int flapTick = tag.getInt("FlapTick");
        final boolean playingFlap = flapTick > 0 && player.tickCount - flapTick < 13;

        if (playingClose && !isGliding) {
            event.setAnimation(CLOSE);
        }
        else if (playingFlap && (isGliding || isFalling || isDiving)) {
            event.setAnimation(FLAP);
        }
        else if (isFalling) {
            event.setAnimation(FALLING);
        }
        else if (isDiving) {
            event.setAnimation(DIVING);
        }
        else if (isGliding) {
            final int openTick = tag.getInt("OpenTick");
            if (player.tickCount - openTick < 8) {
                event.setAnimation(OPEN);
            }
            else {
                event.setAnimation(GLIDING);
            }

        }
        else {
            final int landTick = tag.getInt("LandTick");
            final int landedFrom = tag.getInt("LandedFrom");
            if (landedFrom == 1 && player.tickCount - landTick < 7) {
                event.setAnimation(LAND);
            }
            else {
                event.setAnimation(CLOSED);
            }

        }

        return PlayState.CONTINUE;
    }

}