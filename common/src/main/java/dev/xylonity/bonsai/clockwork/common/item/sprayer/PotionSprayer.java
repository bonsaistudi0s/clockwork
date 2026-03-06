package dev.xylonity.bonsai.clockwork.common.item.sprayer;

import dev.xylonity.bonsai.clockwork.client.item.renderer.PotionSprayerRenderer;
import dev.xylonity.bonsai.clockwork.client.particle.PotionSprayParticleData;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.trigger.PotionSprayTriggerProjectile;
import dev.xylonity.bonsai.clockwork.common.item.gecko.GeckoItem;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import dev.xylonity.knightlib.api.util.KnightLibUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PotionSprayer extends GeckoItem {

    public static final String NBT_SPRAY_TICKS = "spray_ticks_left";
    public static final String NBT_ORIGINAL_MAX = "spray_original_max";
    public static final String NBT_SPRAYING = "clockwork_spraying";
    private static final String NBT_USE_TICKS = "spray_use_ticks";

    private static final RawAnimation ANIM_USE = RawAnimation.begin().thenPlay("use");

    private static final int DURABILITY_INTERVAL = 20;

    public PotionSprayer(Properties properties) {
        super(properties);
    }

    @Override
    protected Object createGeckoRenderer() {
        return new PotionSprayerRenderer();
    }

    @Override
    public @NotNull UseAnim getUseAnimation(final @NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(final @NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean isValidRepairItem(final @NotNull ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(ClockworkItems.CLOCKWORK_GEAR.get());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        final ItemStack sprayer = player.getItemInHand(hand);

        // No compatible potion
        final PotionSlot potionSlot = findFirstPotion(player);
        if (potionSlot == null) {
            return InteractionResultHolder.fail(sprayer);
        }

        // Initialize spray tracking nbt on the potion if not already present
        if (!hasSprayTracking(potionSlot.stack)) {
            final int ticks = initSprayTracking(potionSlot.stack);
            if (ticks <= 0) {
                return InteractionResultHolder.fail(sprayer);
            }

            potionSlot.save.run();
        }

        // Marks the sprayer as active and begins the continuous use
        setSpraying(sprayer, true);
        setUseTicks(sprayer, 0);
        player.startUsingItem(hand);

        return InteractionResultHolder.consume(sprayer);
    }

    @Override
    public void onUseTick(final Level level, final @NotNull LivingEntity livingEntity, final @NotNull ItemStack itemStack, int remainingUseDuration) {
        if (level.isClientSide) {
            return;
        }
        if (!(livingEntity instanceof Player player)) {
            return;
        }

        // Applies durability damage at regular intervals
        tickDurability(itemStack, player);

        // Stops if the potion is not available
        final PotionSlot slot = findFirstPotion(player);
        if (slot == null) {
            stopSpraying(player, itemStack);
            return;
        }

        // Inits tracking if the potion was added to the inventory mid-use
        if (!hasSprayTracking(slot.stack)) {
            final int ticks = initSprayTracking(slot.stack);
            if (ticks <= 0) {
                stopSpraying(player, itemStack);
                return;
            }

            slot.save.run();
        }

        // Decrementing spray ticks
        final int remaining = getSprayTicks(slot.stack) - 1;
        setSprayTicks(slot.stack, remaining);
        slot.save.run();

        // Spawn particles and the trigger projectile
        spawnSprayEffects((ServerLevel) level, player, slot.stack);

        // If the potion is emptied, consuming and moving to the next one
        if (remaining <= 0) {
            consumePotion(player, slot);
            advanceToNextPotion(player, itemStack);
        }
    }

    @Override
    public void releaseUsing(final @NotNull ItemStack sprayer, final @NotNull Level level, final @NotNull LivingEntity entity, int timeLeft) {
        setSpraying(sprayer, false);
        setUseTicks(sprayer, 0);
    }

    @Override
    public void inventoryTick(final @NotNull ItemStack stack, final Level level, final @NotNull Entity entity, int slot, boolean selected) {
        if (!level.isClientSide) {
            return;
        }

        if (isSpraying(stack) && !isActivelyUsed(stack, entity)) {
            setSpraying(stack, false);
        }
    }

    public static boolean isCompatiblePotion(final ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (!stack.is(Items.POTION) && !stack.is(Items.SPLASH_POTION) && !stack.is(Items.LINGERING_POTION)) {
            return false;
        }

        return !hasInstantEffect(stack);
    }

    private static boolean hasInstantEffect(final ItemStack stack) {
        return PotionUtils.getMobEffects(stack).stream().anyMatch(mobEffectInstance -> mobEffectInstance.getEffect().isInstantenous());
    }

    public static int getMaxEffectDuration(final ItemStack potion) {
        int max = 0;
        for (MobEffectInstance effect : PotionUtils.getMobEffects(potion)) {
            max = Math.max(max, effect.getDuration());
        }

        return max;
    }

    public static int getSprayTicks(final ItemStack potion) {
        return potion.getOrCreateTag().getInt(NBT_SPRAY_TICKS);
    }

    public static int getOriginalMax(final ItemStack potion) {
        return potion.getOrCreateTag().getInt(NBT_ORIGINAL_MAX);
    }

    private static void setSprayTicks(final ItemStack potion, int ticks) {
        potion.getOrCreateTag().putInt(NBT_SPRAY_TICKS, Math.max(0, ticks));
    }

    private static boolean hasSprayTracking(final ItemStack potion) {
        return potion.hasTag() && potion.getTag().contains(NBT_SPRAY_TICKS);
    }

    private static int initSprayTracking(final ItemStack potion) {
        final int maxDuration = getMaxEffectDuration(potion);
        if (maxDuration <= 0) {
            return 0;
        }

        potion.getOrCreateTag().putInt(NBT_ORIGINAL_MAX, maxDuration);
        setSprayTicks(potion, maxDuration);

        return maxDuration;
    }

    public static boolean isSpraying(final ItemStack sprayer) {
        return sprayer.hasTag() && sprayer.getTag().getBoolean(NBT_SPRAYING);
    }

    private static void setSpraying(final ItemStack sprayer, boolean value) {
        sprayer.getOrCreateTag().putBoolean(NBT_SPRAYING, value);
    }

    private static int getUseTicks(final ItemStack sprayer) {
        return sprayer.getOrCreateTag().getInt(NBT_USE_TICKS);
    }

    private static void setUseTicks(final ItemStack sprayer, int ticks) {
        sprayer.getOrCreateTag().putInt(NBT_USE_TICKS, ticks);
    }

    public static List<MobEffectInstance> scaleEffectsBySprayRemaining(final ItemStack potionStack, List<MobEffectInstance> originalEffects) {
        if (!hasSprayTracking(potionStack)) {
            return originalEffects;
        }

        final int remaining = Math.max(0, getSprayTicks(potionStack));
        final int originalMax = getOriginalMax(potionStack);

        if (remaining <= 0) {
            return List.of();
        }
        if (originalMax <= 0) {
            return originalEffects;
        }

        // Normalized ratio of the remaining ticks to the original duration of the current potion
        final double ratio = Math.min(1.0, (double) remaining / (double) originalMax);

        // Rebuilds each effect with its duration scaled by the ratio
        final List<MobEffectInstance> scaled = new ArrayList<>(originalEffects.size());
        for (MobEffectInstance effect : originalEffects) {
            final int newDuration = Math.max(1, (int) Math.floor(effect.getDuration() * ratio));
            scaled.add(new MobEffectInstance(
                    effect.getEffect(),
                    newDuration,
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.isVisible(),
                    effect.showIcon()
            ));

        }

        return scaled;
    }

    @Nullable
    public static PotionSlot findFirstPotion(final Player player) {
        final ItemStack offhandItem = player.getOffhandItem();
        if (isCompatiblePotion(offhandItem)) {
            return new PotionSlot(offhandItem, () -> player.setItemInHand(InteractionHand.OFF_HAND, offhandItem));
        }

        final ItemStack mainHandItem = player.getMainHandItem();
        if (isCompatiblePotion(mainHandItem)) {
            return new PotionSlot(mainHandItem, () -> player.setItemInHand(InteractionHand.MAIN_HAND, mainHandItem));
        }

        final Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            final ItemStack potion = inventory.getItem(i);
            if (isCompatiblePotion(potion)) {
                final int slot = i;
                return new PotionSlot(potion, () -> inventory.setItem(slot, potion));
            }

        }

        return null;
    }

    private void spawnSprayEffects(final ServerLevel level, final Player player, final ItemStack potion) {
        // Slightly in front of and below the player's eyes
        final Vec3 playerLookAngle = player.getLookAngle().normalize();
        final Vec3 spawnPosition = player.getEyePosition(1.0f)
                .add(playerLookAngle.scale(0.8))
                .add(0.0, -0.3, 0.0);

        // Base velocity aligned with the player's look direction
        final Vec3 velocity = playerLookAngle.scale(0.3225);
        final int color = PotionUtils.getColor(potion);

        level.sendParticles(
                new PotionSprayParticleData((float) velocity.x, (float) velocity.y, (float) velocity.z, color),
                spawnPosition.x, spawnPosition.y, spawnPosition.z,
                10, 0, 0, 0, 0
        );

        if (level.random.nextFloat() < 0.38 && player.tickCount % 2 == 0) {
            final PotionSprayTriggerProjectile sprayTriggerProjectile = new PotionSprayTriggerProjectile(
                    ClockworkEntities.POTION_SPRAY_TRIGGER_PROJECTILE.get(), level, potion
            );

            sprayTriggerProjectile.setPos(spawnPosition.x, spawnPosition.y, spawnPosition.z);
            sprayTriggerProjectile.setDeltaMovement(
                    KnightLibUtil.randomVectorInCone(velocity, 40, new Random()).scale(0.65 * (0.8 + level.random.nextDouble() * 0.4))
            );

            sprayTriggerProjectile.setOwner(player);

            level.addFreshEntity(sprayTriggerProjectile);
        }

    }

    private void tickDurability(final ItemStack sprayer, final Player player) {
        final int ticks = getUseTicks(sprayer) + 1;
        setUseTicks(sprayer, ticks);

        if (ticks % DURABILITY_INTERVAL == 0 && !player.getAbilities().instabuild) {
            sprayer.hurtAndBreak(1, player, playerEntity -> playerEntity.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
    }

    private void consumePotion(final Player player, final PotionSlot slot) {
        final ItemStack potion = slot.stack;
        final boolean isRegularPotion = potion.is(Items.POTION);

        potion.shrink(1);
        slot.save.run();

        if (isRegularPotion) {
            final ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().add(glassBottle)) {
                player.drop(glassBottle, false);
            }

        }

    }

    private void advanceToNextPotion(final Player player, final ItemStack sprayer) {
        final PotionSlot next = findFirstPotion(player);
        if (next == null) {
            stopSpraying(player, sprayer);
            return;
        }

        if (!hasSprayTracking(next.stack)) {
            final int ticks = initSprayTracking(next.stack);
            if (ticks <= 0) {
                stopSpraying(player, sprayer);
                return;
            }

            next.save.run();
        }

    }

    private void stopSpraying(final Player player, final ItemStack sprayer) {
        player.stopUsingItem();
        setSpraying(sprayer, false);
        setUseTicks(sprayer, 0);
    }

    private static boolean isActivelyUsed(final ItemStack stack, final Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }

        return livingEntity.isUsingItem() && livingEntity.getUseItem() == stack;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(
                new AnimationController<>(this, "main", 0, state -> {
                    final ItemStack stack = state.getData(DataTickets.ITEMSTACK);
                    if (stack == null || !isSpraying(stack)) {
                        return PlayState.STOP;
                    }

                    state.setAnimation(ANIM_USE);

                    return PlayState.CONTINUE;
                })

        );

    }

    @Override
    public double getBoneResetTime() {
        return 0;
    }

    public record PotionSlot(
            ItemStack stack,
            Runnable save
    ) {
        ;;
    }

}