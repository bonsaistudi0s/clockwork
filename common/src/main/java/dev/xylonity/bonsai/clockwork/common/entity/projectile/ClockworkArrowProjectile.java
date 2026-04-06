package dev.xylonity.bonsai.clockwork.common.entity.projectile;

import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import dev.xylonity.bonsai.clockwork.registry.ClockworkSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClockworkArrowProjectile extends AbstractArrow implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation IDLE_OFF = RawAnimation.begin().thenPlay("idle_off");

    private static final EntityDataAccessor<Optional<UUID>> TARGET_UUID = SynchedEntityData.defineId(ClockworkArrowProjectile.class, EntityDataSerializers.OPTIONAL_UUID);

    private @Nullable LivingEntity cachedTarget;
    private boolean wasInGround = false;

    private boolean initialRedirectDone = false;

    public ClockworkArrowProjectile(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        setSoundEvent(ClockworkSounds.CLOCKWORK_ARROW_HIT_GROUND.get());
    }

    public ClockworkArrowProjectile(Level level, LivingEntity shooter) {
        super(ClockworkEntities.CLOCKWORK_ARROW_PROJECTILE.get(), shooter, level);
        setSoundEvent(ClockworkSounds.CLOCKWORK_ARROW_HIT_GROUND.get());
    }

    public void setTarget(@Nullable LivingEntity target) {
        this.cachedTarget = target;
        this.entityData.set(TARGET_UUID, target != null ? Optional.of(target.getUUID()) : Optional.empty());
    }

    public @Nullable LivingEntity getTarget() {
        if (cachedTarget != null && cachedTarget.isAlive()) {
            return cachedTarget;
        }

        final Optional<UUID> uuid = this.entityData.get(TARGET_UUID);
        if (uuid.isPresent() && this.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getEntity(uuid.get()) instanceof LivingEntity entity && entity.isAlive()) {
                cachedTarget = entity;
                return entity;
            }

        }

        cachedTarget = null;
        return null;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && !this.inGround) {
            applyHoming();
        }

        super.tick();
    }

    private void applyHoming() {
        final LivingEntity target = resolveTarget();
        if (target == null) {
            return;
        }

        final Vec3 movement = this.getDeltaMovement();
        final double speed = movement.length();
        if (speed < 0.0001) {
            return;
        }

        final Vec3 toTarget = target.getEyePosition().subtract(this.position()).normalize();
        if (!initialRedirectDone) {
            this.setDeltaMovement(toTarget.scale(speed));
            initialRedirectDone = true;
        }
        else {
            // Homing movement
            final Vec3 newMovement = movement.normalize().lerp(toTarget, 0.1).normalize().scale(speed);
            this.setDeltaMovement(newMovement);
        }

        this.hasImpulse = true;
    }

    private @Nullable LivingEntity resolveTarget() {
        LivingEntity target = getTarget();
        if (target != null && isValidTarget(target)) {
            return target;
        }

        target = findNearestEnemy();
        if (target != null) {
            setTarget(target);
        }

        return target;
    }

    private boolean isValidTarget(LivingEntity candidate) {
        if (!candidate.isAlive() || candidate.isRemoved() || candidate.isSpectator()) {
            return false;
        }
        if (candidate instanceof Player player && (player.getAbilities().invulnerable || player.isCreative())) {
            return false;
        }
        if (this.getOwner() instanceof LivingEntity owner) {
            return candidate != owner && !candidate.isAlliedTo(owner);
        }

        return true;
    }

    private @Nullable LivingEntity findNearestEnemy() {
        final List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(3),
                this::isValidTarget
        );
        if (nearby.isEmpty()) {
            return null;
        }

        nearby.sort(Comparator.comparingDouble(livingEntity -> livingEntity.distanceToSqr(this)));
        return nearby.get(0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TARGET_UUID, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.entityData.get(TARGET_UUID).ifPresent(uuid -> tag.putUUID("Target", uuid));
        tag.putBoolean("InitialRedirectDone", initialRedirectDone);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Target")) {
            this.entityData.set(TARGET_UUID, Optional.of(tag.getUUID("Target")));
        }

        initialRedirectDone = tag.getBoolean("InitialRedirectDone");
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(ClockworkItems.CLOCKWORK_ARROW.get());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 2, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> event) {
        if (inGround) {
            if (!wasInGround) {
                event.setAndContinue(IDLE_OFF);
                wasInGround = true;
            }

            return PlayState.CONTINUE;
        }

        wasInGround = false;
        event.setAnimation(IDLE);

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

}