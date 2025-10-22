package dev.xylonity.bonsai.clockwork.client.sound;

import dev.xylonity.bonsai.clockwork.client.sound.handler.SoundHandler;
import dev.xylonity.bonsai.clockwork.proxy.ISoundProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Main sound proxy handler, where every single sound handler must hook to
 */
@SuppressWarnings("unchecked")
public final class ClientSoundProxy implements ISoundProxy {

    private final List<SoundHandler<?>> handlers = new ArrayList<>();

    public void registerHandler(SoundHandler<?> handler) {
        if (handler != null && !handlers.contains(handler)) {
            handlers.add(handler);
        }

    }

    @Override
    public void tickSounds(Entity entity) {
        if (Minecraft.getInstance().level == null || entity == null) return;

        for (SoundHandler<?> handler : handlers) {
            if (handler.canHandle(entity)) {
                SoundHandler<Entity> generic = (SoundHandler<Entity>) handler;
                generic.tick(entity);
                return;
            }
        }

    }

    @Override
    public void stopAllFor(Entity entity) {
        if (entity == null) return;

        for (SoundHandler<?> handler : handlers) {
            if (handler.canHandle(entity)) {
                SoundHandler<Entity> generic = (SoundHandler<Entity>) handler;
                generic.stopAll(entity);
                return;
            }
        }

    }

    @Override
    public void clearAll() {
        for (SoundHandler<?> handler : handlers) {
            handler.clear();
        }

    }

}