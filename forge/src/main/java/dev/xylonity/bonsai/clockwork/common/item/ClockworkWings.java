package dev.xylonity.bonsai.clockwork.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.ClockworkWingsBoostProjectile;
import dev.xylonity.bonsai.clockwork.common.item.generic.GenericGeckoArmorItem;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClockworkWings extends GenericGeckoArmorItem {

    private static final String NBT_GLIDING = "clockwork_wings_gliding";
    private static final String NBT_INIT = "clockwork_wings_init";
    private static final String NBT_FALL_ACC = "clockwork_wings_fall_acc";
    private static final String NBT_DIVE_ACC = "clockwork_wings_dive_acc";
    private static final String NBT_BOOST_UNTIL = "clockwork_wings_boost_until";
    private static final String NBT_FLAP_PULSE = "clockwork_wings_flap_pulse";

    private static final int FALL_ACCELERATION_MAX = 8;
    private static final int DIVE_ACCELERATION_MAX = 12;

    public static final int BOOST_COOLDOWN_TICKS = 100;

    // Client sided animation state cache to prevent NBT sync issues
    private static final Map<UUID, AnimationStateData> clientAnimationCache = new HashMap<>();

    private static class AnimationStateData {
        boolean isGliding = false;
        boolean isInit = false;
        int fallAcc = 0;
        int diveAcc = 0;
        boolean flapPulse = false;
        long lastUpdateTick = 0;
    }

    private static final RawAnimation CLOSED = RawAnimation.begin().thenLoop("closed");
    private static final RawAnimation GLIDE = RawAnimation.begin().thenLoop("glide");
    private static final RawAnimation FALL = RawAnimation.begin().thenLoop("fall");
    private static final RawAnimation DIVE = RawAnimation.begin().thenLoop("dive");
    private static final RawAnimation FLAP = RawAnimation.begin().thenPlay("flap");

    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("close");

    public ClockworkWings(Properties properties, ArmorMaterials armorMaterial, Type type, String id) {
        super(armorMaterial, type, properties, id);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return ClockworkConfig.CLOCKWORK_WINGS_DEFAULT_DURABILITY;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        // Empty armor material atts
        return ImmutableMultimap.of();
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        // Hard elytra compatibility
        return isFlyEnabled(stack);
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (!entity.level().isClientSide) {
            int next = flightTicks + 1;
            if (next % 10 == 0) {
                if (next % 20 == 0) {
                    if (entity instanceof Player player && !player.getAbilities().instabuild) {
                        stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(EquipmentSlot.CHEST));
                    }
                }

                entity.gameEvent(GameEvent.ELYTRA_GLIDE);
            }
        }

        return true;
    }

    public static boolean isFlyEnabled(ItemStack stack) {
        return stack.getDamageValue() < stack.getMaxDamage() - 1;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!(entity instanceof LivingEntity player)) {
            return;
        }

        if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() == this) {
            // Handles boost detection
            if (level.isClientSide) {
                if (!level.getEntitiesOfClass(ClockworkWingsBoostProjectile.class, player.getBoundingBox().inflate(1.6), e -> e.isAlive() && e.tickCount <= 1).isEmpty()) {
                    AnimationStateData stateData = getOrCreateAnimationState(player.getUUID());
                    stateData.flapPulse = true;
                }
            }
        }
        else {
            // Clears both NBT and cache when unequipped
            if (level.isClientSide) {
                clientAnimationCache.remove(player.getUUID());
            }

            resetFlags(stack);
        }
    }

    private static AnimationStateData getOrCreateAnimationState(UUID playerId) {
        return clientAnimationCache.computeIfAbsent(playerId, k -> new AnimationStateData());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "state", 2, this::statePredicate));
        registrar.add(new AnimationController<>(this, "transition", 2, this::transitionPredicate));
    }

    private <T extends GeoAnimatable> PlayState statePredicate(AnimationState<T> event) {
        Entity raw = event.getData(DataTickets.ENTITY);

        // If not attached to an entity (a player most of the time)
        if (!(raw instanceof LivingEntity player)) {
            return PlayState.STOP;
        }

        // If the item is not valid
        ItemStack stack = event.getData(DataTickets.ITEMSTACK);
        if (stack == null || stack.isEmpty()) {
            return PlayState.STOP;
        }

        // If the player is not wearing the wings
        if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() != this) {
            return PlayState.STOP;
        }

        UUID playerId = player.getUUID();
        Level level = player.level();

        // Using client-sided cache for animations to avoid NBT sync issues (that for some reason happen in survival and not in creative mode)
        if (level.isClientSide) {
            AnimationStateData stateData = getOrCreateAnimationState(playerId);
            long currentTick = level.getGameTime();

            // Only updates state every few ticks to prevent fast changes that could break anims
            if (currentTick - stateData.lastUpdateTick >= 2) {
                boolean isGlidingNow = player.isFallFlying() && isFlyEnabled(stack);
                boolean isFallingNow = !isGlidingNow && computeFalling(player);
                boolean isDivingNow = isGlidingNow && computeDiving(player);

                // hysteresis
                if (isFallingNow) {
                    stateData.fallAcc = Math.min(stateData.fallAcc + 1, FALL_ACCELERATION_MAX);
                }
                else {
                    stateData.fallAcc = Math.max(stateData.fallAcc - 1, 0);
                }

                if (isDivingNow) {
                    stateData.diveAcc = Math.min(stateData.diveAcc + 1, DIVE_ACCELERATION_MAX);
                }
                else {
                    stateData.diveAcc = Math.max(stateData.diveAcc - 1, 0);
                }

                // Apply thresholds
                isFallingNow = stateData.fallAcc >= 2;
                isDivingNow = stateData.diveAcc >= 8;

                stateData.lastUpdateTick = currentTick;

                // First frame after equip or render init
                if (!stateData.isInit) {
                    if (isGlidingNow) {
                        event.setAndContinue(isDivingNow ? DIVE : GLIDE);
                    }
                    else if (isFallingNow) {
                        event.setAndContinue(FALL);
                    }
                    else {
                        event.setAndContinue(CLOSED);
                    }

                    stateData.isInit = true;
                    stateData.isGliding = isGlidingNow;

                    return PlayState.CONTINUE;
                }

                // After init updates
                if (isGlidingNow) {
                    event.setAndContinue(isDivingNow ? DIVE : GLIDE);
                }
                else if (isFallingNow) {
                    event.setAndContinue(FALL);
                }
                else {
                    event.setAndContinue(CLOSED);
                }
            }
        }

        return PlayState.CONTINUE;
    }

    private <T extends GeoAnimatable> PlayState transitionPredicate(AnimationState<T> event) {
        Entity raw = event.getData(DataTickets.ENTITY);

        // If not attached to an entity (a player most of the time)
        if (!(raw instanceof LivingEntity player)) {
            return PlayState.STOP;
        }

        // If the item is not valid
        ItemStack stack = event.getData(DataTickets.ITEMSTACK);
        if (stack == null || stack.isEmpty()) {
            return PlayState.STOP;
        }

        // If the player is not wearing the wings
        if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() != this) {
            return PlayState.STOP;
        }

        UUID uuid = player.getUUID();
        Level level = player.level();

        if (level.isClientSide) {
            AnimationStateData stateData = getOrCreateAnimationState(uuid);

            // Skip transitions until the state has initialized
            if (!stateData.isInit) {
                return PlayState.STOP;
            }

            boolean isGlidingNow = player.isFallFlying() && isFlyEnabled(stack);

            // Checks if a flap pulse is pending
            if (stateData.flapPulse) {
                stateData.flapPulse = false;

                // If gliding, plays the flap
                if (isGlidingNow) {
                    event.getController().forceAnimationReset();
                    event.setAndContinue(FLAP);

                    return PlayState.CONTINUE;
                }
            }

            // If gliding started
            if (isGlidingNow && !stateData.isGliding) {
                stateData.isGliding = true;

                event.getController().forceAnimationReset();
                event.setAndContinue(OPEN);

                return PlayState.CONTINUE;
            }

            // If gliding stopped
            if (!isGlidingNow && stateData.isGliding) {
                stateData.isGliding = false;

                event.getController().forceAnimationReset();
                event.setAndContinue(CLOSE);

                return PlayState.CONTINUE;
            }
        }

        // If a transition is still playing, continues it until it finished
        return (event.getController().getCurrentAnimation() != null && !event.getController().hasAnimationFinished()) ? PlayState.CONTINUE : PlayState.STOP;
    }

    private static boolean computeFalling(LivingEntity entity) {
        if (entity.isFallFlying()) return false;
        if (entity.onClimbable()) return false;
        if (entity.isInWater() || entity.isInLava()) return false;
        if (entity.onGround()) return false;

        // True if vertical speed downward enough and fall distance is big enough
        return entity.getDeltaMovement().y <= -0.1 && entity.fallDistance >= 0.9;
    }

    private static boolean computeDiving(LivingEntity player) {
        if (!player.isFallFlying()) return false;
        if (player.onClimbable()) return false;
        if (player.isInWater() || player.isInLava()) return false;

        Vec3 deltaMovement = player.getDeltaMovement();
        if (deltaMovement.length() < 0.02) return false;

        // Diving if the velocity downward is aligned with the look and pitch steep
        return deltaMovement.y <= -0.35 && player.getLookAngle().normalize().dot(deltaMovement.normalize()) >= 0.7 && player.getXRot() >= 42f;
    }

    private static void resetFlags(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(NBT_INIT, false);
        tag.putBoolean(NBT_GLIDING, false);
        tag.putInt(NBT_FALL_ACC, 0);
        tag.putInt(NBT_DIVE_ACC, 0);
        tag.putBoolean(NBT_FLAP_PULSE, false);
    }

    public static void spawnBoostEntity(Level level, Player player) {
        if (level.isClientSide) return;

        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (stack.isEmpty() || stack.getItem() != player.getItemBySlot(EquipmentSlot.CHEST).getItem()) {
            return;
        }

        if (!(stack.getItem() instanceof ClockworkWings)) {
            return;
        }

        if (!player.isFallFlying()) {
            return;
        }

        if (!isFlyEnabled(stack)) {
            return;
        }

        int ticksLeft = getBoostRemainingTicks(stack, level);
        if (ticksLeft > 0) {
            int secsLeft = (int)Math.ceil(ticksLeft / 20.0);
            player.displayClientMessage(Component.literal("Boost ready in " + secsLeft + "s"), true);
            return;
        }

        level.addFreshEntity(new ClockworkWingsBoostProjectile(ClockworkEntities.CLOCKWORK_WINGS_BOOST_PROJECTILE.get(), level, player));

        double dx = (level.random.nextDouble() - 0.5) * 2.0;
        double dy = (level.random.nextDouble() - 0.5) * 2.0;
        double dz = (level.random.nextDouble() - 0.5) * 2.0;
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + player.getBbHeight() * 0.5f, player.getZ(), 10, dx, dy, dz, 0.0225);
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(NBT_BOOST_UNTIL, level.getGameTime() + BOOST_COOLDOWN_TICKS);
        tag.putBoolean(NBT_FLAP_PULSE, true);
    }

    private static int getBoostRemainingTicks(ItemStack stack, Level level) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }

        long now = level.getGameTime();
        long until = tag.getLong(NBT_BOOST_UNTIL);
        long left = until - now;

        return (int) Math.max(0, left);
    }

}