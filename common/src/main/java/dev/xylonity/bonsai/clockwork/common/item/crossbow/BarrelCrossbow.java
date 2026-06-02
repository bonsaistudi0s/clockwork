package dev.xylonity.bonsai.clockwork.common.item.crossbow;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.item.renderer.BarrelCrossbowRenderer;
import dev.xylonity.bonsai.clockwork.common.item.crossbow.arrow.BarrelCrossbowProjectiles;
import dev.xylonity.bonsai.clockwork.common.item.gecko.GeckoCrossbowItem;
import dev.xylonity.bonsai.clockwork.common.util.EnchantmentsUtil;
import dev.xylonity.bonsai.clockwork.common.util.StackNbt;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import dev.xylonity.bonsai.clockwork.registry.ClockworkSounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.PlayState;

import java.util.function.Predicate;

public class BarrelCrossbow extends GeckoCrossbowItem {

    public static final String NBT_LOAD_START_TICK  = "barrel_load_start";
    public static final String NBT_LOAD_VARIANT = "barrel_load_variant";
    public static final String NBT_LOAD_PLAYED = "barrel_load_played";
    public static final String NBT_ANIM_PHASE = "barrel_anim_phase";
    public static final String NBT_CAN_LOAD = "barrel_can_load";
    public static final String NBT_NEXT_ACTION_TICK = "barrel_next_action";
    public static final String NBT_BARREL_SPINS = "barrel_spins";

    private static final RawAnimation ANIM_IDLE  = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ANIM_SHOOT = RawAnimation.begin().thenPlay("shoot");
    private static final RawAnimation[] ANIM_LOAD_VARIANTS = {
            RawAnimation.begin().thenPlayAndHold("load"),
            RawAnimation.begin().thenPlayAndHold("load_quickcharge_1"),
            RawAnimation.begin().thenPlayAndHold("load_quickcharge_2"),
            RawAnimation.begin().thenPlayAndHold("load_quickcharge_3"),
    };

    private static final float[] LOAD_DURATIONS_SEC = { 0.25f, 0.21f, 0.18f, 0.14f };
    private static final int SHOOT_RECOVERY_TICKS = 3;
    private static final float LOAD_SOUND_THRESHOLD = 0.2f;

    public static final float ARROW_VELOCITY = 3.15f;
    public static final float FIREWORK_VELOCITY = 1.28f;
    public static final float VELOCITY_MULTIPLIER = 0.7f;
    public static final int HOMING_SCAN_RANGE = 128;

    private boolean loadSoundPlayed = false;

    public BarrelCrossbow(Properties properties) {
        super(properties);
    }

    @Override
    protected Object createGeckoRenderer() {
        return new BarrelCrossbowRenderer();
    }

    public static int clampVariant(int v) {
        return Math.max(0, Math.min(v, ANIM_LOAD_VARIANTS.length - 1));
    }

    public static int getQuickChargeLevel(ItemStack stack) {
        final int raw = EnchantmentsUtil.level(stack, net.minecraft.world.item.enchantment.Enchantments.QUICK_CHARGE);
        return clampVariant(Math.max(0, raw));
    }

    public static int getChargeDurationTicks(ItemStack stack) {
        return chargeDurationTicks(getQuickChargeLevel(stack));
    }

    public static int chargeDurationTicks(int quickChargeLevel) {
        return Math.max(1, Math.round(LOAD_DURATIONS_SEC[clampVariant(quickChargeLevel)] * 20f));
    }

