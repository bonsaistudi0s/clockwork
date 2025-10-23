package dev.xylonity.bonsai.clockwork.common.entity.passive;

import dev.xylonity.bonsai.clockwork.common.entity.HostileClockworkEntity;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import dev.xylonity.bonsai.clockwork.registry.ClockworkSounds;
import net.minecraft.core.particles.ParticleTypes;
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

    private final RawAnimation DEACTIVATED = RawAnimation.begin().thenPlay("deactivated");

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
                DragonflyEntity entity = ClockworkEntities.DRAGONFLY.get().create(level());
                if (entity != null) {
                    entity.setPos(position());
                    entity.setActivatingTicks(0);
                    level().addFreshEntity(entity);
                }

                level().playSound(null, blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1, 1);
                generatePoofParticles();
                this.discard();
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
        for (int i = 0; i < 20; i++) {
            double dx = (this.random.nextDouble() - 0.5) * 1.25;
            double dy = (this.random.nextDouble() - 0.5) * 1.25;
            double dz = (this.random.nextDouble() - 0.5) * 1.25;
            if (this.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.POOF, position().x, this.getY() + getBbHeight() * Math.random(), position().z, 1, dx, dy, dz, 0);
            }
        }

    }

    private void generateFailParticles() {
        for (int i = 0; i < 15; i++) {
            double dx = (this.random.nextDouble() - 0.5) * 1.25;
            double dy = (this.random.nextDouble() - 0.5) * 1.25;
            double dz = (this.random.nextDouble() - 0.5) * 1.25;
            if (this.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.SMOKE, position().x, this.getY() + getBbHeight() * Math.random(), position().z, 1, dx, dy, dz, 0);
            }
        }

    }

    @Override
    public void push(double x, double y, double z) {
        ;;
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
        event.setAnimation(DEACTIVATED);
        return PlayState.CONTINUE;
    }

}
