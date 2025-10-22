package dev.xylonity.bonsai.clockwork.client.sound.handler.custom;

import dev.xylonity.bonsai.clockwork.common.entity.passive.DragonflyEntity;
import dev.xylonity.bonsai.clockwork.client.sound.handler.SoundHandler;
import dev.xylonity.bonsai.clockwork.mixin.AbstractTickableSoundInstanceAccessor;
import dev.xylonity.bonsai.clockwork.registry.ClockworkSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;

public final class DragonflySoundHandler implements SoundHandler<DragonflyEntity> {

    private final Map<Integer, FlyLoop> flyById = new HashMap<>();
    private final Map<Integer, IdleLoop> idleById = new HashMap<>();

    @Override
    public boolean canHandle(Object entity) {
        return entity instanceof DragonflyEntity;
    }

    @Override
    public void tick(DragonflyEntity e) {
        int id = e.getId();
        boolean isFlying = (e.getState() == 1);

        // Flying loop
        if (isFlying) {
            FlyLoop current = flyById.get(id);
            if (current == null || current.isStopped()) {
                FlyLoop loop = new FlyLoop(ClockworkSounds.DRAGONFLY_FLY.get(), e);
                play(loop);
                flyById.put(id, loop);
            }

        }
        else {
            stopImmediate(flyById.remove(id));
        }

        // Idle loop
        if (!isFlying) {
            IdleLoop current = idleById.get(id);
            if (current == null || current.isStopped()) {
                IdleLoop loop = new IdleLoop(ClockworkSounds.DRAGONFLY_IDLE.get(), e);
                play(loop);
                idleById.put(id, loop);
            }

        }
        else {
            stopImmediate(idleById.remove(id));
        }

        if (isFlying) {
            stopImmediate(idleById.remove(id));
        }
        else {
            stopImmediate(flyById.remove(id));
        }

    }

    @Override
    public void stopAll(DragonflyEntity e) {
        int id = e.getId();
        stopImmediate(flyById.remove(id));
        stopImmediate(idleById.remove(id));
    }

    @Override
    public void clear() {
        flyById.values().forEach(DragonflySoundHandler::stopImmediate);
        idleById.values().forEach(DragonflySoundHandler::stopImmediate);
        flyById.clear();
        idleById.clear();
    }

    private static void play(AbstractTickableSoundInstance soundInstance) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSoundManager() != null) {
            minecraft.getSoundManager().play(soundInstance);
        }

    }

    private static void stopImmediate(AbstractTickableSoundInstance soundInstance) {
        if (soundInstance != null) {
            ((AbstractTickableSoundInstanceAccessor) soundInstance).clockwork$stop();
        }

    }

    private static abstract class DragonflyLoop extends AbstractTickableSoundInstance {

        protected final DragonflyEntity entity;

        protected DragonflyLoop(SoundEvent event, DragonflyEntity entity) {
            super(event, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
            this.entity = entity;
            this.looping = true;
            this.delay = 0;
            this.volume = 0.4f;
            this.pitch  = 1.0F;
            this.x = (float) entity.getX();
            this.y = (float) entity.getY();
            this.z = (float) entity.getZ();
        }

        @Override
        public void tick() {
            if (entity == null || entity.isRemoved() || !entity.level().isClientSide) {
                this.stop();
                return;
            }

            this.x = (float) entity.getX();
            this.y = (float) entity.getY();
            this.z = (float) entity.getZ();

            if (!isStillValidFor(entity)) {
                this.stop();
            }

        }

        protected abstract boolean isStillValidFor(DragonflyEntity e);
    }

    private static final class FlyLoop extends DragonflyLoop {

        FlyLoop(SoundEvent event, DragonflyEntity entity) {
            super(event, entity);
        }

        @Override
        protected boolean isStillValidFor(DragonflyEntity e) {
            return e.getState() == 1 && !e.isRemoved();
        }

    }

    private static final class IdleLoop extends DragonflyLoop {

        IdleLoop(SoundEvent event, DragonflyEntity entity) {
            super(event, entity);
        }

        @Override
        protected boolean isStillValidFor(DragonflyEntity e) {
            return e.getState() != 1 && !e.isRemoved();
        }

    }

}