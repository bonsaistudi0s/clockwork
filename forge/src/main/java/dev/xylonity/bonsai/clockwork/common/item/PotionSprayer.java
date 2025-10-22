package dev.xylonity.bonsai.clockwork.common.item;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.bonsai.clockwork.client.item.renderer.PotionSprayerRenderer;
import dev.xylonity.bonsai.clockwork.client.particle.PotionSprayParticle;
import dev.xylonity.bonsai.clockwork.client.sound.Sounds;
import dev.xylonity.bonsai.clockwork.network.packets.PotionSprayerParticlesC2SPacket;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import dev.xylonity.knightlib.api.network.Network;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.function.Consumer;

public class PotionSprayer extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public static final String NBT_SPRAY_TICKS = "spray_ticks_left";
    public static final String NBT_SPRAYING = "clockwork_spraying";

    private static final RawAnimation USE = RawAnimation.begin().thenPlay("use");

    private int useTicks = 0;

    public PotionSprayer(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    private static boolean isAnyPotion(ItemStack s) {
        return !s.isEmpty() && (s.is(Items.POTION) || s.is(Items.SPLASH_POTION) || s.is(Items.LINGERING_POTION)) && !isInstantPotion(s);
    }

    private static boolean isInstantPotion(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        return PotionUtils.getMobEffects(stack).stream().anyMatch(e -> e.getEffect().isInstantenous());
    }

    private static int initialPotionTicks(ItemStack potion) {
        int max = 0;
        for (MobEffectInstance effect : PotionUtils.getMobEffects(potion)) {
            max = Math.max(max, effect.getDuration());
        }

        return max;
    }

    private static int getSprayTicks(ItemStack potion) {
        return potion.getOrCreateTag().getInt(NBT_SPRAY_TICKS);
    }

    private static void setSprayTicks(ItemStack potion, int t) {
        potion.getOrCreateTag().putInt(NBT_SPRAY_TICKS, Math.max(0, t));
    }

    private static class PotionReference {
        ItemStack stack;
        Runnable save;
        PotionReference(ItemStack stack, Runnable run) {
            this.stack = stack;
            this.save = run;
        }

    }

    @Nullable
    private static PotionSprayer.PotionReference findFirstPotion(Player player) {
        // offhand
        ItemStack off = player.getOffhandItem();
        if (isAnyPotion(off)) {
            return new PotionReference(off, () -> player.setItemInHand(InteractionHand.OFF_HAND, off));
        }

        // mainhand
        ItemStack main = player.getMainHandItem();
        if (isAnyPotion(main)) {
            return new PotionReference(main, () -> player.setItemInHand(InteractionHand.MAIN_HAND, main));
        }

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (isAnyPotion(stack)) {
                int idx = i;
                return new PotionReference(stack, () -> inventory.setItem(idx, stack));
            }

        }

        return null;
    }

    private static boolean isSpraying(ItemStack s) {
        return s.hasTag() && s.getOrCreateTag().getBoolean(NBT_SPRAYING);
    }

    private static void setSpraying(ItemStack s, boolean v) {
        s.getOrCreateTag().putBoolean(NBT_SPRAYING, v);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        PotionReference reference = findFirstPotion(player);
        if (reference == null) {
            return InteractionResultHolder.fail(stack);
        }

        if (!reference.stack.getOrCreateTag().contains(NBT_SPRAY_TICKS)) {
            int ticks = initialPotionTicks(reference.stack);
            if (ticks <= 0) {
                return InteractionResultHolder.fail(stack);
            }

            setSprayTicks(reference.stack, ticks);
            reference.save.run();
        }

        setSpraying(stack, true);

        player.startUsingItem(hand);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        setSpraying(stack, false);

        if (level.isClientSide && entity instanceof Player player) {
            Sounds.proxy().stopAllFor(player);
        }

    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack sprayer, int remainingUseDuration) {
        if (level.isClientSide) {
            if (user instanceof Player player) {
                Sounds.proxy().tickSounds(player);
            }

            return;
        }

        useTicks++;
        sprayer.getOrCreateTag().putInt("useTicks", useTicks);

        if (useTicks % 20 == 0) {
            if (user instanceof Player player && !player.getAbilities().instabuild) {
                sprayer.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            }

        }

        if (!(user instanceof Player player)) return;

        PotionReference reference = findFirstPotion(player);
        if (reference == null) {
            player.stopUsingItem();

            setSpraying(sprayer, false);

            return;
        }

        ItemStack potion = reference.stack;

        // If the potion reference doesn't have a spray reduction assigned, sets the maximum amount to the potion's duration
        if (!potion.getOrCreateTag().contains(NBT_SPRAY_TICKS)) {
            int init = initialPotionTicks(potion);
            if (init <= 0) {
                player.stopUsingItem();
                setSpraying(sprayer, false);
                return;
            }

            setSprayTicks(potion, init);

            reference.save.run();
        }

        // 1 second per second (yes)
        int left = getSprayTicks(potion) - 1;
        setSprayTicks(potion, left);

        reference.save.run();

        if (left <= 0) {
            potion.shrink(1);

            reference.save.run();

            if (potion.is(Items.POTION)) {
                ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().add(bottle)) {
                    player.drop(bottle, false);
                }

            }

            PotionReference nextPotion = findFirstPotion(player);
            if (nextPotion == null) {
                player.stopUsingItem();

                setSpraying(sprayer, false);

                return;
            }

            int nextTicks = nextPotion.stack.getOrCreateTag().contains(NBT_SPRAY_TICKS) ? getSprayTicks(nextPotion.stack) : initialPotionTicks(nextPotion.stack);
            if (nextTicks <= 0) {
                player.stopUsingItem();

                setSpraying(sprayer, false);

                return;
            }

            setSprayTicks(nextPotion.stack, nextTicks);

            nextPotion.save.run();
        }

    }

    /**
     * Replication of the ItemInHandRenderer tick logic
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide) return;

        boolean heldInMain = (entity instanceof LivingEntity le) && le.getMainHandItem() == stack;
        boolean heldInOff = (entity instanceof LivingEntity le) && le.getOffhandItem() == stack;

        if ((!heldInMain && !heldInOff) || !((entity instanceof LivingEntity le) && le.isUsingItem() && le.getUseItem() == stack)) {
            if (isSpraying(stack)) {
                setSpraying(stack, false);
            }
        }

    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private PotionSprayerRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new PotionSprayerRenderer();
                }

                return renderer;
            }
        });

    }

    private static int getFirstPotionColor(Player p) {
        ItemStack off = p.getOffhandItem();
        if (isPotion(off)) {
            return PotionUtils.getColor(off);
        }

        ItemStack main = p.getMainHandItem();
        if (isPotion(main)) {
            return PotionUtils.getColor(main);
        }

        Inventory inventoryu = p.getInventory();
        for (int i = 0; i < inventoryu.getContainerSize(); i++) {
            ItemStack stack = inventoryu.getItem(i);
            if (isPotion(stack)) {
                return PotionUtils.getColor(stack);
            }
        }

        return 0xFFFFFF;
    }

    private static boolean isPotion(ItemStack s) {
        return !s.isEmpty() && (s.is(Items.POTION) || s.is(Items.SPLASH_POTION) || s.is(Items.LINGERING_POTION));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(
                new AnimationController<>(this, "main", 0,
                state -> {
                    ItemStack stack = state.getData(DataTickets.ITEMSTACK);
                    if (stack != null && isSpraying(stack)) {
                        state.setAnimation(USE);
                    }
                    else if (stack != null && !isSpraying(stack)) {
                        return PlayState.STOP;
                    }

                    return PlayState.CONTINUE;
                })
                .setParticleKeyframeHandler(event -> {
                    Player player = Clockwork.PROXY.getClientPlayer();
                    if (player == null) return;

                    if (event.getAnimatable() == player.getMainHandItem().getItem()) {
                        Vec3 look = player.getLookAngle().normalize();

                        Vec3 right = new Vec3(-look.z, 0.0D, look.x).normalize();
                        double side = player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT ? 1.0D : -1.0D;

                        double forward = 0.8;
                        double lateral = 0D;
                        double up = -0.3;

                        Vec3 pos = player.getEyePosition(1.0F).add(look.scale(forward)).add(right.scale(lateral * side)).add(0.0D, up, 0.0D);
                        Vec3 viewVector = player.getViewVector(1).normalize().multiply(new Vec3(0.3225f, 0.3225f, 0.3225f));

                        PotionSprayParticle.setDefaultVelocityAndColor((float) viewVector.x, (float) viewVector.y, (float) viewVector.z, getFirstPotionColor(player));

                        ItemStack potion = ItemStack.EMPTY;
                        PotionReference reference = findFirstPotion(player);
                        if (reference != null) {
                            potion = reference.stack;
                        }

                        Network.sendToServer(new PotionSprayerParticlesC2SPacket(pos.x, pos.y, pos.z, (float) viewVector.x, (float) viewVector.y, (float) viewVector.z, potion));
                    }
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getBoneResetTime() {
        return 0;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.getItem() == ClockworkItems.CLOCKWORK_GEAR.get();
    }

}