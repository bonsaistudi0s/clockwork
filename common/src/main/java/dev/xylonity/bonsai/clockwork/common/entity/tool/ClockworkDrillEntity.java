package dev.xylonity.bonsai.clockwork.common.entity.tool;

import dev.xylonity.bonsai.clockwork.common.menu.DrillMenu;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClockworkDrillEntity extends Entity implements GeoEntity, Container, MenuProvider, OwnableEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation DRILL = RawAnimation.begin().thenLoop("drill");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(ClockworkDrillEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    // 0 inactive, 1 active, 2 broken
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(ClockworkDrillEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DRILLING = SynchedEntityData.defineId(ClockworkDrillEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DRILLING_UP = SynchedEntityData.defineId(ClockworkDrillEntity.class, EntityDataSerializers.BOOLEAN);

    private float breakProgress = 0f;
    private BlockPos breakingPos = null;
    private float breakProgressUpper = 0f;
    private BlockPos breakingPosUpper = null;

    public float drillTilt = 0f;
    public float prevDrillTilt = 0f;

    private NonNullList<ItemStack> inventory = NonNullList.withSize(5, ItemStack.EMPTY);

    public ClockworkDrillEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean create(final Level level, final BlockPos blockPos, @NotNull final Player player) {
        final ClockworkDrillEntity drill = ClockworkEntities.CLOCKWORK_DRILL.get().create(level);
        if (drill != null) {
            drill.setYRot(player.getYRot());

            drill.setOwnerUUID(player.getUUID());

            final BlockPos spawnPos = blockPos.above();
            drill.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

            level.addFreshEntity(drill);

            return true;
        }

        return false;
    }

    @Override
    public void defineSynchedData() {
        this.getEntityData().define(DATA_OWNERUUID_ID, Optional.empty());
        this.getEntityData().define(STATE, 0);
        this.getEntityData().define(DRILLING, false);
        this.getEntityData().define(DRILLING_UP, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            setYHeadRot(Mth.approachDegrees(getYHeadRot(), getYRot(), 12.0f));
            prevDrillTilt = drillTilt;
            if (isDrillingUp()) {
                drillTilt = Math.min(1f, drillTilt + 0.12f);
            }
            else {
                drillTilt = Math.max(0f, drillTilt - 0.12f);
            }

        }

        // Broken particles
        if (tickCount % 10 == 0 && isBroken() && !level().isClientSide) {
            final double offsetX = (random.nextDouble() * 0.5 - 0.25) * 2;
            final double offsetY = (random.nextDouble() * 0.5 - 0.25) * 2;
            final double offsetZ = (random.nextDouble() * 0.5 - 0.25) * 2;
            ((ServerLevel) level()).sendParticles(ParticleTypes.LARGE_SMOKE,
                    getX(), getY() + getBbHeight() * 0.5f, getZ(), 1, offsetX, offsetY, offsetZ, 0);
        }

        // Movement
        if (isActive()) {
            final float yawRad = getYRot() * Mth.DEG_TO_RAD;
            final double speed = 0.05;

            final double dirX = -Mth.sin(yawRad);
            final double dirZ =  Mth.cos(yawRad);

            final double lookAhead = 1;
            final BlockPos ahead = BlockPos.containing(getX() + dirX * lookAhead, getY(), getZ() + dirZ * lookAhead);
            // If the block in front is air
            if (level().getBlockState(ahead).isAir()) {
                setDeltaMovement(dirX * speed, getDeltaMovement().y, dirZ * speed);
                move(MoverType.SELF, getDeltaMovement());
                setDrilling(false);

                if (breakingPos != null && !level().isClientSide) {
                    level().destroyBlockProgress(getId(), breakingPos, -1);
                    breakingPos = null;
                    breakProgress = 0f;
                }
            }
            // If the block in front is mineable
            else {
                setDeltaMovement(0.0, 0.0, 0.0);
                setDrilling(true);

                // Block breaking
                if (!level().isClientSide) {
                    final BlockState state = level().getBlockState(ahead);

                    if (!ahead.equals(breakingPos)) {
                        if (breakingPos != null) {
                            level().destroyBlockProgress(getId(), breakingPos, -1);
                        }

                        breakingPos = ahead.immutable();
                        breakProgress = 0f;
                    }

                    float hardness = state.getDestroySpeed(level(), ahead);
                    if (hardness >= 0) {
                        // Replicates the speed of a stone tool (4 mining speed)
                        final boolean correctTool = !state.requiresCorrectToolForDrops();
                        final float delta = 4f / hardness / (correctTool ? 30f : 100f);

                        breakProgress += delta;

                        final int stage = (int) (breakProgress * 10f);
                        level().destroyBlockProgress(getId(), ahead, Mth.clamp(stage, 0, 9));

                        // If the block has been broken successfully
                        if (breakProgress >= 1.0f) {
                            final BlockState blockState = level().getBlockState(ahead);
                            final List<ItemStack> drops = Block.getDrops(blockState, (ServerLevel) level(), ahead, level().getBlockEntity(ahead));

                            level().destroyBlock(ahead, false);

                            for (ItemStack drop : drops) {
                                if (!insertItem(drop)) {
                                    Block.popResource(level(), blockPosition(), drop);
                                }

                            }

                            breakProgress = 0f;
                            breakingPos = null;
                        }
                    }

                }

            }

        }

    }

    public boolean insertItem(final ItemStack toInsert) {
        if (toInsert.isEmpty()) {
            return false;
        }

        // Tries to stack the given item inside the inventory
        for (final ItemStack slot : inventory) {
            if (!slot.isEmpty() && ItemStack.isSameItemSameTags(slot, toInsert)) {
                final int space = slot.getMaxStackSize() - slot.getCount();
                if (space > 0) {
                    final int transfer = Math.min(space, toInsert.getCount());
                    slot.grow(transfer);
                    toInsert.shrink(transfer);
                    if (toInsert.isEmpty()) {
                        return true;
                    }

                }

            }

        }

        // Searches for empty slots
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).isEmpty()) {
                inventory.set(i, toInsert.copy());
                toInsert.setCount(0);
                return true;
            }

        }

        // Inventory's full
        return false;
    }

    public boolean isDrilling() {
        return this.getEntityData().get(DRILLING);
    }

    public void setDrilling(boolean drilling) {
        this.getEntityData().set(DRILLING, drilling);
    }

    public boolean isDrillingUp() {
        return this.getEntityData().get(DRILLING_UP);
    }

    public void setDrillingUp(boolean drillingUp) {
        this.getEntityData().set(DRILLING_UP, drillingUp);
    }

    public boolean isBroken() {
        return this.getEntityData().get(STATE) == 2;
    }

    public boolean isActive() {
        return this.getEntityData().get(STATE) == 1;
    }

    public boolean isInactive() {
        return this.getEntityData().get(STATE) == 0;
    }

    public void setState(int state) {
        this.getEntityData().set(STATE, state);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("OwnerUUID")) {
            this.setOwnerUUID(compound.getUUID("OwnerUUID"));
        }
        if (compound.contains("InventorySize")) {
            inventory = NonNullList.withSize(compound.getInt("InventorySize"), ItemStack.EMPTY);
        }
        if (compound.contains("Inventory")) {
            ContainerHelper.loadAllItems(compound.getCompound("Inventory"), inventory);
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        if (this.getOwnerUUID() != null) {
            compound.putUUID("OwnerUUID", this.getOwnerUUID());
        }

        compound.putInt("InventorySize", inventory.size());
        final CompoundTag inventoryTag = new CompoundTag();
        ContainerHelper.saveAllItems(inventoryTag, inventory);
        compound.put("Inventory", inventoryTag);

    }

    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(uuid));
    }

    @Override
    public int getContainerSize() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (final ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return false;
            }

        }

        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return inventory.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int count) {
        return ContainerHelper.removeItem(inventory, slot, count);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }

    }

    @Override
    public void setChanged() {
        ;;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !isRemoved() && player.distanceToSqr(this) <= 64.0;
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DrillMenu(i, inventory, this);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        setState(isActive() ? 0 : 1);

        return super.interact(player, hand);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", this::predicate));
        controllerRegistrar.add(new AnimationController<>(this, "drillingController", this::drillingPredicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> event) {
        if (isActive()) {
            event.setAnimation(WALK);
        }
        else {
            event.setAnimation(IDLE);
        }

        return PlayState.CONTINUE;
    }

    private <T extends GeoAnimatable> PlayState drillingPredicate(AnimationState<T> event) {
        if (isDrilling() && isActive()) {
            event.setAnimation(DRILL);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

}