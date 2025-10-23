package dev.xylonity.bonsai.clockwork.common.item;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.item.renderer.BarrelCrossbowRenderer;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.ClockworkArrowProjectile;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import dev.xylonity.bonsai.clockwork.registry.ClockworkSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.ForgeEventFactory;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BarrelCrossbow extends CrossbowItem implements GeoItem {

    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public static final String NBT_LOAD_START_TICK = "barrel_load_start";
    public static final String NBT_LOAD_VARIANT = "barrel_load_variant";
    public static final String NBT_LOAD_PLAYED = "barrel_load_played";
    // 0 IDLE, 1 LOADING, 2 LOADED, 3 SHOOTING
    public static final String NBT_ANIM_PHASE = "barrel_anim_phase";
    public static final String NBT_CAN_LOAD = "barrel_can_load";
    public static final String NBT_NEXT_ACTION_TICK= "barrel_next_action";

    private static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("shoot");
    private static final RawAnimation LOAD = RawAnimation.begin().thenPlay("load");
    private static final RawAnimation LOAD_QUICKCHARGE_1 = RawAnimation.begin().thenPlay("load_quickcharge_1");
    private static final RawAnimation LOAD_QUICKCHARGE_2 = RawAnimation.begin().thenPlay("load_quickcharge_2");
    private static final RawAnimation LOAD_QUICKCHARGE_3 = RawAnimation.begin().thenPlay("load_quickcharge_3");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private static final float DURATION_QUICKCHARGE_0 = 0.50f;
    private static final float DURATION_QUICKCHARGE_1 = 0.46f;
    private static final float DURATION_QUICKCHARGE_2 = 0.42f;
    private static final float DURATION_QUICKCHARGE_3 = 0.38f;

    private static final RawAnimation[] LOAD_VARIANTS = new RawAnimation[]{
            LOAD,
            LOAD_QUICKCHARGE_1,
            LOAD_QUICKCHARGE_2,
            LOAD_QUICKCHARGE_3
    };

    private static final float[] LOAD_DURATIONS = new float[] {
            DURATION_QUICKCHARGE_0,
            DURATION_QUICKCHARGE_1,
            DURATION_QUICKCHARGE_2,
            DURATION_QUICKCHARGE_3
    };

    private static final int PHASE_IDLE = 0;
    private static final int PHASE_LOADING = 1;
    private static final int PHASE_LOADED = 2;
    private static final int PHASE_SHOOTING = 3;

    private static final int ANIMATION_SHOOT_TICKS = 3;
    private static final float LOAD_SOUND_PROGRESS = 0.2f;

    private static final float VANILLA_ARROW_VEL = 3.15f;
    private static final float VANILLA_FIREWORK_VEL = 1.28f;

    private boolean cwLoadingPlayedStart = false;

    private static float baseVelocityFor(ItemStack stack) {
        return containsChargedProjectile(stack, Items.FIREWORK_ROCKET) ? VANILLA_FIREWORK_VEL : VANILLA_ARROW_VEL;
    }

    public BarrelCrossbow(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static int getQuickChargeLevel(ItemStack stack) {
        int quickCharge = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack);
        if (quickCharge < 0) {
            return 0;
        }

        return Math.min(quickCharge, 3);
    }

    public static int getChargeDurationTicks(ItemStack stack) {
        return Math.max(1, Math.round(LOAD_DURATIONS[getQuickChargeLevel(stack)] * 20f));
    }

    private static void setPhase(ItemStack stack, int phase) {
        stack.getOrCreateTag().putInt(NBT_ANIM_PHASE, phase);
    }
    private static int getPhase(ItemStack stack) {
        return stack.getOrCreateTag().getInt(NBT_ANIM_PHASE);
    }

    private static void setNextActionTick(ItemStack stack, long tick) {
        stack.getOrCreateTag().putLong(NBT_NEXT_ACTION_TICK, tick);
    }

    private static long getNextActionTick(ItemStack stack) {
        return stack.getOrCreateTag().getLong(NBT_NEXT_ACTION_TICK);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CROSSBOW;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private BarrelCrossbowRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new BarrelCrossbowRenderer();
                }

                return renderer;
            }

        });

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "state", 1, this::statePredicate)
                .setSoundKeyframeHandler(event -> Clockwork.PROXY.playScopeCrossbowSound()));
        registrar.add(new AnimationController<>(this, "fire", 1, this::firePredicate)
                .triggerableAnim("shoot", SHOOT));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Loops the interaction again when it ends
        if (isCharged(stack)) {
            if (!level.isClientSide) {
                triggerShoot(stack, player);

                performShootingCW(level, player, hand, stack, baseVelocityFor(stack), 1.0F);

                setCharged(stack, false);
                clearLoadFlags(stack);

                setPhase(stack, PHASE_SHOOTING);
                setNextActionTick(stack, level.getGameTime() + ANIMATION_SHOOT_TICKS);

                if (hasLoadableAmmo(player, stack)) {
                    player.startUsingItem(hand);
                }
                else {
                    setPhase(stack, PHASE_IDLE);
                }
            }

            return InteractionResultHolder.consume(stack);
        }

        // If there is no ammo
        if (!hasLoadableAmmo(player, stack)) {
            setPhase(stack, PHASE_IDLE);

            stack.getOrCreateTag().putBoolean(NBT_CAN_LOAD, false);

            return InteractionResultHolder.fail(stack);
        }

        // Loop
        if (!level.isClientSide) {
            long now = level.getGameTime();

            markLoadStart(stack, now, getQuickChargeLevel(stack));

            setPhase(stack, PHASE_LOADING);
            setNextActionTick(stack, now + getChargeDurationTicks(stack));

            cwLoadingPlayedStart = false;

            stack.getOrCreateTag().putBoolean(NBT_CAN_LOAD, true);
        }

        player.startUsingItem(hand);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide) return;

        int phase = getPhase(stack);
        long now = level.getGameTime();

        // If there is no ammo, and it's not loaded (which shouldn't be a case either)
        if (!hasLoadableAmmo(entity, stack) && !isCharged(stack) && phase != PHASE_SHOOTING) {
            entity.stopUsingItem();

            setPhase(stack, PHASE_IDLE);

            stack.getOrCreateTag().putBoolean(NBT_CAN_LOAD, false);

            clearLoadFlags(stack);

            return;
        }

        if (phase == PHASE_LOADING) {
            long start = stack.getOrCreateTag().getLong(NBT_LOAD_START_TICK);
            int chargeTicks = getChargeDurationTicks(stack);

            float progress = Math.max(0f, Math.min(1f, (now - start) / (float) chargeTicks));
            if (progress < LOAD_SOUND_PROGRESS) {
                cwLoadingPlayedStart = false;
            }

            if (progress >= LOAD_SOUND_PROGRESS && !cwLoadingPlayedStart) {
                cwLoadingPlayedStart = true;
                level.playSound(null, entity.blockPosition(), ClockworkSounds.CLOCKWORK_BARREL_CROSSBOW_LOADING.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
            }

        }

        long next = getNextActionTick(stack);

        switch (phase) {
            case PHASE_LOADING -> {
                if (now >= next) {
                    avoidVanillaSounds(level, entity, stack);

                    if (isCharged(stack)) {
                        triggerShoot(stack, entity);
                        performShootingCW(level, entity, entity.getUsedItemHand(), stack, baseVelocityFor(stack), 1.0F);

                        setCharged(stack, false);
                        clearLoadFlags(stack);

                        setPhase(stack, PHASE_SHOOTING);
                        setNextActionTick(stack, now + ANIMATION_SHOOT_TICKS);
                    }
                    else {
                        entity.stopUsingItem();

                        setPhase(stack, PHASE_IDLE);

                        clearLoadFlags(stack);
                    }

                }

            }
            case PHASE_LOADED -> {
                if (now >= next) {
                    triggerShoot(stack, entity);

                    performShootingCW(level, entity, entity.getUsedItemHand(), stack, baseVelocityFor(stack), 1.0F);

                    setCharged(stack, false);

                    clearLoadFlags(stack);

                    setPhase(stack, PHASE_SHOOTING);
                    setNextActionTick(stack, now + ANIMATION_SHOOT_TICKS);
                }
            }
            case PHASE_SHOOTING -> {
                if (now >= next) {
                    if (hasLoadableAmmo(entity, stack)) {
                        markLoadStart(stack, now, getQuickChargeLevel(stack));
                        setPhase(stack, PHASE_LOADING);

                        setNextActionTick(stack, now + getChargeDurationTicks(stack));

                        cwLoadingPlayedStart = false;

                        stack.getOrCreateTag().putBoolean(NBT_CAN_LOAD, true);
                    }
                    else {
                        entity.stopUsingItem();
                        setPhase(stack, PHASE_IDLE);
                        stack.getOrCreateTag().putBoolean(NBT_CAN_LOAD, false);
                        clearLoadFlags(stack);
                    }
                }

            }
            default -> {
                ;;
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        setPhase(stack, PHASE_IDLE);
        stack.getOrCreateTag().putBoolean(NBT_CAN_LOAD, false);
        clearLoadFlags(stack);
    }

    private <T extends GeoAnimatable> PlayState statePredicate(AnimationState<T> event) {
        ItemStack stack = event.getData(DataTickets.ITEMSTACK);
        if (stack == null || stack.isEmpty()) return PlayState.STOP;

        Entity entity = event.getData(DataTickets.ENTITY);
        LivingEntity user = (entity instanceof LivingEntity le) ? le : (Clockwork.PROXY.getClientPlayer() != null ? Clockwork.PROXY.getClientPlayer() : null);
        if (user == null) return PlayState.STOP;

        int phase = getPhase(stack);
        CompoundTag tag = stack.getOrCreateTag();
        int variant = clampVariant(stack);

        if (isCharged(stack) || phase == PHASE_LOADED) {
            return PlayState.CONTINUE;
        }

        if (phase == PHASE_LOADING) {
            if (!tag.getBoolean(NBT_LOAD_PLAYED)) {
                event.getController().setAnimationSpeed(speedForVariant(variant));

                event.setAndContinue(LOAD_VARIANTS[variant]);

                tag.putBoolean(NBT_LOAD_PLAYED, true);
            }

            return PlayState.CONTINUE;
        }

        event.setAndContinue(IDLE);

        return PlayState.CONTINUE;
    }

    private float speedForVariant(int variant) {
        return 1.0f / Math.max(0.001f, getChargeDurationSecondsFromTicksVariant(variant));
    }

    private static float getChargeDurationSecondsFromTicksVariant(int variant) {
        variant = clampVariant(variant);
        int ticks = Math.max(1, Math.round(LOAD_DURATIONS[variant] * 20f));
        return ticks / 20f;
    }

    private <T extends GeoAnimatable> PlayState firePredicate(AnimationState<T> e) {
        e.getController().setAnimationSpeed(1.0f);
        return PlayState.CONTINUE;
    }

    private void triggerShoot(ItemStack stack, LivingEntity user) {
        if (user.level() instanceof ServerLevel) {
            this.triggerAnim(user, GeoItem.getId(stack), "fire", "shoot");
        }

    }

    private static void avoidVanillaSounds(Level level, LivingEntity entity, ItemStack stack) {
        if (isCharged(stack)) return;
        if (tryLoadProjectiles(entity, stack)) {
            setCharged(stack, true);
            level.playSound(null, entity.blockPosition(), ClockworkSounds.CLOCKWORK_CROSSBOW_LOADING_END.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }

    }

    private static boolean hasLoadableAmmo(LivingEntity user, ItemStack crossbowStack) {
        if (user instanceof Player player && player.getAbilities().instabuild) {
            return true;
        }

        ItemStack ammo = user.getProjectile(crossbowStack);
        return !ammo.isEmpty() && ammo.getItem() instanceof ArrowItem || ammo.getItem() == Items.FIREWORK_ROCKET;
    }

    /**
     * Replicated vanilla's logic to avoid hardcoded sounds and arrows velocities
     */
    private static void performShootingCW(Level level, LivingEntity shooter, InteractionHand usedHand, ItemStack crossbowStack, float velocity, float inaccuracy) {
        if (shooter instanceof Player player) {
            if (ForgeEventFactory.onArrowLoose(crossbowStack, shooter.level(), player, 1, true) < 0) return;
        }

        List<ItemStack> list = getChargedProjectiles(crossbowStack);
        float[] pitches = getShotPitches(shooter.getRandom());

        for (int i = 0; i < list.size(); ++i) {
            ItemStack ammo = list.get(i);
            boolean creative = shooter instanceof Player && ((Player)shooter).getAbilities().instabuild;
            if (!ammo.isEmpty()) {
                if (i == 0) shootProjectileCW(level, shooter, usedHand, crossbowStack, ammo, pitches[i], creative, velocity * 0.7f, inaccuracy, 0.0F);
                else if (i == 1) shootProjectileCW(level, shooter, usedHand, crossbowStack, ammo, pitches[i], creative, velocity * 0.7f, inaccuracy, -10.0F);
                else if (i == 2) shootProjectileCW(level, shooter, usedHand, crossbowStack, ammo, pitches[i], creative, velocity * 0.7f, inaccuracy, 10.0F);
            }
        }

        onCrossbowShotCW(crossbowStack);
    }

    /**
     * Replicated vanilla's logic to avoid hardcoded sounds and arrows velocities
     */
    private static void shootProjectileCW(Level level, LivingEntity shooter, InteractionHand hand, ItemStack crossbowStack, ItemStack ammoStack, float soundPitch, boolean creativeOnlyPickup, float velocity, float inaccuracy, float angleDeg) {
        if (level.isClientSide) return;

        Projectile proj;
        if (ammoStack.is(Items.FIREWORK_ROCKET)) {
            proj = new FireworkRocketEntity(level, ammoStack, shooter, shooter.getX(), shooter.getEyeY() - 0.15000000596, shooter.getZ(), true);
        }
        else {
            ArrowItem arrowItem = (ArrowItem) (ammoStack.getItem() instanceof ArrowItem ? ammoStack.getItem() : Items.ARROW);
            AbstractArrow arrow = arrowItem.createArrow(level, ammoStack, shooter);

            if (shooter instanceof Player) {
                arrow.setCritArrow(true);
            }

            arrow.setShotFromCrossbow(true);

            int pierce = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, crossbowStack);
            if (pierce > 0) {
                arrow.setPierceLevel((byte) pierce);
            }

            if (creativeOnlyPickup || angleDeg != 0.0F) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            if (arrow instanceof ClockworkArrowProjectile clockworkArrow) {
                LivingEntity target = ScopeCrossbow.searchForwardTarget(level, shooter, 128);
                if (target != null) clockworkArrow.setTarget(target);
            }

            proj = arrow;
        }

        if (shooter instanceof CrossbowAttackMob mob) {
            mob.shootCrossbowProjectile(mob.getTarget(), crossbowStack, proj, angleDeg);
        }
        else {
            Vec3 up = shooter.getUpVector(1.0F);
            Vec3 look = shooter.getViewVector(1.0F);
            Vector3f dir = look.toVector3f().rotate(new Quaternionf().setAngleAxis(angleDeg * 0.017453292F, up.x, up.y, up.z));
            proj.shoot(dir.x(), dir.y(), dir.z(), velocity, inaccuracy);
        }

        crossbowStack.hurtAndBreak(ammoStack.is(Items.FIREWORK_ROCKET) ? 3 : 1, shooter, b -> b.broadcastBreakEvent(hand));
        level.addFreshEntity(proj);

        level.playSound(null, shooter.blockPosition(), ClockworkSounds.CLOCKWORK_CROSSBOW_SHOOT.get(), SoundSource.PLAYERS, 1.0F, soundPitch);
    }

    private static void onCrossbowShotCW(ItemStack crossbowStack) {
        clearChargedProjectiles(crossbowStack);
    }

    private static List<ItemStack> getChargedProjectiles(ItemStack crossbowStack) {
        List<ItemStack> list = new ArrayList<>();
        CompoundTag tag = crossbowStack.getTag();
        if (tag != null && tag.contains("ChargedProjectiles", 9)) {
            ListTag listtag = tag.getList("ChargedProjectiles", 10);
            for (int i = 0; i < listtag.size(); ++i) {
                list.add(ItemStack.of(listtag.getCompound(i)));
            }

        }

        return list;
    }

    private static void clearChargedProjectiles(ItemStack crossbowStack) {
        CompoundTag tag = crossbowStack.getTag();
        if (tag != null) {
            ListTag list = tag.getList("ChargedProjectiles", 9);
            list.clear();
            tag.put("ChargedProjectiles", list);
        }

    }

    /**
     * Replicated vanilla's logic to avoid hardcoded sounds and arrows velocities
     */
    private static boolean tryLoadProjectiles(LivingEntity shooter, ItemStack crossbowStack) {
        int multishot = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, crossbowStack);
        int shots = multishot == 0 ? 1 : 3;

        boolean creative = shooter instanceof Player && ((Player) shooter).getAbilities().instabuild;
        ItemStack ammo = shooter.getProjectile(crossbowStack);
        ItemStack ammoCopy = ammo.copy();

        for (int k = 0; k < shots; ++k) {
            if (k > 0) {
                ammo = ammoCopy.copy();
            }

            if (ammo.isEmpty() && creative) {
                ammo = new ItemStack(Items.ARROW);
                ammoCopy = ammo.copy();
            }

            if (!loadProjectile(shooter, crossbowStack, ammo, k > 0, creative)) {
                return false;
            }

        }

        return true;
    }

    /**
     * Replicated vanilla's logic to avoid hardcoded sounds and arrows velocities
     */
    private static boolean loadProjectile(LivingEntity shooter, ItemStack crossbowStack, ItemStack ammo, boolean hasAmmo, boolean creative) {
        if (ammo.isEmpty()) return false;

        boolean flag = creative && ammo.getItem() instanceof ArrowItem;
        ItemStack toAdd;
        if (!flag && !creative && !hasAmmo) {
            toAdd = ammo.split(1);
            if (ammo.isEmpty() && shooter instanceof Player p) {
                p.getInventory().removeItem(ammo);
            }
        }
        else {
            toAdd = ammo.copy();
        }

        addChargedProjectile(crossbowStack, toAdd);

        return true;
    }

    private static void addChargedProjectile(ItemStack crossbowStack, ItemStack ammoStack) {
        CompoundTag tag = crossbowStack.getOrCreateTag();
        ListTag list = tag.contains("ChargedProjectiles", 9) ? tag.getList("ChargedProjectiles", 10) : new ListTag();
        CompoundTag entry = new CompoundTag();
        ammoStack.save(entry);
        list.add(entry);
        tag.put("ChargedProjectiles", list);
    }

    private static void markLoadStart(ItemStack stack, long tick, int variant) {
        var tag = stack.getOrCreateTag();
        tag.putLong(NBT_LOAD_START_TICK, tick);
        tag.putInt(NBT_LOAD_VARIANT, clampVariant(variant));
        tag.putBoolean(NBT_LOAD_PLAYED, false);
    }

    private static void clearLoadFlags(ItemStack stack) {
        if (!stack.hasTag()) return;
        var tag = stack.getOrCreateTag();
        tag.remove(NBT_LOAD_START_TICK);
        tag.remove(NBT_LOAD_VARIANT);
        tag.remove(NBT_LOAD_PLAYED);
    }

    private static int clampVariant(int v) {
        return (v < 0) ? 0 : Math.min(v, 3);
    }

    private static int clampVariant(ItemStack v) {
        return v.getEnchantmentLevel(Enchantments.QUICK_CHARGE);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return s -> s.getItem() instanceof ArrowItem || s.is(Items.FIREWORK_ROCKET);
    }

    @Override
    public double getBoneResetTime() {
        return 0;
    }

    private static float[] getShotPitches(RandomSource random) {
        boolean flag = random.nextBoolean();
        return new float[]{1.0F, getRandomShotPitch(flag, random), getRandomShotPitch(!flag, random)};
    }

    private static float getRandomShotPitch(boolean high, RandomSource random) {
        float f = high ? 0.63F : 0.43F;
        return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + f;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.getItem() == ClockworkItems.CLOCKWORK_GEAR.get();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

}