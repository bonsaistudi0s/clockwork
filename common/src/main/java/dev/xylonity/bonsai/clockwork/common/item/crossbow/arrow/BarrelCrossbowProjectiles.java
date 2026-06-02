package dev.xylonity.bonsai.clockwork.common.item.crossbow.arrow;

import dev.xylonity.bonsai.clockwork.common.entity.projectile.ClockworkArrowProjectile;
import dev.xylonity.bonsai.clockwork.common.item.crossbow.BarrelCrossbow;
import dev.xylonity.bonsai.clockwork.common.item.crossbow.ScopeCrossbow;
import dev.xylonity.bonsai.clockwork.registry.ClockworkSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class BarrelCrossbowProjectiles {

    public static boolean tryLoad(LivingEntity shooter, ItemStack crossbow) {
        final int multishotLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, crossbow);
        final int shotCount = (multishotLevel == 0) ? 1 : 3;
        final boolean creative = isCreative(shooter);

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

            if (!loadSingle(shooter, crossbow, ammo, /* isExtra */ i > 0, creative)) {
                return false;
            }

        }

        return true;
    }

    public static void fireAllLoaded(Level level, LivingEntity shooter, InteractionHand hand, ItemStack crossbow) {
        if (level.isClientSide) {
            return;
        }

        final float velocity = BarrelCrossbow.baseVelocityFor(crossbow);
        final List<ItemStack> charged = getChargedProjectiles(crossbow);
        final float[] pitches = randomShotPitches(shooter.getRandom());

        for (int i = 0; i < charged.size(); i++) {
            if (charged.get(i).isEmpty()) {
                continue;
            }

            float angleDeg = switch (i) {
                case 1 -> -10.0f;
                case 2 -> 10.0f;
                default -> 0.0f;
            };

            shootSingle(level, shooter, hand, crossbow, charged.get(i), pitches[i], isCreative(shooter), velocity * BarrelCrossbow.VELOCITY_MULTIPLIER, 1.0f, angleDeg);
        }

        clearChargedProjectiles(crossbow);
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

        if (arrow instanceof ClockworkArrowProjectile clockworkArrow) {
            final LivingEntity target = ScopeCrossbow.searchForwardTarget(level, shooter, BarrelCrossbow.HOMING_SCAN_RANGE);
            if (target != null) {
                clockworkArrow.setTarget(target);
            }

        }

        return arrow;
    }

    private static void launchProjectile(Projectile projectile, LivingEntity shooter, ItemStack crossbow, float angleDeg, float velocity, float inaccuracy) {
        if (shooter instanceof CrossbowAttackMob mob) {
            mob.shootCrossbowProjectile(mob.getTarget(), crossbow, projectile, angleDeg);
        }
        else {
            final Vec3 up = shooter.getUpVector(1.0f);
            final Vec3 look = shooter.getViewVector(1.0f);
            final Vector3f direction = look.toVector3f().rotate(new Quaternionf().setAngleAxis(angleDeg * 0.017453292f, up.x, up.y, up.z));
            projectile.shoot(direction.x(), direction.y(), direction.z(), velocity, inaccuracy);
        }

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

    private static float[] randomShotPitches(RandomSource random) {
        final boolean flag = random.nextBoolean();
        return new float[] { 1.0f, pitchVariant(flag, random), pitchVariant(!flag, random) };
    }

    private static float pitchVariant(boolean high, RandomSource random) {
        return 1.0f / (random.nextFloat() * 0.5f + 1.8f) + (high ? 0.63f : 0.43f);
    }

    private static boolean isCreative(LivingEntity entity) {
        return entity instanceof Player p && p.getAbilities().instabuild;
    }

}