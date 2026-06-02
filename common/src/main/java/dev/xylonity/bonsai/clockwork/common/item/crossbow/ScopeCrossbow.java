package dev.xylonity.bonsai.clockwork.common.item.crossbow;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.item.renderer.ScopeCrossbowRenderer;
import dev.xylonity.bonsai.clockwork.common.entity.projectile.ClockworkArrowProjectile;
import dev.xylonity.bonsai.clockwork.common.item.gecko.GeckoCrossbowItem;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import dev.xylonity.bonsai.clockwork.registry.ClockworkSounds;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ScopeCrossbow extends GeckoCrossbowItem {

    private static final String NBT_LOAD_START_TICK = "sc_load_start";
    private static final String NBT_LOAD_VARIANT = "sc_load_variant";
    private static final String NBT_LOAD_PLAYED = "sc_load_played";
    private static final String NBT_SCOPING = "sc_scoping";
    private static final String NBT_SCOPE_HAND = "sc_scope_hand";
    public  static final String NBT_CAN_LOAD = "sc_can_load";

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ANIM_SHOOT = RawAnimation.begin().thenPlay("shoot");
    private static final RawAnimation ANIM_LOADED = RawAnimation.begin().thenPlay("loaded");
    private static final RawAnimation[] ANIM_LOAD_VARIANTS = {
            RawAnimation.begin().thenPlay("load"),
            RawAnimation.begin().thenPlay("load_quickcharge_1"),
            RawAnimation.begin().thenPlay("load_quickcharge_2"),
            RawAnimation.begin().thenPlay("load_quickcharge_3"),
    };

    private static final float[] LOAD_DURATIONS_SEC = { 1.50f, 1.29f, 1.08f, 0.88f };
    private static final float LOAD_SOUND_THRESHOLD = 0.2f;

    private static final float ARROW_BASE_VELOCITY = 3.15f;
    private static final float FIREWORK_BASE_VELOCITY = 2.5f;
    private static final float VELOCITY_MULTIPLIER = 1.55f;

    private static final double ARROW_MAX_DISTANCE = 128.0;
    private static final float SEARCH_RADIUS = 3.5f;

    private boolean loadSoundPlayed = false;

    public ScopeCrossbow(Properties props) {
        super(props);
    }

    @Override
    protected Object createGeckoRenderer() {
        return new ScopeCrossbowRenderer();
    }

    public static int clampVariant(int v) {
        return Math.max(0, Math.min(v, ANIM_LOAD_VARIANTS.length - 1));
    }

    public static int getQuickChargeLevel(ItemStack stack) {
        final int raw = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack);
        return clampVariant(Math.max(0, raw));
    }

    public static int getChargeDurationTicks(ItemStack stack) {
        return chargeDurationTicks(getQuickChargeLevel(stack));
    }

    private static int chargeDurationTicks(int quickChargeLevel) {
        return Math.max(1, Math.round(LOAD_DURATIONS_SEC[clampVariant(quickChargeLevel)] * 20f));
    }

    private static float baseVelocityFor(ItemStack stack) {
        return containsProjectileType(stack, Items.FIREWORK_ROCKET) ? FIREWORK_BASE_VELOCITY : ARROW_BASE_VELOCITY;
    }

    private static boolean isScoping(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(NBT_SCOPING);
    }

    private static InteractionHand getScopeHand(ItemStack stack) {
        if (!stack.hasTag()) {
            return InteractionHand.MAIN_HAND;
        }

        return stack.getTag().getByte(NBT_SCOPE_HAND) == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private static void beginScoping(ItemStack stack, InteractionHand hand) {
        final CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(NBT_SCOPING, true);
        tag.putByte(NBT_SCOPE_HAND, (byte) (hand == InteractionHand.MAIN_HAND ? 0 : 1));
    }

    private static void clearScoping(ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }

        final CompoundTag tag = stack.getOrCreateTag();
        tag.remove(NBT_SCOPING);
        tag.remove(NBT_SCOPE_HAND);
    }

    private static void beginLoad(ItemStack stack, long tick, int quickChargeLevel) {
        final CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(NBT_LOAD_START_TICK, tick);
        tag.putInt(NBT_LOAD_VARIANT, clampVariant(quickChargeLevel));
        tag.putBoolean(NBT_LOAD_PLAYED, false);
    }

    private static void clearLoadFlags(ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }

        final CompoundTag tag = stack.getOrCreateTag();
        tag.remove(NBT_LOAD_START_TICK);
        tag.remove(NBT_LOAD_VARIANT);
        tag.remove(NBT_LOAD_PLAYED);
    }

    private static void setCanLoad(ItemStack stack, boolean canLoad) {
        stack.getOrCreateTag().putBoolean(NBT_CAN_LOAD, canLoad);
    }

    public static int getLoadVariant(ItemStack stack) {
        return clampVariant(stack.getOrCreateTag().getInt(NBT_LOAD_VARIANT));
    }

    private static boolean hasLoadableAmmo(LivingEntity shooter, ItemStack crossbow) {
        if (shooter instanceof Player player && player.getAbilities().instabuild) {
            return true;
        }

        final ItemStack ammo = shooter.getProjectile(crossbow);
        return !ammo.isEmpty() && (ammo.getItem() instanceof ArrowItem || ammo.is(Items.FIREWORK_ROCKET));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return isScoping(stack) ? UseAnim.SPYGLASS : UseAnim.CROSSBOW;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return itemStack -> itemStack.getItem() instanceof ArrowItem || itemStack.is(Items.FIREWORK_ROCKET);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(ClockworkItems.CLOCKWORK_GEAR.get());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (isCharged(stack)) {
            beginScoping(stack, hand);
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        if (!hasLoadableAmmo(player, stack)) {
            setCanLoad(stack, false);
            return InteractionResultHolder.fail(stack);
        }

        beginLoad(stack, level.getGameTime(), getQuickChargeLevel(stack));
        setCanLoad(stack, true);
        loadSoundPlayed = false;

        player.startUsingItem(hand);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingDuration) {
        if (level.isClientSide) {
            return;
        }

        if (!isCharged(stack) && !hasLoadableAmmo(entity, stack)) {
            abortUse(entity, stack);
            return;
        }

        tickLoadSound(level, entity, stack, remainingDuration);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        if (!level.isClientSide && isScoping(stack) && isCharged(stack)) {
            fireScopedShot(level, user, stack);
            return;
        }

        final int usedTicks = getUseDuration(stack) - timeLeft;
        if (usedTicks >= getChargeDurationTicks(stack) && !isCharged(stack)) {
            if (tryLoadProjectiles(user, stack)) {
                setCharged(stack, true);
            }

        }

        clearScoping(stack);
        clearLoadFlags(stack);
        setCanLoad(stack, false);
    }

    private void fireScopedShot(Level level, LivingEntity user, ItemStack stack) {
        triggerShootAnimation(stack, user);

        final InteractionHand hand = getScopeHand(stack);
        final float velocity = baseVelocityFor(stack);
        fireAllLoaded(level, user, hand, stack, velocity, 0.0f);

        setCharged(stack, false);
        clearScoping(stack);
        clearLoadFlags(stack);
        setCanLoad(stack, false);
    }

    private void tickLoadSound(Level level, LivingEntity entity, ItemStack stack, int remainingDuration) {
        final int chargeTicks = getChargeDurationTicks(stack);
        final int usedTicks = stack.getUseDuration() - remainingDuration;
        final float progress = usedTicks / (float) chargeTicks;

        if (progress < LOAD_SOUND_THRESHOLD) {
            loadSoundPlayed = false;
        }
        if (progress >= LOAD_SOUND_THRESHOLD && !loadSoundPlayed) {
            loadSoundPlayed = true;
            level.playSound(null, entity.blockPosition(), ClockworkSounds.CLOCKWORK_SCOPE_CROSSBOW_LOADING.get(), SoundSource.PLAYERS, 0.5f, 1.0f);
        }

    }

    private void abortUse(LivingEntity entity, ItemStack stack) {
        entity.stopUsingItem();
        clearLoadFlags(stack);
        setCanLoad(stack, false);
        loadSoundPlayed = false;
    }

    private void triggerShootAnimation(ItemStack stack, LivingEntity user) {
        if (user.level() instanceof ServerLevel) {
            triggerAnim(user, GeoItem.getId(stack), "fire", "shoot");
        }

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(
                new AnimationController<>(this, "state", 1, this::statePredicate)
                        .setSoundKeyframeHandler(event -> Clockwork.PROXY.playScopeCrossbowSound())
        );
        registrar.add(
                new AnimationController<>(this, "fire", 1, this::firePredicate)
                        .triggerableAnim("shoot", ANIM_SHOOT)
        );

    }

    private <T extends GeoAnimatable> PlayState statePredicate(AnimationState<T> event) {
        final ItemStack stack = event.getData(DataTickets.ITEMSTACK);
        if (stack == null || stack.isEmpty()) {
            return PlayState.STOP;
        }
        if (resolveUser(event) == null) {
            return PlayState.STOP;
        }

        final CompoundTag tag = stack.getOrCreateTag();
        if (isCharged(stack)) {
            event.setAnimation(ANIM_LOADED);
            if (tag.getLong(NBT_LOAD_START_TICK) > 0L) {
                clearLoadFlags(stack);
            }

            return PlayState.CONTINUE;
        }

        if (isCurrentlyLoading(stack, event)) {
            if (!tag.getBoolean(NBT_LOAD_PLAYED)) {
                final int variant = getLoadVariant(stack);
                final float durationSec = chargeDurationTicks(variant) / 20f;
                event.getController().setAnimationSpeed(1.0f / Math.max(0.001f, durationSec));
                event.setAndContinue(ANIM_LOAD_VARIANTS[variant]);
                tag.putBoolean(NBT_LOAD_PLAYED, true);
            }

            return PlayState.CONTINUE;
        }

        event.getController().setAnimationSpeed(1f);
        event.setAndContinue(ANIM_IDLE);

        return PlayState.CONTINUE;
    }

    private <T extends GeoAnimatable> boolean isCurrentlyLoading(ItemStack stack, AnimationState<T> event) {
        final CompoundTag tag = stack.getOrCreateTag();
        final boolean canLoad = tag.getBoolean(NBT_CAN_LOAD) || hasLoadableAmmo(resolveUser(event), stack);
        final boolean hasLoadStart = tag.getLong(NBT_LOAD_START_TICK) > 0L;

        final LivingEntity user = resolveUser(event);
        final boolean isUsingThisItem = user != null && user.isUsingItem() && user.getUseItem() != null && user.getUseItem().getItem() == stack.getItem();

        return canLoad && (isUsingThisItem || hasLoadStart);
    }

    private <T extends GeoAnimatable> PlayState firePredicate(AnimationState<T> event) {
        event.getController().setAnimationSpeed(1.0f);
        return PlayState.CONTINUE;
    }

    private static <T extends GeoAnimatable> LivingEntity resolveUser(AnimationState<T> event) {
        final Entity entity = event.getData(DataTickets.ENTITY);
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        }

        return Clockwork.PROXY.getClientPlayer();
    }

    private static void fireAllLoaded(Level level, LivingEntity shooter, InteractionHand hand, ItemStack crossbow, float velocity, float inaccuracy) {
        if (level.isClientSide) {
            return;
        }

        //if (shooter instanceof Player player) {
        //    if (ForgeEventFactory.onArrowLoose(crossbow, level, player, 1, true) < 0) return;
        //}

        final List<ItemStack> charged = getChargedProjectiles(crossbow);
        final float[] pitches = randomShotPitches(shooter.getRandom());
        for (int i = 0; i < charged.size(); i++) {
            if (charged.get(i).isEmpty()) {
                continue;
            }

            final float degrees = switch (i) {
                case 1 -> -10.0f;
                case 2 -> 10.0f;
                default -> 0.0f;
            };

            final boolean creative = shooter instanceof Player player && player.getAbilities().instabuild;
            shootSingle(level, shooter, hand, crossbow, charged.get(i), pitches[i], creative, velocity * VELOCITY_MULTIPLIER, inaccuracy, degrees);
        }

        awardShotStats(level, shooter, crossbow);
        clearChargedProjectiles(crossbow);
    }

    private static void shootSingle(Level level, LivingEntity shooter, InteractionHand hand, ItemStack crossbow, ItemStack ammo, float soundPitch, boolean creative, float velocity, float inaccuracy, float angleDeg) {
        if (level.isClientSide) {
            return;
        }

        final Projectile projectile = createProjectile(level, shooter, crossbow, ammo, creative, angleDeg);
        launchProjectile(projectile, shooter, crossbow, angleDeg, velocity, inaccuracy);

        final int cost = ammo.is(Items.FIREWORK_ROCKET) ? 3 : 1;
        crossbow.hurtAndBreak(cost, shooter, e -> e.broadcastBreakEvent(hand));

        level.addFreshEntity(projectile);
        level.playSound(null, shooter.blockPosition(), ClockworkSounds.CLOCKWORK_CROSSBOW_SHOOT.get(), SoundSource.PLAYERS, 1.0f, soundPitch);
    }

    private static Projectile createProjectile(Level level, LivingEntity shooter, ItemStack crossbow, ItemStack ammo, boolean creative, float angleDeg) {
        if (ammo.is(Items.FIREWORK_ROCKET)) {
            return new FireworkRocketEntity(level, ammo, shooter, shooter.getX(), shooter.getEyeY() - 0.15, shooter.getZ(), true);
        }

        final ArrowItem arrowItem = (ammo.getItem() instanceof ArrowItem arrowItem1) ? arrowItem1 : (ArrowItem) Items.ARROW;
        final AbstractArrow arrow = arrowItem.createArrow(level, ammo, shooter);

        if (shooter instanceof Player) {
            arrow.setCritArrow(true);
        }
        arrow.setShotFromCrossbow(true);

        final int pierce = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, crossbow);
        if (pierce > 0) {
            arrow.setPierceLevel((byte) pierce);
        }

        if (creative || angleDeg != 0.0f) {
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }

        if (arrow instanceof ClockworkArrowProjectile clockworkArrowProjectile) {
            final LivingEntity target = searchForwardTarget(level, shooter, ARROW_MAX_DISTANCE);
            if (target != null) {
                clockworkArrowProjectile.setTarget(target);
            }

        }

        return arrow;
    }

    private static void launchProjectile(Projectile projectile, LivingEntity shooter, ItemStack crossbow, float angleDeg, float velocity, float inaccuracy) {
        if (shooter instanceof CrossbowAttackMob crossbowAttackMob) {
            crossbowAttackMob.shootCrossbowProjectile(crossbowAttackMob.getTarget(), crossbow, projectile, angleDeg);
        }
        else {
            Vec3 up = shooter.getUpVector(1.0f);
            Vec3 look = shooter.getViewVector(1.0f);
            Vector3f direction = look.toVector3f().rotate(new Quaternionf().setAngleAxis(angleDeg * 0.017453292f, up.x, up.y, up.z));
            projectile.shoot(direction.x(), direction.y(), direction.z(), velocity, inaccuracy);
        }

    }

    private static void awardShotStats(Level level, LivingEntity shooter, ItemStack crossbow) {
        if (shooter instanceof ServerPlayer player && !level.isClientSide) {
            CriteriaTriggers.SHOT_CROSSBOW.trigger(player, crossbow);
            player.awardStat(Stats.ITEM_USED.get(crossbow.getItem()));
        }

    }

    private static boolean tryLoadProjectiles(LivingEntity shooter, ItemStack crossbow) {
        final int multishotLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, crossbow);
        final int shotCount = (multishotLevel == 0) ? 1 : 3;
        final boolean creative = shooter instanceof Player p && p.getAbilities().instabuild;

        ItemStack ammo = shooter.getProjectile(crossbow);
        ItemStack ammoCopy = ammo.copy();
        for (int i = 0; i < shotCount; i++) {
            if (i > 0) {
                ammo = ammoCopy.copy();
            }
            if (ammo.isEmpty() && creative) {
                ammo = new ItemStack(Items.ARROW);
                ammoCopy = ammo.copy();
            }

            if (!loadSingle(shooter, crossbow, ammo, i > 0, creative)) {
                return false;
            }

        }

        return true;
    }

    private static boolean loadSingle(LivingEntity shooter, ItemStack crossbow, ItemStack ammo, boolean isExtra, boolean creative) {
        if (ammo.isEmpty()) {
            return false;
        }

        final boolean infiniteArrow = creative && ammo.getItem() instanceof ArrowItem;
        ItemStack toAdd;

        if (!infiniteArrow && !creative && !isExtra) {
            toAdd = ammo.split(1);
            if (ammo.isEmpty() && shooter instanceof Player player) {
                player.getInventory().removeItem(ammo);
            }

        }
        else {
            toAdd = ammo.copy();
        }

        addChargedProjectile(crossbow, toAdd);

        return true;
    }

    private static List<ItemStack> getChargedProjectiles(ItemStack crossbow) {
        final List<ItemStack> result = new ArrayList<>();
        final CompoundTag tag = crossbow.getTag();
        if (tag != null && tag.contains("ChargedProjectiles", 9)) {
            final ListTag list = tag.getList("ChargedProjectiles", 10);
            for (int i = 0; i < list.size(); i++) {
                result.add(ItemStack.of(list.getCompound(i)));
            }

        }

        return result;
    }

    private static void clearChargedProjectiles(ItemStack crossbow) {
        final CompoundTag tag = crossbow.getTag();
        if (tag != null) {
            final ListTag list = tag.getList("ChargedProjectiles", 9);
            list.clear();
            tag.put("ChargedProjectiles", list);
        }

    }

    private static void addChargedProjectile(ItemStack crossbow, ItemStack ammo) {
        final CompoundTag tag = crossbow.getOrCreateTag();
        final ListTag list = tag.contains("ChargedProjectiles", 9) ? tag.getList("ChargedProjectiles", 10) : new ListTag();
        final CompoundTag entry = new CompoundTag();
        ammo.save(entry);
        list.add(entry);
        tag.put("ChargedProjectiles", list);
    }

    private static boolean containsProjectileType(ItemStack crossbow, Item item) {
        return getChargedProjectiles(crossbow).stream().anyMatch(s -> s.is(item));
    }

    private static float[] randomShotPitches(RandomSource random) {
        final boolean flag = random.nextBoolean();
        return new float[] { 1f, pitchVariant(flag, random), pitchVariant(!flag, random) };
    }

    private static float pitchVariant(boolean high, RandomSource random) {
        return 1f / (random.nextFloat() * 0.5f + 1.8f) + (high ? 0.63f : 0.43f);
    }

    public static LivingEntity searchForwardTarget(Level level, LivingEntity shooter, double maxDistance) {
        final Vec3 start = shooter.getEyePosition();
        Vec3 end = start.add(shooter.getViewVector(1f).scale(maxDistance));

        final HitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        final List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(start, end).inflate(SEARCH_RADIUS),
                entity -> isValidTarget(entity, shooter)
        );

        if (entities.isEmpty()) {
            return null;
        }

        return findClosestToTrajectory(entities, start, end);
    }

    private static boolean isValidTarget(LivingEntity entity, LivingEntity shooter) {
        if (!entity.isAlive() || entity == shooter) {
            return false;
        }
        if (!entity.isPickable() || entity.isSpectator()) {
            return false;
        }
        if (entity instanceof Player player && (player.getAbilities().invulnerable || player.isCreative())) {
            return false;
        }

        return !entity.isAlliedTo(shooter);
    }

    private static LivingEntity findClosestToTrajectory(List<LivingEntity> candidates, Vec3 start, Vec3 end) {
        final Vec3 trajectory = end.subtract(start);
        final double trajectoryLengthSqr = trajectory.lengthSqr();

        LivingEntity best = null;
        double bestDistSqr = Double.MAX_VALUE;
        double bestProjDistSqr = Double.MAX_VALUE;

        for (final LivingEntity target : candidates) {
            final Vec3 center = target.getBoundingBox().getCenter();
            final double targetCenter = (trajectoryLengthSqr <= 1e-9) ? 0.0 : Mth.clamp(center.subtract(start).dot(trajectory) / trajectoryLengthSqr, 0.0, 1.0);

            final Vec3 closestOnLine = start.add(trajectory.scale(targetCenter));
            final double distanceSqr = center.distanceToSqr(closestOnLine);

            if (distanceSqr > SEARCH_RADIUS * SEARCH_RADIUS) {
                continue;
            }

            final double projectileDistanceSqr = start.distanceToSqr(closestOnLine);
            final boolean closer = distanceSqr < bestDistSqr || (Math.abs(distanceSqr - bestDistSqr) < 1e-9 && projectileDistanceSqr < bestProjDistSqr);
            if (closer) {
                bestDistSqr = distanceSqr;
                bestProjDistSqr = projectileDistanceSqr;
                best = target;
            }

        }

        return best;
    }

}
