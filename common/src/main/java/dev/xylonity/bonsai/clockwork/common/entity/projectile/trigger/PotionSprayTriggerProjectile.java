package dev.xylonity.bonsai.clockwork.common.entity.projectile.trigger;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
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

    private static List<MobEffectInstance> computeAdjustedEffects(ItemStack stack) {
        List<MobEffectInstance> originalEffects = PotionUtils.getMobEffects(stack);
        List<MobEffectInstance> newEffects = new ArrayList<>(originalEffects.size());

        int effectsLeft = 0;
        if (stack.hasTag()) {
            effectsLeft = stack.getTag().getInt(NBT_SPRAY_TICKS);
        }

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

    @Override
    public void tick() {

        super.tick();

        if (!effects.isEmpty()) {
            List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, new AABB(getX() - 1, getY() - 0.5, getZ() - 1, getX() + 1, getY() + 0.5, getZ() + 1));
            for (LivingEntity entity : entities) {
                for (MobEffectInstance effect : effects) {
                    if (!(entity.equals(getOwner()) && tickCount < 10)) {
                        entity.addEffect(new MobEffectInstance(
                                effect.getEffect(),
                                60,
                                effect.getAmplifier(),
                                effect.isAmbient(),
                                effect.isVisible(),
                                effect.showIcon()
                        ));
                    }
                }

            }

        }

        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x * 0.98, movement.y - 0.015f, movement.z * 0.98);
        this.move(MoverType.SELF, this.getDeltaMovement());

        if (!level().isClientSide && onGround()) this.discard();
    }

}
