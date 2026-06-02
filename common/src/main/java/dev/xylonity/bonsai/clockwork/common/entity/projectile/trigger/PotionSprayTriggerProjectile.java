package dev.xylonity.bonsai.clockwork.common.entity.projectile.trigger;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import dev.xylonity.bonsai.clockwork.common.util.StackNbt;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PotionSprayTriggerProjectile extends GenericTriggerProjectile {

    private List<MobEffectInstance> effects = new ArrayList<>();

    // Shared nbt key with PotionSprayer
    private static final String NBT_SPRAY_TICKS = "spray_ticks_left";

    public PotionSprayTriggerProjectile(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    public PotionSprayTriggerProjectile(EntityType<? extends Projectile> type, Level level, ItemStack potionStack) {
        super(type, level);
        this.effects = computeAdjustedEffects(potionStack);
    }

    @Override
    public void tick() {

        super.tick();

        if (!level().isClientSide && !effects.isEmpty()) {
            final List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, new AABB(
                    getX() - 1, getY() - 0.5, getZ() - 1,
                    getX() + 1, getY() + 0.5, getZ() + 1
            ));

            for (final LivingEntity entity : entities) {
                for (final MobEffectInstance effect : effects) {
                    if (!(entity.equals(getOwner()) && tickCount < 10)) {

                        int duration = 3;
                        final MobEffectInstance entityEffect = entity.getEffect(effect.getEffect());
                        if (entity.hasEffect(effect.getEffect()) && entityEffect != null) {
                            duration = entityEffect.getDuration() + 2;
                        }

                        entity.addEffect(new MobEffectInstance(
                                effect.getEffect(),
                                duration,
                                effect.getAmplifier(),
                                effect.isAmbient(),
                                effect.isVisible(),
                                effect.showIcon()
                        ));
                    }
                }

            }

        }

        final Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x * 0.98, movement.y - 0.015f, movement.z * 0.98);
        this.move(MoverType.SELF, this.getDeltaMovement());

        if (!level().isClientSide && onGround()) {
            this.discard();
        }
    }

    private List<MobEffectInstance> computeAdjustedEffects(ItemStack stack) {
        final List<MobEffectInstance> originalEffects = new ArrayList<>();
        stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects().forEach(originalEffects::add);
        final List<MobEffectInstance> newEffects = new ArrayList<>(originalEffects.size());

        final int effectsLeft = StackNbt.tag(stack).getInt(NBT_SPRAY_TICKS);

        if (effectsLeft > 0) {
            int baseMax = 0;
            for (MobEffectInstance effect : originalEffects) {
                baseMax = Math.max(baseMax, effect.getDuration());
            }

            if (baseMax > 0) {
                for (MobEffectInstance effect : originalEffects) {
                    newEffects.add(new MobEffectInstance(
                            effect.getEffect(),
                            60,
                            effect.getAmplifier(),
                            effect.isAmbient(),
                            effect.isVisible(),
                            effect.showIcon()
                    ));
                }

                return newEffects;
            }

        }

        for (MobEffectInstance effect : originalEffects) {
            newEffects.add(new MobEffectInstance(
                    effect.getEffect(), 60, effect.getAmplifier(),
                    effect.isAmbient(), effect.isVisible(), effect.showIcon()
            ));

        }

        return newEffects;
    }

}
