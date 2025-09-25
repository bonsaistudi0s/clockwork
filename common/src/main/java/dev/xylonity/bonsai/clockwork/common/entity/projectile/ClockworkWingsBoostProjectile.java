package dev.xylonity.bonsai.clockwork.common.entity.projectile;

import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.knightlib.common.entity.BaseProjectile;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.OptionalInt;

public class ClockworkWingsBoostProjectile extends BaseProjectile {

    private static final EntityDataAccessor<OptionalInt> IS_ATTACHED_TO_TARGET = SynchedEntityData.defineId(ClockworkWingsBoostProjectile.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);

    private LivingEntity attachedToEntity;

    public ClockworkWingsBoostProjectile(EntityType<? extends BaseProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ClockworkWingsBoostProjectile(EntityType<? extends BaseProjectile> pEntityType, Level pLevel, LivingEntity target) {
        this(pEntityType, pLevel);
        this.entityData.set(IS_ATTACHED_TO_TARGET, OptionalInt.of(target.getId()));
        this.attachedToEntity = target;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ATTACHED_TO_TARGET, OptionalInt.empty());
    }

    private boolean isAttachedToEntity() {
        return (this.entityData.get(IS_ATTACHED_TO_TARGET)).isPresent();
    }

    public void tick() {
        super.tick();

        // Attaches to a target entity and constantly increases its delta movement (similar to the elytra-firework functionality)
        if (this.isAttachedToEntity()) {
            if (this.attachedToEntity == null) {
                (this.entityData.get(IS_ATTACHED_TO_TARGET)).ifPresent((ent) -> {
                    Entity entity = this.level().getEntity(ent);
                    if (entity instanceof LivingEntity e) {
                        this.attachedToEntity = e;
                    }

                });

            }

            if (this.attachedToEntity != null) {
                if (this.attachedToEntity.isFallFlying()) {
                    Vec3 lookAngle = this.attachedToEntity.getLookAngle();
                    Vec3 deltaMovement = this.attachedToEntity.getDeltaMovement();
                    double boostMult = ClockworkConfig.CLOCKWORK_WINGS_BOOST_VELOCITY;
                    this.attachedToEntity.setDeltaMovement(deltaMovement.add(lookAngle.x * 0.1 + (lookAngle.x * boostMult - deltaMovement.x) * 0.5, lookAngle.y * 0.1 + (lookAngle.y * boostMult - deltaMovement.y) * 0.5, lookAngle.z * 0.1 + (lookAngle.z * boostMult - deltaMovement.z) * 0.5));
                }

                this.setPos(this.attachedToEntity.getX(), this.attachedToEntity.getY(), this.attachedToEntity.getZ());
                this.setDeltaMovement(this.attachedToEntity.getDeltaMovement());
            }

        }

        // Trail particles
        if (!level().isClientSide && tickCount % 2 == 0) {
            ((ServerLevel) level()).sendParticles(ParticleTypes.POOF, getX(), getY() + getBbHeight() * 0.5f, getZ(), 1, 0, 0, 0, 0.0225);
        }

    }

    @Override
    protected int baseLifetime() {
        return 10;
    }

}
