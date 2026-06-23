package dev.xylonity.bonsai.clockwork.common.entity.tool;

import dev.xylonity.bonsai.clockwork.common.menu.ClockworkDrillMenu;
import dev.xylonity.bonsai.clockwork.config.ClockworkConfig;
import dev.xylonity.bonsai.clockwork.registry.ClockworkEntities;
import dev.xylonity.bonsai.clockwork.registry.ClockworkItems;
import dev.xylonity.bonsai.clockwork.registry.ClockworkSounds;
import dev.xylonity.knightlib.KnightLib;
import dev.xylonity.knightlib.api.sound.persistent.KnightLibPersistentSounds;
import dev.xylonity.knightlib.api.util.KnightLibEasings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClockworkDrillEntity extends Entity implements GeoEntity, Container, MenuProvider, OwnableEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenPlay("walk");
    private static final RawAnimation ACTIVATE = RawAnimation.begin().thenPlay("activate");
    private static final RawAnimation DEACTIVATED = RawAnimation.begin().thenPlay("deactivated");

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(ClockworkDrillEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    // 0 inactive, 1 active, 2 broken
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(ClockworkDrillEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DRILLING = SynchedEntityData.defineId(ClockworkDrillEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DRILLING_UP = SynchedEntityData.defineId(ClockworkDrillEntity.class, EntityDataSerializers.BOOLEAN);

    private float breakProgress = 0f;
    private BlockPos breakingPos = null;
    private float breakProgressUpper = 0f;
    private BlockPos breakingPosUpper = null;

    // Smooth drill_1 bone rotation computation angles
    public float drillTilt = 0f;
    public float prevDrillTilt = 0f;

    // Drilling pause counter between the first block and the one above (if present)
    private int drillingPauseTicks = 0;

    // Drilling animation
    public float drillSpinFactor = 0f;
    public float prevDrillSpinAngle = 0f;
    public float drillSpinAngle = 0f;

    // Generic counters for both blocks mined until breaking the drill and the amount of gears given to the drill when broken
    public int blocksMinedCount = 0;
    public int gearsToRepairCount = 0;

    private static final int DRILLING_PAUSE_DURATION = 7;
    private static final int BLOCKS_UNTIL_BROKEN = ClockworkConfig.DRILL_BLOCKS_UNTIL_BROKEN;

    private NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);

    private final ContainerData drillData = new ContainerData() {
        @Override
        public int get(int i) {
            return switch (i) {
                case 0 -> blocksMinedCount;
                case 1 -> BLOCKS_UNTIL_BROKEN;
                default -> 0;
            };

        }

        @Override
        public void set(int i, int i1) {
            if (i == 0) {
                blocksMinedCount = i1;
            }

        }

        @Override
        public int getCount() {
            return 2;
        }

    };

    public ClockworkDrillEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    public static boolean create(final Level level, final BlockPos blockPos, @NotNull final Player player, @Nullable CompoundTag itemTag) {
        final ClockworkDrillEntity drill = ClockworkEntities.CLOCKWORK_DRILL.get().create(level);
        if (drill != null) {
            float yaw = player.getYRot();
            //if (player.isShiftKeyDown()) {
            yaw = Math.round(yaw / 90f) * 90f;
            //}
            drill.setYRot(yaw);

            drill.setOwnerUUID(player.getUUID());

            if (itemTag != null) {
                drill.readAdditionalSaveData(itemTag);
            }

            // Always starts in the idle state when placed
            if (!drill.isBroken()) {
                drill.setState(0);
                drill.setDrilling(false);
                drill.setDrillingUp(false);
            }

            final BlockPos spawnPos = blockPos.above();
            drill.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

            level.addFreshEntity(drill);

            return true;
        }

        return false;
    }

    @Override
    public void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_OWNERUUID_ID, Optional.empty());
        builder.define(STATE, 0);
        builder.define(DRILLING, false);
        builder.define(DRILLING_UP, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            if (isActive() && !isDrilling() && !isDrillingUp()) {
                KnightLibPersistentSounds.tick(this, "clockwork:drill_walk_loop");
            }
            else if (isDrilling() || isDrillingUp()) {
                KnightLibPersistentSounds.tick(this, "clockwork:drill_loop");
            }
            else if (!isBroken()) {
                KnightLibPersistentSounds.tick(this, "clockwork:drill_idle");
            }

            setYHeadRot(Mth.approachDegrees(getYHeadRot(), getYRot(), 12.0f));
            prevDrillTilt = drillTilt;
            if (isDrillingUp()) {
                drillTilt = Math.min(1f, drillTilt + 0.16f);
            }
            else {
                drillTilt = Math.max(0f, drillTilt - 0.16f);
            }

            if (isDrilling()) {
                drillSpinFactor = Math.min(1f, drillSpinFactor + 0.3f);
            }
            else {
                drillSpinFactor = Math.max(0f, drillSpinFactor - 0.05f);
            }

            prevDrillSpinAngle = drillSpinAngle;
            final float easedSpeed = KnightLibEasings.EASE_IN_OUT_SINE.apply(drillSpinFactor);
            drillSpinAngle += easedSpeed * 36f * Mth.DEG_TO_RAD;
        }

        if (!onGround()) {
            setDeltaMovement(getDeltaMovement().add(0, -0.04, 0));
        }
        else {
            setDeltaMovement(getDeltaMovement().x, 0, getDeltaMovement().z);
        }

        // Broken particles
        if (tickCount % 10 == 0 && isBroken() && !level().isClientSide) {
            spawnParticles(ParticleTypes.LARGE_SMOKE, 1);
        }

        // Movement
        if (!isActive()) {
            setDeltaMovement(0, getDeltaMovement().y, 0);
            move(MoverType.SELF, getDeltaMovement());
            return;
        }

        final float yawRad = getYRot() * Mth.DEG_TO_RAD;
        final double speed = 0.05;

        final double dirX = -Mth.sin(yawRad);
        final double dirZ =  Mth.cos(yawRad);

        final double lookAhead = 1;
        final BlockPos ahead = BlockPos.containing(getX() + dirX * lookAhead, getY(), getZ() + dirZ * lookAhead);
        final BlockPos aboveAhead = ahead.above();

        if (isDrillingUp()) {
            setDeltaMovement(0, 0, 0);

            if (!level().isClientSide) {
                if (drillingPauseTicks > 0) {
                    drillingPauseTicks--;
                    if (drillingPauseTicks == 0) {
                        setDrilling(true);
                    }

                }
                else {
                    mineBlock(aboveAhead, true);
                }

            }

            return;
        }

        if (!level().getBlockState(ahead).isAir()) {
            setDeltaMovement(0, 0, 0);
            setDrilling(true);

            if (!level().isClientSide) {
                mineBlock(ahead, false);
            }

            return;
        }

        // Stops if there is a cliff in front
        if (
                level().getBlockState(ahead.below()).isAir() &&
                level().getBlockState(ahead.below().below()).isAir() &&
                level().getBlockState(ahead.below().below().below()).isAir()
        ) {
            setState(0);
            setDrilling(false);
            setDrillingUp(false);
            if (!level().isClientSide) {
                resetBreakProgress(false);
                resetBreakProgress(true);
            }

            return;
        }

        setDeltaMovement(dirX * speed, getDeltaMovement().y, dirZ * speed);
        move(MoverType.SELF, getDeltaMovement());
        setDrilling(false);

        if (!level().isClientSide) {
            resetBreakProgress(false);
            resetBreakProgress(true);
        }

    }

    private void spawnParticles(final ParticleOptions particle, final int amount) {
        final double offsetX = (random.nextDouble() * 0.5 - 0.25) * 2;
        final double offsetY = (random.nextDouble() * 0.5 - 0.25) * 2;
        final double offsetZ = (random.nextDouble() * 0.5 - 0.25) * 2;
        ((ServerLevel) level()).sendParticles(particle,
                getX(), getY() + getBbHeight() * 0.5f, getZ(), amount, offsetX, offsetY, offsetZ, 0);
    }

    private void mineBlock(final BlockPos target, boolean upper) {
        final BlockState state = level().getBlockState(target);
        if (state.isAir()) {
            if (upper) {
                setDrillingUp(false);
                setDrilling(false);
                resetBreakProgress(true);
            }

            return;
        }

        BlockPos currentPos = upper ? breakingPosUpper : breakingPos;
        float currentProgress = upper ? breakProgressUpper : breakProgress;

        if (!target.equals(currentPos)) {
            if (currentPos != null) {
                level().destroyBlockProgress(getId() + (upper ? 1 : 0), currentPos, -1);
            }

            currentPos = target.immutable();
            currentProgress = 0f;
        }

        // Stops if the block in front is unbreakable
        float hardness = state.getDestroySpeed(level(), target);
        if (hardness < 0) {
            setState(0);
            setDrilling(false);
            setDrillingUp(false);
            resetBreakProgress(false);
            resetBreakProgress(true);
            return;
        }

        if (hardness >= 0) {
            // Replicates the speed of a stone tool (4 mining speed)
            final boolean correctTool = !state.requiresCorrectToolForDrops();
            final float delta = 4f / hardness / (correctTool ? 30f : 100f);
            currentProgress += delta;

            final int stage = (int) (currentProgress * 10f);
            level().destroyBlockProgress(getId() + (upper ? 1 : 0), target, Mth.clamp(stage, 0, 9));

            if (currentProgress >= 1) {
                final List<ItemStack> drops = Block.getDrops(state, (ServerLevel) level(), target, level().getBlockEntity(target));
                level().destroyBlock(target, false);

                for (ItemStack drop : drops) {
                    if (!insertItem(drop)) {
                        Block.popResource(level(), blockPosition(), drop);
                    }

                }

                ++this.blocksMinedCount;
                if (blocksMinedCount >= BLOCKS_UNTIL_BROKEN) {
                    setState(2);
                    setDrilling(false);
                    setDrillingUp(false);
                    resetBreakProgress(false);
                    resetBreakProgress(true);
                    level().playSound(null, blockPosition(), ClockworkSounds.DRILL_BREAK.get(), SoundSource.NEUTRAL);
                    return;
                }

                if (upper) {
                    setDrillingUp(false);
                    setDrilling(false);
                    resetBreakProgress(true);
                }
                else {
                    resetBreakProgress(false);
                    if (!level().getBlockState(target.above()).isAir()) {
                        setDrillingUp(true);
                        setDrilling(false);
                        drillingPauseTicks = DRILLING_PAUSE_DURATION;
                    }
                    else {
                        setDrilling(false);
                    }

                }

                return;
            }

        }

        if (upper) {
            breakingPosUpper = currentPos;
            breakProgressUpper = currentProgress;
        }
        else {
            breakingPos = currentPos;
            breakProgress = currentProgress;
        }

    }

    private void resetBreakProgress(boolean upper) {
        if (upper) {
            if (breakingPosUpper != null) {
                level().destroyBlockProgress(getId() + 1, breakingPosUpper, -1);
                breakingPosUpper = null;
                breakProgressUpper = 0f;
            }

        }
        else {
            if (breakingPos != null) {
                level().destroyBlockProgress(getId(), breakingPos, -1);
                breakingPos = null;
                breakProgress = 0f;
            }

        }

    }

    public boolean insertItem(final ItemStack toInsert) {
        if (toInsert.isEmpty()) {
            return false;
        }

        // Tries to stack the given item inside the inventory
        for (final ItemStack slot : inventory) {
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, toInsert)) {
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
            ContainerHelper.loadAllItems(compound.getCompound("Inventory"), inventory, this.registryAccess());
        }
        if (compound.contains("BlocksMined")) {
            blocksMinedCount = compound.getInt("BlocksMined");
        }
        if (compound.contains("GearsToRepair")) {
            gearsToRepairCount = compound.getInt("GearsToRepair");
        }
        if (compound.contains("State")) {
            setState(compound.getInt("State"));
        }

    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        if (this.getOwnerUUID() != null) {
            compound.putUUID("OwnerUUID", this.getOwnerUUID());
        }

        compound.putInt("BlocksMined", blocksMinedCount);
        compound.putInt("GearsToRepair", gearsToRepairCount);
        compound.putInt("State", getEntityData().get(STATE));

        compound.putInt("InventorySize", inventory.size());
        final CompoundTag inventoryTag = new CompoundTag();
        ContainerHelper.saveAllItems(inventoryTag, inventory, this.registryAccess());
        compound.put("Inventory", inventoryTag);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide) {
            return false;
        }

        // Can only be collected by the drill's owner
        final Entity entity = source.getEntity();
        if (entity instanceof Player player && player.getUUID().equals(getOwnerUUID())) {
            final ItemStack drillItem = new ItemStack(ClockworkItems.CLOCKWORK_DRILL.get());

            final CompoundTag entityTag = new CompoundTag();
            addAdditionalSaveData(entityTag);
            drillItem.set(DataComponents.CUSTOM_DATA, CustomData.of(entityTag));

            if (!player.getInventory().add(drillItem)) {
                Block.popResource(level(), blockPosition(), drillItem);
            }

            discard();
            return true;

        }

        return false;
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
        return new ClockworkDrillMenu(i, inventory, this, drillData);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown() && player.getUUID().equals(getOwnerUUID())) {
            if (player instanceof ServerPlayer serverPlayer) {
                KnightLib.PLATFORM.openMenu(serverPlayer, this, buf -> buf.writeInt(getId()));
            }

            return InteractionResult.SUCCESS;
        }

        final int maxGearAmount = ClockworkConfig.DRILL_CLOCKWORK_GEAR_AMOUNT;
        final ItemStack itemStack = player.getItemInHand(hand);
        if (isBroken()) {
            if (itemStack.is(ClockworkItems.CLOCKWORK_GEAR.get())) {
                if (gearsToRepairCount < maxGearAmount - 1) {
                    ++gearsToRepairCount;
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }

                }
                else {
                    player.displayClientMessage(Component.translatable("message.clockwork.broken_drill_repaired"), true);

                    setState(0);
                    triggerAnim("activateController", "activate");

                    level().playSound(null, blockPosition(), ClockworkSounds.DRILL_REPAIR.get(), SoundSource.NEUTRAL);

                    blocksMinedCount = 0;
                    gearsToRepairCount = 0;

                    spawnParticles(ParticleTypes.POOF, 10);

                    return InteractionResult.SUCCESS;
                }

                spawnParticles(ParticleTypes.POOF, 1);
                playSound(SoundEvents.PLAYER_LEVELUP, 1, 1);
            }
            else {
                playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1, 1);
            }

            player.displayClientMessage(Component.translatable("message.clockwork.broken_drill", maxGearAmount - gearsToRepairCount, maxGearAmount == 1 ? "" : "s"), true);
        }
        else {
            setState(isActive() ? 0 : 1);
            if (!isActive()) {
                setDrilling(false);
                setDrillingUp(false);
                resetBreakProgress(true);
                resetBreakProgress(false);
            }

        }

        return super.interact(player, hand);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", this::predicate));
        controllerRegistrar.add(new AnimationController<>(this, "activateController", 2,
                animationState -> PlayState.STOP).triggerableAnim("activate", ACTIVATE));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> event) {
        if (isBroken()) {
            event.setAnimation(DEACTIVATED);
        }
        else if (isActive() && !isDrilling() && !isDrillingUp()) {
            event.setAnimation(WALK);
        }
        else {
            event.setAnimation(IDLE);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

}