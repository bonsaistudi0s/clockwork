package dev.xylonity.bonsai.clockwork.common.item;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.item.renderer.ScopeCrossbowRenderer;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.ClockworkArrowProjectile;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import dev.xylonity.bonsai.clockwork.registry.ClockworkSounds;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
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

public class ScopeCrossbow extends CrossbowItem implements GeoItem {

    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    private static final String NBT_LOAD_START_TICK = "bc_load_start";
    private static final String NBT_LOAD_VARIANT = "bc_load_variant";
    private static final String NBT_LOAD_PLAYED = "bc_load_played";
    private static final String NBT_SCOPING = "bc_scoping";
    private static final String NBT_SCOPE_HAND = "bc_scope_hand";
    private static final String NBT_CAN_LOAD = "bc_can_load";

    private static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("shoot");
    private static final RawAnimation LOAD = RawAnimation.begin().thenPlay("load");
    private static final RawAnimation LOADED = RawAnimation.begin().thenPlay("loaded");
    private static final RawAnimation LOAD_QUICKCHARGE_1 = RawAnimation.begin().thenPlay("load_quickcharge_1");
    private static final RawAnimation LOAD_QUICKCHARGE_2 = RawAnimation.begin().thenPlay("load_quickcharge_2");
    private static final RawAnimation LOAD_QUICKCHARGE_3 = RawAnimation.begin().thenPlay("load_quickcharge_3");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private static final float DURATION = 1.50f;
    private static final float DURATION_QUICKCHARGE_1 = 1.29f;
    private static final float DURATION_QUICKCHARGE_2 = 1.08f;
    private static final float DURATION_QUICKCHARGE_3 = 0.88f;

    private static final RawAnimation[] LOAD_VARIANTS = new RawAnimation[]{
            LOAD,
            LOAD_QUICKCHARGE_1,
            LOAD_QUICKCHARGE_2,
            LOAD_QUICKCHARGE_3
    };
    private static final float[] LOAD_DURATIONS = new float[] {
            DURATION,
            DURATION_QUICKCHARGE_1,
            DURATION_QUICKCHARGE_2,
            DURATION_QUICKCHARGE_3
    };

    private boolean cwLoadingPlayedStart = false;

