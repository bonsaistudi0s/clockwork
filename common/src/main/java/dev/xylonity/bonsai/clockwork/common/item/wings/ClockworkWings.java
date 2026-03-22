package dev.xylonity.bonsai.clockwork.common.item.wings;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.armor.renderer.GenericArmorItemRenderer;
import dev.xylonity.bonsai.clockwork.common.item.gecko.GeckoArmorItem;
import dev.xylonity.bonsai.clockwork.network.packets.c2s.ClockworkWingsSoundC2SPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.HashMap;
import java.util.Map;

public class ClockworkWings extends GeckoArmorItem implements CustomGlider {

    private static final RawAnimation CLOSED = RawAnimation.begin().thenPlay("closed");
    private static final RawAnimation DIVING = RawAnimation.begin().thenPlay("diving");
    private static final RawAnimation GLIDING = RawAnimation.begin().thenPlay("gliding");
    private static final RawAnimation FALLING = RawAnimation.begin().thenPlay("falling");
    private static final RawAnimation LAND = RawAnimation.begin().thenPlay("land");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("close").thenLoop("closed");
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation FLAP = RawAnimation.begin().thenPlay("flap");

    // Client-sided animation state to prevent anims from overlapping per second (since the wings are updated per x point/s of durability lost)
    private static final Map<Integer, WingsAnimState> ANIMATION_STATE = new HashMap<>();

    public static class WingsAnimState {
        public boolean wasOnGround;
        public boolean wasGliding;
        public int airborneState;
        public int landTick;
        public int landedFrom;
        public int closeTick;
        public int openTick;
        public int flapTick;
        public boolean equipped;
    }

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

    public static WingsAnimState getAnimState(int entityId) {
        return ANIMATION_STATE.computeIfAbsent(entityId, k -> new WingsAnimState());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 2, this::predicate));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide && entity instanceof Player player) {
            final boolean isEquipped = player.getItemBySlot(EquipmentSlot.CHEST) == stack;
            final WingsAnimState state = getAnimState(player.getId());

            // Trying to solve the animation bug (at the first tick) triggered by the singleton instance
            if (isEquipped && !state.equipped) {
                state.closeTick = 0;
                state.openTick = 0;
                state.flapTick = 0;
                state.landTick = 0;
                state.airborneState = 0;
                state.landedFrom = 0;
                state.wasGliding = false;
                state.wasOnGround = player.onGround();
            }

            state.equipped = isEquipped;
        }

    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> event) {
        final Entity rawEntity = event.getData(DataTickets.ENTITY);
        if (!(rawEntity instanceof Player player)) {
            event.setAndContinue(CLOSED);
            return PlayState.CONTINUE;
        }

        final ItemStack stack = event.getData(DataTickets.ITEMSTACK);
        if (stack.isEmpty()) {
            event.setAndContinue(CLOSED);
            return PlayState.CONTINUE;
        }

        final boolean isGliding = player.isFallFlying();
        final boolean isFalling = !player.onGround() && !isGliding && player.getDeltaMovement().y < -0.75;
        final boolean isDiving = isGliding && player.getDeltaMovement().y < -1.1 && player.getXRot() > 42f;

        final WingsAnimState state = getAnimState(player.getId());

        final boolean onGround = player.onGround();

        // 1 falling, 2 gliding/diving
        if (!onGround) {
            if (isGliding || isDiving) {
                state.airborneState = 2;
            }
            else if (isFalling && state.airborneState < 2) {
                state.airborneState = 1;
            }

        }

        // Landing
        if (onGround && !state.wasOnGround) {
            state.wasOnGround = true;
            state.landTick = player.tickCount;
            state.landedFrom = state.airborneState;
            state.airborneState = 0;
        }
        else if (!onGround) {
            state.wasOnGround = false;
        }

        // Glide stop (mid-air or on ground)
        if (state.wasGliding && !isGliding) {
            state.closeTick = player.tickCount;
            Clockwork.NETWORK.sendToServer(new ClockworkWingsSoundC2SPacket(0));
        }

        if (isGliding && !state.wasGliding) {
            state.openTick = player.tickCount;
            Clockwork.NETWORK.sendToServer(new ClockworkWingsSoundC2SPacket(1));
        }
        state.wasGliding = isGliding;

        final boolean playingClose = state.closeTick > 0 && player.tickCount - state.closeTick < 12;
        final boolean playingFlap = state.flapTick > 0 && player.tickCount - state.flapTick < 13;

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
            if (player.tickCount - state.openTick < 8) {
                event.setAnimation(OPEN);
            }
            else {
                event.setAnimation(GLIDING);
            }

        }
        else {
            if (state.landedFrom == 1 && player.tickCount - state.landTick < 7) {
                event.setAnimation(LAND);
            }
            else {
                event.setAnimation(CLOSED);
            }

        }

        return PlayState.CONTINUE;
    }

}