    public static float baseVelocityFor(ItemStack stack) {
        final boolean hasFirework = stack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).contains(Items.FIREWORK_ROCKET);
        return hasFirework ? FIREWORK_VELOCITY : ARROW_VELOCITY;
    }

    public static boolean hasLoadableAmmo(LivingEntity shooter, ItemStack crossbow) {
        if (shooter instanceof Player player && player.getAbilities().instabuild) {
            return true;
        }

        final ItemStack ammo = shooter.getProjectile(crossbow);
        return !ammo.isEmpty() && (ammo.getItem() instanceof ArrowItem || ammo.is(Items.FIREWORK_ROCKET));
    }

    public static Phase getPhase(ItemStack stack) {
        return Phase.fromId(StackNbt.tag(stack).getInt(NBT_ANIM_PHASE));
    }

    private static void setPhase(ItemStack stack, Phase phase) {
        StackNbt.update(stack, tag -> tag.putInt(NBT_ANIM_PHASE, phase.id()));
    }

    private static long getNextActionTick(ItemStack stack) {
        return StackNbt.tag(stack).getLong(NBT_NEXT_ACTION_TICK);
    }

    private static void setNextActionTick(ItemStack stack, long tick) {
        StackNbt.update(stack, tag -> tag.putLong(NBT_NEXT_ACTION_TICK, tick));
    }

    private static boolean isActionDue(ItemStack stack, long now) {
        return now >= getNextActionTick(stack);
    }

    public static float getLoadProgress(ItemStack stack, long currentTick) {
        final CompoundTag tag = StackNbt.tag(stack);
        if (!tag.contains(NBT_LOAD_START_TICK)) {
            return 0f;
        }

        final long start = tag.getLong(NBT_LOAD_START_TICK);
        final int duration = getChargeDurationTicks(stack);
        return Math.max(0f, Math.min(1f, (currentTick - start) / (float) duration));
    }

    public static int getLoadVariant(ItemStack stack) {
        return clampVariant(StackNbt.tag(stack).getInt(NBT_LOAD_VARIANT));
    }

    public static int getBarrelSpins(ItemStack stack) {
        return StackNbt.tag(stack).getInt(NBT_BARREL_SPINS);
    }

    private static void incrementBarrelSpins(ItemStack stack) {
        StackNbt.update(stack, tag -> tag.putInt(NBT_BARREL_SPINS, tag.getInt(NBT_BARREL_SPINS) + 1));
    }

    private static void resetBarrelSpins(ItemStack stack) {
        StackNbt.remove(stack, NBT_BARREL_SPINS);
    }

    private static void beginLoad(ItemStack stack, long tick, int quickChargeLevel) {
        StackNbt.update(stack, tag -> {
            tag.putLong(NBT_LOAD_START_TICK, tick);
            tag.putInt(NBT_LOAD_VARIANT, clampVariant(quickChargeLevel));
            tag.putBoolean(NBT_LOAD_PLAYED, false);
        });
    }

    private static void clearLoadFlags(ItemStack stack) {
        if (!StackNbt.has(stack)) {
            return;
        }

        StackNbt.update(stack, tag -> {
            tag.remove(NBT_LOAD_START_TICK);
            tag.remove(NBT_LOAD_VARIANT);
            tag.remove(NBT_LOAD_PLAYED);
        });
    }

    private static void setCanLoad(ItemStack stack, boolean canLoad) {
        StackNbt.update(stack, tag -> tag.putBoolean(NBT_CAN_LOAD, canLoad));
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return itemStack -> itemStack.getItem() instanceof ArrowItem || itemStack.is(Items.FIREWORK_ROCKET);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CROSSBOW;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(ClockworkItems.CLOCKWORK_GEAR.get());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);

        if (isCharged(stack)) {
            if (!level.isClientSide) {
                fireAndAdvance(level, player, hand, stack);
                if (hasLoadableAmmo(player, stack)) {
                    player.startUsingItem(hand);
                }
                else {
                    setPhase(stack, Phase.IDLE);
                    resetBarrelSpins(stack);
                }

            }

            return InteractionResultHolder.consume(stack);
        }

        if (!hasLoadableAmmo(player, stack)) {
            setPhase(stack, Phase.IDLE);
            setCanLoad(stack, false);
            resetBarrelSpins(stack);
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            startLoading(stack, level.getGameTime());
        }

        player.startUsingItem(hand);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide) {
            return;
        }

        final Phase phase = getPhase(stack);
        final long now = level.getGameTime();

        if (!hasLoadableAmmo(entity, stack) && !isCharged(stack) && phase != Phase.SHOOTING) {
            abortUse(entity, stack);
            return;
        }

        if (phase == Phase.LOADING) {
            tickLoadSound(level, entity, stack, now);
        }

        if (!isActionDue(stack, now)) {
            return;
        }

        switch (phase) {
            case LOADING -> onLoadingComplete(level, entity, stack, now);
            case LOADED -> fireAndAdvance(level, entity, entity.getUsedItemHand(), stack);
            case SHOOTING -> onShootRecoveryDone(level, entity, stack, now);
            default -> {
                ;;
            }

        }

    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        setPhase(stack, Phase.IDLE);
        setCanLoad(stack, false);
        clearLoadFlags(stack);
        resetBarrelSpins(stack);
    }

    private void onLoadingComplete(Level level, LivingEntity entity, ItemStack stack, long now) {
        if (chargeFromInventory(level, entity, stack)) {
            fireAndAdvance(level, entity, entity.getUsedItemHand(), stack);
        }
        else {
            entity.stopUsingItem();
            setPhase(stack, Phase.IDLE);
            clearLoadFlags(stack);
            resetBarrelSpins(stack);
        }

    }

    private void onShootRecoveryDone(Level level, LivingEntity entity, ItemStack stack, long now) {
        if (hasLoadableAmmo(entity, stack)) {
            startLoading(stack, now);
        }
        else {
            entity.stopUsingItem();
            setPhase(stack, Phase.IDLE);
            setCanLoad(stack, false);
            clearLoadFlags(stack);
            resetBarrelSpins(stack);
        }

    }

    private void startLoading(ItemStack stack, long now) {
        final int quickChargeLevel = getQuickChargeLevel(stack);
        beginLoad(stack, now, quickChargeLevel);
        setPhase(stack, Phase.LOADING);
        setNextActionTick(stack, now + chargeDurationTicks(quickChargeLevel));
        setCanLoad(stack, true);
        loadSoundPlayed = false;
    }

    private void fireAndAdvance(Level level, LivingEntity shooter, InteractionHand hand, ItemStack stack) {
        triggerShootAnimation(stack, shooter);
        BarrelCrossbowProjectiles.fireAllLoaded(level, shooter, hand, stack);

        incrementBarrelSpins(stack);

        stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        clearLoadFlags(stack);
        setPhase(stack, Phase.SHOOTING);
        setNextActionTick(stack, level.getGameTime() + SHOOT_RECOVERY_TICKS);
    }

    private boolean chargeFromInventory(Level level, LivingEntity entity, ItemStack stack) {
        if (isCharged(stack)) {
            return true;
        }

        if (BarrelCrossbowProjectiles.tryLoad(entity, stack)) {
            level.playSound(null, entity.blockPosition(), ClockworkSounds.CLOCKWORK_CROSSBOW_LOADING_END.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            return true;
        }

        return false;
    }

    private void tickLoadSound(Level level, LivingEntity entity, ItemStack stack, long now) {
        float progress = getLoadProgress(stack, now);

        if (progress < LOAD_SOUND_THRESHOLD) {
            loadSoundPlayed = false;
        }

        if (progress >= LOAD_SOUND_THRESHOLD && !loadSoundPlayed) {
            loadSoundPlayed = true;
            level.playSound(null, entity.blockPosition(),
                    ClockworkSounds.CLOCKWORK_BARREL_CROSSBOW_LOADING.get(), SoundSource.PLAYERS, 0.5f, 1.0f);
        }

    }

    private void abortUse(LivingEntity entity, ItemStack stack) {
        entity.stopUsingItem();
        setPhase(stack, Phase.IDLE);
        setCanLoad(stack, false);
        clearLoadFlags(stack);
        resetBarrelSpins(stack);
    }

    private void triggerShootAnimation(ItemStack stack, LivingEntity user) {
        if (user.level() instanceof ServerLevel) {
            triggerAnim(user, GeoItem.getId(stack), "fire", "shoot");
        }

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(
                new AnimationController<>(this, "state", 0, this::statePredicate)
                        .setSoundKeyframeHandler(event -> Clockwork.PROXY.playScopeCrossbowSound())
        );
        registrar.add(
                new AnimationController<>(this, "fire", 0, this::firePredicate)
                        .triggerableAnim("shoot", ANIM_SHOOT)
        );

    }

    private <T extends GeoAnimatable> PlayState statePredicate(AnimationState<T> event) {
        ItemStack stack = event.getData(DataTickets.ITEMSTACK);
        if (stack == null || stack.isEmpty()) {
            return PlayState.STOP;
        }

        if (resolveUser(event) == null) {
            return PlayState.STOP;
        }

        final Phase phase = getPhase(stack);
        if (isCharged(stack) || phase == Phase.LOADED) {
            return PlayState.CONTINUE;
        }

        if (phase == Phase.LOADING) {
            if (!StackNbt.tag(stack).getBoolean(NBT_LOAD_PLAYED)) {
                final int variant = getLoadVariant(stack);
                final float durationSec = chargeDurationTicks(variant) / 20f;
                event.getController().setAnimationSpeed(1.0f / Math.max(0.001f, durationSec));
                event.setAndContinue(ANIM_LOAD_VARIANTS[variant]);
                StackNbt.update(stack, tag -> tag.putBoolean(NBT_LOAD_PLAYED, true));
            }

            return PlayState.CONTINUE;
        }

        event.setAndContinue(ANIM_IDLE);

        return PlayState.CONTINUE;
    }

    private <T extends GeoAnimatable> PlayState firePredicate(AnimationState<T> event) {
        return PlayState.CONTINUE;
    }

    private static <T extends GeoAnimatable> LivingEntity resolveUser(AnimationState<T> event) {
        if (event.getData(DataTickets.ENTITY) instanceof LivingEntity entity) {
            return entity;
        }

        return Clockwork.PROXY.getClientPlayer();
    }

    @Override
    public double getBoneResetTime() {
        return 0;
    }

    public enum Phase {
        IDLE(0),
        LOADING(1),
        LOADED(2),
        SHOOTING(3);

        private final int id;

        Phase(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }

        public static Phase fromId(int id) {
            return switch (id) {
                case 1 -> LOADING;
                case 2 -> LOADED;
                case 3 -> SHOOTING;
                default -> IDLE;
            };
        }

    }

}