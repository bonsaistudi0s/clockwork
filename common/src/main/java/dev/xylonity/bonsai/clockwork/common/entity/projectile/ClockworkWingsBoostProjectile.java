package dev.xylonity.bonsai.clockwork.common.entity.projectile;

import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.knightlib.common.entity.AbstractProjectile;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class ClockworkWingsBoostProjectile extends GenericProjectile {

    private static final EntityDataAccessor<Optional<UUID>> TARGET_ENTITY_UUID = SynchedEntityData.defineId(ClockworkWingsBoostProjectile.class, EntityDataSerializers.OPTIONAL_UUID);

    public ClockworkWingsBoostProjectile(EntityType<? extends AbstractProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET_ENTITY_UUID, Optional.empty());
    }

    public Optional<UUID> getAttachedEntityUUID() {
        return this.getEntityData().get(TARGET_ENTITY_UUID);
    }

    public void setAttachedEntityUUID(UUID uuid) {
        this.getEntityData().set(TARGET_ENTITY_UUID, Optional.of(uuid));
    }

    public void tick() {
        super.tick();

        if (!level().isClientSide && tickCount % 2 == 0 && !getAttachedEntityUUID().isEmpty()) {
            final Entity attachedEntity = ((ServerLevel) level()).getEntity(getAttachedEntityUUID().get());
            if (attachedEntity instanceof Player player && player.isFallFlying()) {
                attachedEntity.setPos(player.position());
                Vec3 lookAngle = attachedEntity.getLookAngle();
                Vec3 deltaMovement = attachedEntity.getDeltaMovement();
                double boostMultiplier = ClockworkConfig.CLOCKWORK_WINGS_BOOST_VELOCITY;
                attachedEntity.setDeltaMovement(deltaMovement.add(
                        lookAngle.x * 0.1 + (lookAngle.x * boostMultiplier - deltaMovement.x) * 0.5,
                        lookAngle.y * 0.1 + (lookAngle.y * boostMultiplier - deltaMovement.y) * 0.5,
                        lookAngle.z * 0.1 + (lookAngle.z * boostMultiplier - deltaMovement.z) * 0.5
                ));
                player.hurtMarked = true;
                player.hasImpulse = true;
            }

            this.setPos(attachedEntity.getX(), attachedEntity.getY(), attachedEntity.getZ());
            this.setDeltaMovement(attachedEntity.getDeltaMovement());
        }

        // Trail particles
        if (!level().isClientSide) {
            ((ServerLevel) level()).sendParticles(ParticleTypes.POOF, getX(), getY() + getBbHeight() * 0.5f, getZ(), 2, 0, 0, 0, 0.0225);
        }

    }

    @Override
    protected int baseLifetime() {
        return 10;
    }

}
