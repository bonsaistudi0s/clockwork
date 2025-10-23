package dev.xylonity.bonsai.clockwork.common.entity.passive;

import dev.xylonity.bonsai.clockwork.common.entity.HostileClockworkEntity;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import dev.xylonity.bonsai.clockwork.registry.ClockworkSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class BrokenDragonflyEntity extends HostileClockworkEntity {

    private final RawAnimation ACTIVATE = RawAnimation.begin().thenPlay("activate");
    private final RawAnimation DEACTIVATED = RawAnimation.begin().thenPlay("deactivated");

    // 0 walk, 1 flying, 2 idle (floor)
    public static final EntityDataAccessor<Integer> ACTIVATED_TIMER = SynchedEntityData.defineId(BrokenDragonflyEntity.class, EntityDataSerializers.INT);

    public static final int ANIMATION_ACTIVATE_TICKS = 28;

    public BrokenDragonflyEntity(EntityType<? extends HostileClockworkEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, ClockworkConfig.DRAGONFLY_DEFAULT_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, 6f)
                .add(Attributes.ATTACK_SPEED, 1.0f)
                .add(Attributes.MOVEMENT_SPEED, ClockworkConfig.DRAGONFLY_DEFAULT_WALKING_SPEED)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.FLYING_SPEED, ClockworkConfig.DRAGONFLY_DEFAULT_FLYING_SPEED);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ACTIVATED_TIMER, ANIMATION_ACTIVATE_TICKS + 1);
    }

    public void setActivatedTimer(int activatedTicks) {
        this.entityData.set(ACTIVATED_TIMER, activatedTicks);
    }

    public int getActivatedTimer() {
        return this.entityData.get(ACTIVATED_TIMER);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            if (getActivatedTimer() <= ANIMATION_ACTIVATE_TICKS) {

                if (getActivatedTimer() == 0) {
                    DragonflyEntity entity = ClockworkEntities.DRAGONFLY.get().create(level());
                    if (entity != null) {
                        entity.setPos(position());
                        level().addFreshEntity(entity);
                    }

                    level().playSound(null, blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1, 1);
                    generatePoofParticles();
                    this.discard();

                    return;
                }

                setActivatedTimer(getActivatedTimer() - 1);
            }
        }

    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (player.getItemInHand(hand).getItem() == ClockworkItems.CLOCKWORK_GEAR.get()) {
            if (level().isClientSide) {
                return InteractionResult.SUCCESS;
            }

            if (!player.getAbilities().instabuild) {
                player.getItemInHand(hand).shrink(1);
            }

            level().playSound(null, blockPosition(), ClockworkSounds.DRAGONFLY_GEAR.get(), SoundSource.BLOCKS, 1, 1);

            if (random.nextFloat() <= 0.3f) {
                // Starts the counter
                setActivatedTimer(getActivatedTimer() - 1);
            }
            else {
                generateFailParticles();
                return InteractionResult.FAIL;
            }


            return InteractionResult.SUCCESS;

        }

        return super.mobInteract(player, hand);
    }

    private void generatePoofParticles() {
        for (int i = 0; i < 30; i++) {
            double dx = (this.random.nextDouble() - 0.5) * 1.25;
            double dy = (this.random.nextDouble() - 0.5) * 1.25;
            double dz = (this.random.nextDouble() - 0.5) * 1.25;
            if (this.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.POOF, position().x, this.getY() + getBbHeight() * Math.random(), position().z, 1, dx, dy, dz, 0.1);
            }
        }

    }

    private void generateFailParticles() {
        for (int i = 0; i < 20; i++) {
            double dx = (this.random.nextDouble() - 0.5) * 1.25;
            double dy = (this.random.nextDouble() - 0.5) * 1.25;
            double dz = (this.random.nextDouble() - 0.5) * 1.25;
            if (this.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.SMOKE, position().x, this.getY() + getBbHeight() * Math.random(), position().z, 1, dx, dy, dz, 0.1);
            }
        }

    }

    @Override
    public void push(double x, double y, double z) {
        ;;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("ActivatedTimer", getActivatedTimer());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("ActivatedTimer")) {
            this.setActivatedTimer(compound.getInt("ActivatedTimer"));
        }

    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ClockworkSounds.DRAGONFLY_HURT.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 2, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> event) {
        if (getActivatedTimer() <= ANIMATION_ACTIVATE_TICKS) {
            event.setAnimation(ACTIVATE);
        }
        else {
            event.setAnimation(DEACTIVATED);
        }

        return PlayState.CONTINUE;
    }

}
