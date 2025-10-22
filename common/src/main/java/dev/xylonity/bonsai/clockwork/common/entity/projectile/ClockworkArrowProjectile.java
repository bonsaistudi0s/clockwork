package dev.xylonity.bonsai.clockwork.common.entity.projectile;

import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
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
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClockworkArrowProjectile extends AbstractArrow implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private final RawAnimation IDLE_OFF = RawAnimation.begin().thenPlay("idle_off");

    private static final EntityDataAccessor<Optional<UUID>> TARGET_UUID = SynchedEntityData.defineId(ClockworkArrowProjectile.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final double H_RANGE_XZ = 3.0;
    private static final double H_RANGE_Y = 3.0;
    private static final double HOMING_STRENGTH = 0.1;

    private boolean wasIdle = false;

    private LivingEntity cachedTarget;

    public ClockworkArrowProjectile(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public ClockworkArrowProjectile(Level level, LivingEntity shooter) {
        super(ClockworkEntities.CLOCKWORK_ARROW_PROJECTILE.get(), shooter, level);
    }

    public void setTarget(@Nullable LivingEntity target) {
        this.cachedTarget = target;
        if (target != null) {
            this.entityData.set(TARGET_UUID, Optional.of(target.getUUID()));
        }
        else {
            this.entityData.set(TARGET_UUID, Optional.empty());
        }

    }

    @Nullable
    public LivingEntity getTarget() {
        if (cachedTarget != null && cachedTarget.isAlive()) return cachedTarget;

        Optional<UUID> optionalUUID = this.entityData.get(TARGET_UUID);
        if (optionalUUID.isPresent() && this.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getEntity(optionalUUID.get()) instanceof LivingEntity entity && entity.isAlive()) {
                cachedTarget = entity;
                return entity;
            }

        }

        return null;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && !this.inGround) {
            LivingEntity target = getTarget();

            if (target == null || !isValidEnemy(target)) {
                target = findNearestEnemy();
                if (target != null) {
                    setTarget(target);
                }

            }

            if (target != null) {
                Vec3 movement = this.getDeltaMovement();
                double speed = movement.length();
                if (speed > 0.0001) {
                    this.setDeltaMovement(movement.normalize().lerp(target.getEyePosition().subtract(this.position()).normalize(), HOMING_STRENGTH).normalize().scale(speed));
                    this.hasImpulse = true;
                }
            }

        }

        super.tick();
    }

    private boolean isValidEnemy(LivingEntity candidate) {
        if (!candidate.isAlive() || candidate.isRemoved()) {
            return false;
        }

        if (candidate.isSpectator()) {
            return false;
        }

        if (candidate instanceof Player p && (p.getAbilities().invulnerable || p.isCreative())) {
            return false;
        }

        if (this.getOwner() instanceof LivingEntity lo) {
            if (candidate == lo) {
                return false;
            }

            return !candidate.isAlliedTo(lo);
        }

        return true;
    }

    @Nullable
    private LivingEntity findNearestEnemy() {
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(H_RANGE_XZ, H_RANGE_Y, H_RANGE_XZ), this::isValidEnemy);
        if (list.isEmpty()) return null;

        list.sort(Comparator.comparingDouble(e -> e.distanceToSqr(this)));

        return list.get(0);
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
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Target")) {
            this.entityData.set(TARGET_UUID, Optional.of(tag.getUUID("Target")));
        }

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 2, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> event) {
        if (inGround) {
            if (!wasIdle) {
                event.setAndContinue(IDLE_OFF);
                wasIdle = true;
            }

            return PlayState.CONTINUE;
        }
        else {
            wasIdle = false;
            event.setAnimation(IDLE);
            return PlayState.CONTINUE;
        }

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(ClockworkItems.CLOCKWORK_ARROW.get());
    }

}
