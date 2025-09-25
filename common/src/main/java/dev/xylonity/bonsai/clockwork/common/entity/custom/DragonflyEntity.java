package dev.xylonity.bonsai.clockwork.common.entity.custom;

import dev.xylonity.bonsai.clockwork.common.entity.PassiveClockworkEntity;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.network.packets.DragonflyAscendKeyC2SPacket;
import dev.xylonity.knightlib.api.network.Network;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class DragonflyEntity extends PassiveClockworkEntity implements PlayerRideable {

    private final RawAnimation WALK = RawAnimation.begin().thenPlay("walk");
    private final RawAnimation FLY  = RawAnimation.begin().thenPlay("fly");
    private final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");

    // 0 walk, 1 flying, 2 idle (floor)
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(DragonflyEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> ASCENDING = SynchedEntityData.defineId(DragonflyEntity.class, EntityDataSerializers.BOOLEAN);

    public DragonflyEntity(EntityType<? extends Animal> entityType, Level level) {
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
        this.entityData.define(STATE, 0);
        this.entityData.define(ASCENDING, false);
    }

    public void setState(int state) {
        this.entityData.set(STATE, state);
    }

    /**
     * 0 walk, 1 flying, 2 idle (floor)
     */
    public int getState() {
        return this.entityData.get(STATE);
    }

    public void setAscending(boolean ascending) {
        this.entityData.set(ASCENDING, ascending);
    }

    public boolean isAscending() {
        return this.entityData.get(ASCENDING);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (onGround()) {
                setState(0);
            }

            if (getControllingPassenger() == null && !onGround() && getState() == 1) {
                this.setDeltaMovement(getDeltaMovement().x, -0.1, getDeltaMovement().z);
            }
        }

    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return getState() != 1;
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!player.isSecondaryUseActive()) {
            if (!level().isClientSide()) {
                player.startRiding(this, true);
            }

            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public LivingEntity getControllingPassenger() {
        if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof LivingEntity rider) {
            return rider;
        }

        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("StateMachine", getState());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("StateMachine")) {
            this.setState(compound.getInt("StateMachine"));
        }

    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction positioner) {
        super.positionRider(passenger, positioner);
        if (passenger instanceof LivingEntity) {
            passenger.setPos(this.getX(), this.getY() + 0.2, this.getZ());
        }
    }

    @Override
    public boolean isNoGravity() {
        return getState() == 1 || super.isNoGravity();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (!this.isAlive()) {
            super.travel(travelVector);
            return;
        }

        LivingEntity rider = this.getControllingPassenger();
        if (this.isVehicle() && rider != null) {
            float baseTurn = (getState() == 1) ? 18.0F : 12.0F;
            float step = baseTurn + (Mth.abs(Mth.wrapDegrees(rider.getYRot() - this.getYRot())) * 0.35F) + (Mth.abs(Mth.wrapDegrees(rider.getYRot() - rider.yRotO)) * 0.65F);

            step = Mth.clamp(step, baseTurn, 50.0F);

            this.setYRot(Mth.approachDegrees(this.getYRot(), rider.getYRot(), step));
            this.yRotO = this.getYRot();

            this.setXRot(rider.getXRot() * 0.5F);

            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();

            float strafe = rider.xxa;
            float forward = rider.zza;

            if (getState() == 1) {
                double capSpeed = this.getAttributeValue(Attributes.FLYING_SPEED) * 0.95D;

                Vec3 look = rider.getLookAngle().normalize();
                Vec3 right = new Vec3(0.0D, 1.0D, 0.0D).cross(look).normalize();
                if (right.lengthSqr() < 1.0e-6) {
                    right = new Vec3(1.0D, 0.0D, 0.0D);
                }
                else {
                    right = new Vec3(right.x, 0.0D, right.z).normalize();
                }

                Vec3 deltaMovement = this.getDeltaMovement().scale(0.90D);
                Vec3 delta = look.scale(forward * capSpeed).add(right.scale(strafe * capSpeed * 0.80D)).subtract(deltaMovement);
                deltaMovement = deltaMovement.add(delta.scale(0.35D));

                double horizontal = Math.sqrt(deltaMovement.x * deltaMovement.x + deltaMovement.z * deltaMovement.z);
                if (horizontal > capSpeed) {
                    double scale = capSpeed / Math.max(horizontal, 1.0e-6);
                    deltaMovement = new Vec3(deltaMovement.x * scale, deltaMovement.y, deltaMovement.z * scale);
                }

                double maxY = capSpeed * 0.6D;
                deltaMovement = new Vec3(deltaMovement.x, Mth.clamp(deltaMovement.y, -maxY, maxY), deltaMovement.z);

                this.setDeltaMovement(deltaMovement);
                this.move(MoverType.SELF, this.getDeltaMovement());

                this.fallDistance = 0.0F;
            }
            else {
                this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.65F);
                super.travel(new Vec3(strafe * 0.75F, travelVector.y, forward * 0.85F));
            }
        }
        else {
            super.travel(travelVector);
        }

        if (isAscending()) {
            //this.setPos(getX(), getY() + 0.25, getZ());
            this.setDeltaMovement(getDeltaMovement().x, getDeltaMovement().y + 0.25, getDeltaMovement().z);

            // For some reason travel is only executed in the client when there is a passenger present, so another packet is sent to reassign the synched data
            Network.sendToServer(new DragonflyAscendKeyC2SPacket(false));
        }

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 2, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> event) {
        if (getState() == 1) {
            event.setAnimation(FLY);
        }
        else if (event.isMoving()) {
            event.setAnimation(WALK);
        }
        else {
            event.setAnimation(IDLE);
        }

        return PlayState.CONTINUE;
    }

}