    public ScopeCrossbow(Properties props) {
        super(props);
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

    private static float secondsFromVariantTicks(int variant) {
        int ticks = Math.max(1, Math.round(LOAD_DURATIONS[clampVariant(variant)] * 20f));
        return ticks / 20f;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return (stack.hasTag() && stack.getTag().getBoolean(NBT_SCOPING)) ? UseAnim.SPYGLASS : UseAnim.CROSSBOW;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private ScopeCrossbowRenderer renderer;

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new ScopeCrossbowRenderer();
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

        if (!isCharged(stack)) {
            if (!hasLoadableAmmo(player, stack)) {
                stack.getOrCreateTag().putBoolean(NBT_CAN_LOAD, false);
                return InteractionResultHolder.fail(stack);
            }

            markLoadStart(stack, level.getGameTime(), getQuickChargeLevel(stack));

            cwLoadingPlayedStart = false;

            stack.getOrCreateTag().putBoolean(NBT_CAN_LOAD, true);

            player.startUsingItem(hand);

            return InteractionResultHolder.consume(stack);
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(NBT_SCOPING, true);
        tag.putByte(NBT_SCOPE_HAND, (byte)(hand == InteractionHand.MAIN_HAND ? 0 : 1));

        player.startUsingItem(hand);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        boolean scoping = stack.hasTag() && stack.getTag().getBoolean(NBT_SCOPING);
        boolean charged = isCharged(stack);

        if (!level.isClientSide && scoping && charged) {

            triggerFire(stack, user);

            performShootingCW(level, user, (stack.hasTag() && stack.getTag().getByte(NBT_SCOPE_HAND) == 1) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, stack, customContainsChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 2.5F : 3.15F, 0.0F);

            setCharged(stack, false);
            clearScoping(stack);
            clearLoadFlags(stack);

            stack.getOrCreateTag().putBoolean(NBT_CAN_LOAD, false);

            return;
        }

        int used = this.getUseDuration(stack) - timeLeft;
        int chargeTicks = getChargeDurationTicks(stack);
        if (used >= chargeTicks && !isCharged(stack) && tryLoadProjectiles(user, stack)) {
            setCharged(stack, true);
        }

        clearScoping(stack);
        clearLoadFlags(stack);

        stack.getOrCreateTag().putBoolean(NBT_CAN_LOAD, false);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (level.isClientSide) return;

        if (!isCharged(stack) && !hasLoadableAmmo(entity, stack)) {
            entity.stopUsingItem();

            clearLoadFlags(stack);

            stack.getOrCreateTag().putBoolean(NBT_CAN_LOAD, false);

            cwLoadingPlayedStart = false;

            return;
        }

        int chargeTicks = getChargeDurationTicks(stack);
        int usedTicks = stack.getUseDuration() - count;
        float progress = usedTicks / (float)chargeTicks;

        if (progress < 0.2F) cwLoadingPlayedStart = false;
        if (progress >= 0.2F && !cwLoadingPlayedStart) {
            cwLoadingPlayedStart = true;
            level.playSound(null, entity.blockPosition(), ClockworkSounds.CLOCKWORK_SCOPE_CROSSBOW_LOADING.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
        }

    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return s -> s.getItem() instanceof ArrowItem || s.is(Items.FIREWORK_ROCKET);
    }

    private <T extends GeoAnimatable> PlayState statePredicate(AnimationState<T> event) {
        ItemStack stack = event.getData(DataTickets.ITEMSTACK);
        if (stack == null || stack.isEmpty()) return PlayState.STOP;

        Entity e = event.getData(DataTickets.ENTITY);
        LivingEntity entity = (e instanceof LivingEntity le) ? le : (Clockwork.PROXY.getClientPlayer() != null ? Clockwork.PROXY.getClientPlayer() : null);
        if (entity == null) return PlayState.STOP;

        boolean charged = isCharged(stack);

        CompoundTag tag = stack.getOrCreateTag();
        long start = tag.getLong(NBT_LOAD_START_TICK);
        int variant = clampVariant(tag.getInt(NBT_LOAD_VARIANT));
        boolean played = tag.getBoolean(NBT_LOAD_PLAYED);

        boolean canLoadNBT = tag.getBoolean(NBT_CAN_LOAD);
        boolean hasAmmoNow = hasLoadableAmmo(entity, stack);
        boolean canLoad = canLoadNBT || hasAmmoNow;

        if (charged) {
            event.setAnimation(LOADED);
            if (start > 0L) {
                clearLoadFlags(stack);
            }

            return PlayState.CONTINUE;
        }

        if (canLoad && ((!charged && (entity.isUsingItem() && entity.getUseItem() != null && entity.getUseItem().getItem() == stack.getItem())) || (start > 0L && !charged))) {
            if (!played) {
                event.getController().setAnimationSpeed(1.0f / Math.max(0.001f, secondsFromVariantTicks(variant)));
                event.setAndContinue(LOAD_VARIANTS[variant]);
                tag.putBoolean(NBT_LOAD_PLAYED, true);
            }

            return PlayState.CONTINUE;
        }

        event.getController().setAnimationSpeed(1f);
        event.setAndContinue(IDLE);

        return PlayState.CONTINUE;
    }

    private <T extends GeoAnimatable> PlayState firePredicate(AnimationState<T> e) {
        e.getController().setAnimationSpeed(1.0f);
        return PlayState.CONTINUE;
    }

    private void triggerFire(ItemStack stack, LivingEntity user) {
        if (user.level() instanceof ServerLevel) {
            this.triggerAnim(user, GeoItem.getId(stack), "fire", "shoot");
        }

    }

    private static void markLoadStart(ItemStack stack, long tick, int variant) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(NBT_LOAD_START_TICK, tick);
        tag.putInt(NBT_LOAD_VARIANT, clampVariant(variant));
        tag.putBoolean(NBT_LOAD_PLAYED, false);
    }

    private static void clearLoadFlags(ItemStack stack) {
        if (!stack.hasTag()) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(NBT_LOAD_START_TICK);
        tag.remove(NBT_LOAD_VARIANT);
        tag.remove(NBT_LOAD_PLAYED);
    }

    private static void clearScoping(ItemStack stack) {
        if (!stack.hasTag()) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(NBT_SCOPING);
        tag.remove(NBT_SCOPE_HAND);
    }

    private static int clampVariant(int v) {
        return (v < 0) ? 0 : Math.min(v, 3);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private static boolean hasLoadableAmmo(LivingEntity user, ItemStack crossbowStack) {
        if (user instanceof Player p && p.getAbilities().instabuild) {
            return true;
        }

        ItemStack ammo = user.getProjectile(crossbowStack);
        return !ammo.isEmpty() && ammo.getItem() instanceof ArrowItem || ammo.getItem() == Items.FIREWORK_ROCKET;
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

    private static boolean customContainsChargedProjectile(ItemStack crossbowStack, Item item) {
        return getChargedProjectiles(crossbowStack).stream().anyMatch(s -> s.is(item));
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
                if (i == 0) shootProjectileCW(level, shooter, usedHand, crossbowStack, ammo, pitches[i], creative, velocity * 1.55f, inaccuracy, 0.0F);
                else if (i == 1) shootProjectileCW(level, shooter, usedHand, crossbowStack, ammo, pitches[i], creative, velocity * 1.55f, inaccuracy, -10.0F);
                else if (i == 2) shootProjectileCW(level, shooter, usedHand, crossbowStack, ammo, pitches[i], creative, velocity * 1.55f, inaccuracy, 10.0F);
            }
        }

        onCrossbowShotCW(level, shooter, crossbowStack);
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
            ArrowItem arrowItem = (ArrowItem)(ammoStack.getItem() instanceof ArrowItem ? ammoStack.getItem() : Items.ARROW);
            AbstractArrow arrow = arrowItem.createArrow(level, ammoStack, shooter);
            if (shooter instanceof Player) {
                arrow.setCritArrow(true);
            }

            arrow.setShotFromCrossbow(true);
            int pierce = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, crossbowStack);
            if (pierce > 0) {
                arrow.setPierceLevel((byte)pierce);
            }

            if (creativeOnlyPickup || angleDeg != 0.0F) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            if (arrow instanceof ClockworkArrowProjectile cw) {
                LivingEntity target = searchForwardTarget(level, shooter, 128);
                if (target != null) {
                    cw.setTarget(target);
                }
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

    public static LivingEntity searchForwardTarget(Level level, LivingEntity shooter, double maxDistance) {
        float searchRadius = 3.5f;

        Vec3 start = shooter.getEyePosition();
        Vec3 end = start.add(shooter.getViewVector(1.0F).scale(maxDistance));

        HitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        List<LivingEntity> potentialTargets = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(start, end).inflate(searchRadius),
                entity -> entity.isAlive()
                        && entity != shooter
                        && entity.isPickable()
                        && !entity.isSpectator()
                        && !(entity instanceof Player player && (player.getAbilities().invulnerable || player.isCreative()))
                        && !entity.isAlliedTo(shooter)
        );

        if (potentialTargets.isEmpty()) {
            return null;
        }

        Vec3 trajectoryVector = end.subtract(start);
        double tLengthSqr = trajectoryVector.lengthSqr();
        LivingEntity closestTarget = null;
        double closestDistanceSquared = Double.MAX_VALUE;
        double closestProjectionDistance = Double.MAX_VALUE;

        for (LivingEntity target : potentialTargets) {
            Vec3 targetCenter = target.getBoundingBox().getCenter();

            // Projects target position onto trajectory line
            double factor = tLengthSqr <= 1e-9 ? 0.0 : Mth.clamp(targetCenter.subtract(start).dot(trajectoryVector) / tLengthSqr, 0.0, 1.0);

            Vec3 closestPoint = start.add(trajectoryVector.scale(factor));
            double distanceSquared = targetCenter.distanceToSqr(closestPoint);

            if (distanceSquared > searchRadius * searchRadius) continue;

            double pDistanceSquared = start.distanceToSqr(closestPoint);

            if (distanceSquared < closestDistanceSquared || (Math.abs(distanceSquared - closestDistanceSquared) < 1e-9 && pDistanceSquared < closestProjectionDistance)) {
                closestDistanceSquared = distanceSquared;
                closestProjectionDistance = pDistanceSquared;
                closestTarget = target;
            }

        }

        return closestTarget;
    }

    private static void onCrossbowShotCW(Level level, LivingEntity entity, ItemStack crossbowStack) {
        if (entity instanceof ServerPlayer player) {
            if (!level.isClientSide) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger(player, crossbowStack);
            }

            player.awardStat(Stats.ITEM_USED.get(crossbowStack.getItem()));
        }

        clearChargedProjectiles(crossbowStack);
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

}