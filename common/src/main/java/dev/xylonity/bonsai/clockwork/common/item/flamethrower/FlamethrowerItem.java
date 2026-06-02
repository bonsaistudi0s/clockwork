package dev.xylonity.bonsai.clockwork.common.item.flamethrower;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.item.renderer.FlamethrowerRenderer;
import dev.xylonity.bonsai.clockwork.client.particle.FlamethrowerParticleData;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.trigger.FlamethrowerTriggerProjectile;
import dev.xylonity.bonsai.clockwork.common.item.gecko.GeckoItem;
import dev.xylonity.bonsai.clockwork.common.util.StackNbt;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import dev.xylonity.knightlib.api.sound.persistent.KnightLibPersistentSounds;
import dev.xylonity.knightlib.api.util.KnightLibUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

import java.util.Random;

public class FlamethrowerItem extends GeckoItem {

    public static final String NBT_SPRAYING = "clockwork_spraying";
    private static final String NBT_USE_TICKS = "flamethrower_use_ticks";
    private static final String NBT_FUEL_TICKS = "flamethrower_fuel_ticks";

    private static final RawAnimation ANIM_USE = RawAnimation.begin().thenPlay("shoot_loop");

    private static final int DURABILITY_INTERVAL = 20;

    static final int PARTICLE_COUNT = 12;
    static final double PARTICLE_BASE_SPEED = 0.3225;
    private static final double PROJECTILE_BASE_SPEED = 0.85;

    public FlamethrowerItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Object createGeckoRenderer() {
        return new FlamethrowerRenderer();
    }

    @Override
    public @NotNull UseAnim getUseAnimation(final @NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(final @NotNull ItemStack stack, final @NotNull LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean isValidRepairItem(final @NotNull ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(ClockworkItems.CLOCKWORK_GEAR.get());
    }

    @Override
    public double getBoneResetTime() {
        return 0;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        final ItemStack flamethrower = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            if (getFuelTicks(flamethrower) <= 0 && findBlazePowder(player) < 0) {
                return InteractionResultHolder.fail(flamethrower);
            }

        }

        setSpraying(flamethrower, true);
        setUseTicks(flamethrower, 0);
        player.startUsingItem(hand);

        return InteractionResultHolder.consume(flamethrower);
    }

    @Override
    public void onUseTick(final Level level, final @NotNull LivingEntity livingEntity, final @NotNull ItemStack itemStack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }

        if (level.isClientSide) {
            KnightLibPersistentSounds.tick(player, "clockwork:flamethrower");
            Clockwork.PROXY.trySpawnFirstPersonFlameParticles(player);
            return;
        }

        final int ticks = getUseTicks(itemStack) + 1;
        setUseTicks(itemStack, ticks);

        if (ticks % DURABILITY_INTERVAL == 0 && !player.getAbilities().instabuild) {
            final EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
            itemStack.hurtAndBreak(1, player, slot);
        }

        if (!player.getAbilities().instabuild) {
            int fuel = getFuelTicks(itemStack);
            if (fuel <= 0) {
                final int slot = findBlazePowder(player);
                if (slot < 0) {
                    stopSpraying(player, itemStack);
                    return;
                }

                player.getInventory().getItem(slot).shrink(1);
                fuel = ClockworkConfig.FLAMETHROWER_USAGE_SECONDS_PER_POWDER * 20;
            }

            setFuelTicks(itemStack, fuel - 1);
        }

        spawnFlameParticles((ServerLevel) level, player);
    }

    @Override
    public void releaseUsing(final @NotNull ItemStack stack, final @NotNull Level level, final @NotNull LivingEntity entity, int timeLeft) {
        setSpraying(stack, false);
        setUseTicks(stack, 0);
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

    private static int findBlazePowder(final Player player) {
        final var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(Items.BLAZE_POWDER)) {
                return i;
            }

        }

        return -1;
    }

    public static boolean isSpraying(final ItemStack stack) {
        return StackNbt.tag(stack).getBoolean(NBT_SPRAYING);
    }

    private static void setSpraying(final ItemStack stack, boolean value) {
        StackNbt.update(stack, tag -> tag.putBoolean(NBT_SPRAYING, value));
    }

    private static int getUseTicks(final ItemStack stack) {
        return StackNbt.tag(stack).getInt(NBT_USE_TICKS);
    }

    private static void setUseTicks(final ItemStack stack, int ticks) {
        StackNbt.update(stack, tag -> tag.putInt(NBT_USE_TICKS, ticks));
    }

    private static int getFuelTicks(final ItemStack stack) {
        return StackNbt.tag(stack).getInt(NBT_FUEL_TICKS);
    }

    private static void setFuelTicks(final ItemStack stack, int ticks) {
        StackNbt.update(stack, tag -> tag.putInt(NBT_FUEL_TICKS, ticks));
    }

    private void spawnFlameParticles(final ServerLevel level, final Player player) {
        final Vec3 playerLookAngle = player.getLookAngle().normalize();
        final Vec3 spawnPosition = player.getEyePosition(1.0f).add(playerLookAngle.scale(0.8)).add(0.0, -0.3, 0.0);

        final Vec3 baseVelocity = playerLookAngle.scale(PARTICLE_BASE_SPEED);

        level.sendParticles(
                new FlamethrowerParticleData(
                        (float) baseVelocity.x, (float) baseVelocity.y, (float) baseVelocity.z,
                        player.getId()
                ),
                spawnPosition.x, spawnPosition.y, spawnPosition.z,
                PARTICLE_COUNT, 0.05, 0.05, 0.05, 0
        );

        final Random random = new Random();
        if (level.random.nextFloat() < 0.8 && player.tickCount % 2 == 0) {
            final FlamethrowerTriggerProjectile projectile = new FlamethrowerTriggerProjectile(
                    ClockworkEntities.FLAMETHROWER_TRIGGER_PROJECTILE.get(), level
            );

            final Vec3 projectileVelocity = playerLookAngle.scale(PROJECTILE_BASE_SPEED * 1.2);
            projectile.setPos(spawnPosition.x, spawnPosition.y, spawnPosition.z);
            projectile.setDeltaMovement(
                    KnightLibUtil.randomVectorInCone(projectileVelocity, 16, random).scale(0.4 + level.random.nextDouble() * 0.4)
            );
            projectile.setOwner(player);

            level.addFreshEntity(projectile);
        }

    }

    private void stopSpraying(final Player player, final ItemStack flamethrower) {
        player.stopUsingItem();
        setSpraying(flamethrower, false);
        setUseTicks(flamethrower, 0);
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

}